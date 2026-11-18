package com.napier.sem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Main application class responsible for managing database connections
 * and executing population reporting queries for the World database.
 */
public class App {

    /**
     * Active connection to the MySQL database.
     * Kept private to follow encapsulation guidelines.
     */
    private Connection con = null;

    /**
     * Gets the active database connection.
     *
     * @return Active SQL Connection object.
     */
    public Connection getConnection() {
        return this.con;
    }

    /**
     * Connects to the MySQL database using retry logic to handle container startup delays.
     *
     * @param location The host and port where MySQL is running (e.g. 'localhost:3307' or 'db:3306').
     * @param delay    Delay in milliseconds between connection retries.
     */
    public void connect(String location, int delay) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Could not load SQL driver: " + e.getMessage());
            System.exit(-1);
        }

        int retries = 10;
        String user = System.getenv().getOrDefault("DB_USER", "population_user");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "your_database_password");
        String database = System.getenv().getOrDefault("DB_NAME", "world");
        String url = "jdbc:mysql://" + location + "/" + database + "?allowPublicKeyRetrieval=true&useSSL=false";

        for (int i = 0; i < retries; ++i) {
            System.out.println("Connecting to database at " + url + " (Attempt " + (i + 1) + "/" + retries + ")...");
            try {
                Thread.sleep(delay);
                con = DriverManager.getConnection(url, user, password);
                System.out.println("Successfully connected to MySQL database: " + database);
                break;
            } catch (SQLException sqle) {
                System.err.println("Failed to connect to database attempt " + (i + 1) + ": " + sqle.getMessage());
            } catch (InterruptedException ie) {
                System.err.println("Thread sleep interrupted: " + ie.getMessage());
            }
        }
    }

    /**
     * Closes the active database connection cleanly.
     */
    public void disconnect() {
        if (con != null) {
            try {
                con.close();
                System.out.println("Database connection closed cleanly.");
            } catch (SQLException e) {
                System.err.println("Error closing connection to database: " + e.getMessage());
            }
        }
    }

    /**
     * Quick smoke test query to retrieve a single country by its 3-letter code.
     *
     * @param code The 3-letter country code (e.g., 'ESP').
     * @return A Country object if found, otherwise null.
     */
    public Country getCountry(String code) {
        if (con == null) {
            System.err.println("Database connection is not established.");
            return null;
        }

        try (Statement stmt = con.createStatement()) {
            String sql = "SELECT Code, Name, Continent, Region, SurfaceArea, Population " +
                    "FROM country " +
                    "WHERE Code = '" + code + "'";
            try (ResultSet rset = stmt.executeQuery(sql)) {
                if (rset.next()) {
                    Country country = new Country();
                    country.setCode(rset.getString("Code"));
                    country.setName(rset.getString("Name"));
                    country.setContinent(rset.getString("Continent"));
                    country.setRegion(rset.getString("Region"));
                    country.setSurfaceArea(rset.getDouble("SurfaceArea"));
                    country.setPopulation(rset.getInt("Population"));
                    return country;
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get country details: " + e.getMessage());
        }
        return null;
    }

    /**
     * Application entry point executing all population reports in sequential order.
     *
     * @param args Optional command-line arguments. First argument can override host:port location.
     */
    public static void main(String[] args) {
        App app = new App();

        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "3307");
        String location = host + ":" + port;

        if (args.length > 0) {
            location = args[0];
        }

        app.connect(location, 3000);

        // Smoke test
        Country testCountry = app.getCountry("ESP");
        if (testCountry != null) {
            System.out.println("Smoke Test Successful! -> Code: " + testCountry.getCode() +
                    ", Name: " + testCountry.getName() +
                    ", Continent: " + testCountry.getContinent() +
                    ", Population: " + testCountry.getPopulation());
        } else {
            System.out.println("Smoke test: ESP not found. Verifying database population...");
        }

        // ==========================================
        // REQ-01: All Countries in the World
        // ==========================================
        System.out.println("\n--- Testing Requirement 1 ---");
        Requirement1 req1 = new Requirement1(app.getConnection());
        ArrayList<Country> worldCountries = req1.getAllCountriesWorld();
        req1.printCountries(worldCountries);

        // ==========================================
        // REQ-02: All Countries in a Continent
        // ==========================================
        System.out.println("\n--- Testing Requirement 2 ---");
        Requirement2 req2 = new Requirement2(app.getConnection());
        ArrayList<Country> europeCountries = req2.getCountriesByContinent("Europe");
        req2.printCountries(europeCountries, "Europe");

        // ==========================================
        // REQ-03: All Countries in a Region
        // ==========================================
        System.out.println("\n--- Testing Requirement 3 ---");
        Requirement3 req3 = new Requirement3(app.getConnection());
        ArrayList<Country> westernEuropeCountries = req3.getCountriesByRegion("Western Europe");
        req3.printCountries(westernEuropeCountries, "Western Europe");

        // ==========================================
        // REQ-04: Top N Countries in the World
        // ==========================================
        System.out.println("\n--- Testing Requirement 4 ---");
        Requirement4 req4 = new Requirement4(app.getConnection());
        ArrayList<Country> top10Countries = req4.getTopNCountriesWorld(10);
        req4.printCountries(top10Countries, 10);

        // ==========================================
        // REQ-05: Top N Countries in a Continent
        // ==========================================
        System.out.println("\n--- Testing Requirement 5 ---");
        Requirement5 req5 = new Requirement5(app.getConnection());
        ArrayList<Country> topEurope = req5.getTopNCountriesInContinent("Europe", 5);
        req5.printCountries(topEurope, "Europe", 5);

        // ==========================================
        // REQ-06: Top N Countries in a Region
        // ==========================================
        System.out.println("\n--- Testing Requirement 6 ---");
        Requirement6 req6 = new Requirement6(app.getConnection());
        ArrayList<Country> topCaribbean = req6.getTopCountriesInRegion("Caribbean", 5);
        req6.printReport(topCaribbean, "Caribbean", 5);

        // ==========================================
        // REQ-07: All Cities in the World
        // ==========================================
        System.out.println("\n--- Testing Requirement 7 ---");
        Requirement7 req7 = new Requirement7(app.getConnection());
        ArrayList<City> worldCities = req7.getAllCitiesWorld();
        req7.printCities(worldCities);

        // ==========================================
        // REQ-08: All Cities in a Continent
        // ==========================================
        System.out.println("\n--- Testing Requirement 8 ---");
        Requirement8 req8 = new Requirement8(app.getConnection());
        ArrayList<City> europeCities = req8.getCitiesInContinent("Europe");
        req8.printCities(europeCities, "Europe");

        // ==========================================
        // REQ-09: All Cities in a Region
        // ==========================================
        System.out.println("\n--- Testing Requirement 9 ---");
        Requirement9 req9 = new Requirement9(app.getConnection());
        ArrayList<City> westernEuropeCities = req9.getCitiesInRegion("Western Europe");
        req9.printCities(westernEuropeCities, "Western Europe");

        // ==========================================
        // REQ-10: All Cities in a Country
        // ==========================================
        System.out.println("\n--- Testing Requirement 10 ---");
        Requirement10 req10 = new Requirement10(app.getConnection());
        ArrayList<City> spainCities = req10.getCitiesInCountry("Spain");
        req10.printCities(spainCities, "Spain");

        // ==========================================
        // REQ-11: All Cities in a District
        // ==========================================
        System.out.println("\n--- Testing Requirement 11 ---");
        Requirement11 req11 = new Requirement11(app.getConnection());
        ArrayList<City> balearesCities = req11.getCitiesInDistrict("Baleares");
        req11.printCities(balearesCities, "Baleares");

        // ==========================================
        // REQ-12: Top N Cities in the World
        // ==========================================
        System.out.println("\n--- Testing Requirement 12 ---");
        Requirement12 req12 = new Requirement12(app.getConnection());
        ArrayList<City> top10CitiesWorld = req12.getTopNCitiesWorld(10);
        req12.printCities(top10CitiesWorld, 10);

        // ==========================================
        // REQ-13: Top N Cities in a Continent
        // ==========================================
        System.out.println("\n--- Testing Requirement 13 ---");
        Requirement13 req13 = new Requirement13(app.getConnection());
        ArrayList<City> top5CitiesEurope = req13.getTopNCitiesInContinent("Europe", 5);
        req13.printCities(top5CitiesEurope, "Europe", 5);

        // ==========================================
        // REQ-14: Top N Cities in a Region
        // ==========================================
        System.out.println("\n--- Testing Requirement 14 ---");
        Requirement14 req14 = new Requirement14(app.getConnection());
        ArrayList<City> top5CitiesWesternEurope = req14.getTopNCitiesInRegion("Western Europe", 5);
        req14.printCities(top5CitiesWesternEurope, "Western Europe", 5);

        // ==========================================
        // REQ-15: Top N Cities in a Country
        // ==========================================
        System.out.println("\n--- Testing Requirement 15 ---");
        Requirement15 req15 = new Requirement15(app.getConnection());
        ArrayList<City> top5CitiesSpain = req15.getTopNCitiesInCountry("Spain", 5);
        req15.printCities(top5CitiesSpain, "Spain", 5);

        // ==========================================
        // REQ-16: Top N Cities in a District
        // ==========================================
        System.out.println("\n--- Testing Requirement 16 ---");
        Requirement16 req16 = new Requirement16(app.getConnection());
        ArrayList<City> top3CitiesGelderland = req16.getTopNCitiesInDistrict("Gelderland", 3);
        req16.printCities(top3CitiesGelderland, "Gelderland", 3);

        // ==========================================
        // REQ-17: All Capital Cities in the World
        // ==========================================
        System.out.println("\n--- Testing Requirement 17 ---");
        Requirement17 req17 = new Requirement17(app.getConnection());
        ArrayList<City> worldCapitalCities = req17.getAllCapitalCitiesWorld();
        req17.printCapitalCities(worldCapitalCities);

        // ==========================================
        // REQ-19: All Capital Cities in a Region
        // ==========================================
        System.out.println("\n--- Testing Requirement 19 ---");
        Requirement19 req19 = new Requirement19(app.getConnection());
        ArrayList<City> westernEuropeCapitals = req19.getCapitalCitiesInRegion("Western Europe");
        req19.printCapitalCities(westernEuropeCapitals, "Western Europe");

        // ==========================================
        // REQ-20: Top N Capital Cities in the World
        // ==========================================
        System.out.println("\n--- Testing Requirement 20 ---");
        Requirement20 req20 = new Requirement20(app.getConnection());
        ArrayList<City> top10CapitalCitiesWorld = req20.getTopNCapitalCitiesWorld(10);
        req20.printCapitalCities(top10CapitalCitiesWorld, 10);

        // ==========================================
        // REQ-21: Top N Capital Cities in a Continent
        // ==========================================
        System.out.println("\n--- Testing Requirement 21 ---");
        Requirement21 req21 = new Requirement21(app.getConnection());
        ArrayList<City> top5CapitalCitiesEurope = req21.getTopNCapitalCitiesInContinent("Europe", 5);
        req21.printCapitalCities(top5CapitalCitiesEurope, "Europe", 5);

        // ==========================================
        // REQ-22: Top N Capital Cities in a Region
        // ==========================================
        System.out.println("\n--- Testing Requirement 22 ---");
        Requirement22 req22 = new Requirement22(app.getConnection());
        ArrayList<City> top3CapitalCitiesWesternEurope = req22.getTopNCapitalCitiesInRegion("Western Europe", 3);
        req22.printCapitalCities(top3CapitalCitiesWesternEurope, "Western Europe", 3);

        // ==========================================
        // REQ-23: Urban vs Rural Population by Continent
        // ==========================================
        System.out.println("\n--- Testing Requirement 23 ---");
        Requirement23 req23 = new Requirement23(app.getConnection());
        ArrayList<String> continentPopulationBreakdown = req23.getContinentPopulationBreakdown();
        req23.printReport(continentPopulationBreakdown);

        // ==========================================
        // REQ-24: Urban vs Rural Population by Region
        // ==========================================
        System.out.println("\n--- Testing Requirement 24 ---");
        Requirement24 req24 = new Requirement24(app.getConnection());
        ArrayList<String> regionPopulationBreakdown = req24.getRegionPopulationBreakdown();
        req24.printReport(regionPopulationBreakdown);

        // ==========================================
        // REQ-25: Urban vs Rural Population by Country
        // ==========================================
        System.out.println("\n--- Testing Requirement 25 ---");
        Requirement25 req25 = new Requirement25(app.getConnection());
        ArrayList<String> countryPopulationBreakdown = req25.getCountryPopulationBreakdown();
        req25.printReport(countryPopulationBreakdown);

        // ==========================================
        // REQ-26: Total Population of the World
        // ==========================================
        System.out.println("\n--- Testing Requirement 26 ---");
        Requirement26 req26 = new Requirement26(app.getConnection());
        req26.printWorldPopulation();

        // ==========================================
        // REQ-27: Total Population of a Continent
        // ==========================================
        System.out.println("\n--- Testing Requirement 27 ---");
        Requirement27 req27 = new Requirement27(app.getConnection());
        req27.printContinentPopulation("Asia");

        // ==========================================
        // REQ-30: Total Population of a District
        // ==========================================
        System.out.println("\n--- Testing Requirement 30 ---");
        Requirement30 req30 = new Requirement30(app.getConnection());
        req30.printDistrictPopulation("Baleares");

        // ==========================================
        // REQ-31: Total Population of a City
        // ==========================================
        System.out.println("\n--- Testing Requirement 31 ---");
        Requirement31 req31 = new Requirement31(app.getConnection());
        req31.printCityPopulation("Palma de Mallorca");

        // ==========================================
        // REQ-32: Language Population Report
        // ==========================================
        System.out.println("\n--- Testing Requirement 32 ---");
        Requirement32 req32 = new Requirement32(app.getConnection());
        ArrayList<String> languagePopulationReport = req32.getLanguagePopulationReport();
        req32.printReport(languagePopulationReport);

        // Disconnect cleanly from database
        app.disconnect();
    }
}