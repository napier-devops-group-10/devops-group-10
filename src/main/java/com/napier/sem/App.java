package com.napier.sem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class App {
    /**
     * Connection to MySQL database.
     */
    private Connection con = null;

    /**
     * Connect to the MySQL database with retry logic.
     *
     * @param location The host:port location of the database.
     * @param delay    Delay in milliseconds between retries.
     */
    public void connect(String location, int delay) {
        try {
            // Load Database driver
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
                // Wait for DB to be available
                Thread.sleep(delay);
                // Connect to database
                con = DriverManager.getConnection(url, user, password);
                System.out.println("Successfully connected to MySQL database: " + database);
                break;
            } catch (SQLException sqle) {
                System.err.println("Failed to connect to database attempt " + (i + 1) + ": " + sqle.getMessage());
            } catch (InterruptedException ie) {
                System.err.println("Thread interrupted: " + ie.getMessage());
            }
        }
    }

    /**
     * Disconnect from the MySQL database.
     */
    public void disconnect() {
        if (con != null) {
            try {
                // Close connection
                con.close();
                System.out.println("Database connection closed cleanly.");
            } catch (Exception e) {
                System.err.println("Error closing connection to database: " + e.getMessage());
            }
        }
    }

    /**
     * Test query to retrieve a single country by code.
     *
     * @param code The country code.
     * @return Country object or null if not found.
     */
    public Country getCountry(String code) {
        if (con == null) {
            System.err.println("Database connection is not established.");
            return null;
        }

        try {
            Statement stmt = con.createStatement();
            String strSelect = "SELECT Code, Name, Continent, Region, SurfaceArea, Population FROM country WHERE Code = '" + code + "'";
            ResultSet rset = stmt.executeQuery(strSelect);

            if (rset.next()) {
                Country c = new Country();
                c.code = rset.getString("Code");
                c.name = rset.getString("Name");
                c.continent = rset.getString("Continent");
                c.region = rset.getString("Region");
                c.surfaceArea = rset.getDouble("SurfaceArea");
                c.population = rset.getInt("Population");
                return c;
            } else {
                return null;
            }
        } catch (Exception e) {
            System.err.println("Failed to get country details: " + e.getMessage());
            return null;
        }
    }

    /**
     * REQ-01: Retrieves all countries in the world organised by largest population to smallest.
     *
     * @return List of Country objects, or empty list if an error occurs.
     */
    public ArrayList<Country> getAllCountriesWorld() {
        String sql = "SELECT c.Code, c.Name, c.Continent, c.Region, c.SurfaceArea, c.Population, ct.Name AS Capital " +
                "FROM country c " +
                "LEFT JOIN city ct ON c.Capital = ct.ID " +
                "ORDER BY c.Population DESC";
        return executeCountryQuery(sql, null);
    }

    /**
     * REQ-02: Retrieves all countries in a continent organised by largest population to smallest.
     *
     * @param continent The continent name to filter by (e.g., 'Europe').
     * @return List of Country objects, or empty list if an error occurs.
     */
    public ArrayList<Country> getCountriesByContinent(String continent) {
        String sql = "SELECT c.Code, c.Name, c.Continent, c.Region, c.SurfaceArea, c.Population, ct.Name AS Capital " +
                "FROM country c " +
                "LEFT JOIN city ct ON c.Capital = ct.ID " +
                "WHERE c.Continent = ? " +
                "ORDER BY c.Population DESC";
        return executeCountryQuery(sql, continent);
    }

    /**
     * REQ-03: Retrieves all countries in a region organised by largest population to smallest.
     *
     * @param region The region name to filter by (e.g., 'Western Europe').
     * @return List of Country objects, or empty list if an error occurs.
     */
    public ArrayList<Country> getCountriesByRegion(String region) {
        String sql = "SELECT c.Code, c.Name, c.Continent, c.Region, c.SurfaceArea, c.Population, ct.Name AS Capital " +
                "FROM country c " +
                "LEFT JOIN city ct ON c.Capital = ct.ID " +
                "WHERE c.Region = ? " +
                "ORDER BY c.Population DESC";
        return executeCountryQuery(sql, region);
    }

    /**
     * REQ-04: Retrieves the top N populated countries in the world where N is provided by the user.
     *
     * @param n The number of top populated countries to retrieve.
     * @return List of Country objects, or empty list if an error occurs.
     */
    public ArrayList<Country> getTopNCountriesWorld(int n) {
        ArrayList<Country> countries = new ArrayList<>();
        if (n <= 0) {
            System.out.println("Invalid value for N: " + n + ". Value must be greater than 0.");
            return countries;
        }

        if (con == null) {
            System.err.println("Database connection is not established.");
            return countries;
        }

        String sql = "SELECT c.Code, c.Name, c.Continent, c.Region, c.SurfaceArea, c.Population, ct.Name AS Capital " +
                "FROM country c " +
                "LEFT JOIN city ct ON c.Capital = ct.ID " +
                "ORDER BY c.Population DESC " +
                "LIMIT ?";

        try {
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, n);
            ResultSet rset = pstmt.executeQuery();

            while (rset.next()) {
                Country country = new Country();
                country.code = rset.getString("Code");
                country.name = rset.getString("Name");
                country.continent = rset.getString("Continent");
                country.region = rset.getString("Region");
                country.surfaceArea = rset.getDouble("SurfaceArea");
                country.population = rset.getInt("Population");
                country.capital = rset.getString("Capital");
                if (country.capital == null) {
                    country.capital = "N/A";
                }
                countries.add(country);
            }
        } catch (SQLException e) {
            System.err.println("Error executing top N countries query: " + e.getMessage());
        }
        return countries;
    }

    /**
     * Helper method to execute country SQL queries and map results to Country models.
     *
     * @param sql       The SQL query string.
     * @param parameter Optional single string parameter for parameterized WHERE clauses.
     * @return Populated ArrayList of Country instances.
     */
    private ArrayList<Country> executeCountryQuery(String sql, String parameter) {
        ArrayList<Country> countries = new ArrayList<>();
        if (con == null) {
            System.err.println("Database connection is not established.");
            return countries;
        }

        try {
            PreparedStatement pstmt = con.prepareStatement(sql);
            if (parameter != null) {
                pstmt.setString(1, parameter);
            }
            ResultSet rset = pstmt.executeQuery();
            while (rset.next()) {
                Country country = new Country();
                country.code = rset.getString("Code");
                country.name = rset.getString("Name");
                country.continent = rset.getString("Continent");
                country.region = rset.getString("Region");
                country.surfaceArea = rset.getDouble("SurfaceArea");
                country.population = rset.getInt("Population");
                country.capital = rset.getString("Capital");
                if (country.capital == null) {
                    country.capital = "N/A";
                }
                countries.add(country);
            }
        } catch (SQLException e) {
            System.err.println("Error executing country query: " + e.getMessage());
        }
        return countries;
    }

    /**
     * Prints formatted country reporting table to standard output.
     *
     * @param countries   List of Country records to print.
     * @param reportTitle Display title of the report.
     */
    public void printCountries(ArrayList<Country> countries, String reportTitle) {
        if (countries == null || countries.isEmpty()) {
            System.out.println("No countries found for report: " + reportTitle);
            return;
        }

        System.out.println("\n=================================================================================================================================");
        System.out.println(" REPORT: " + reportTitle);
        System.out.println("=================================================================================================================================");
        System.out.printf("%-6s %-40s %-20s %-30s %-15s %-25s%n", "Code", "Name", "Continent", "Region", "Population", "Capital");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

        for (Country country : countries) {
            System.out.printf("%-6s %-40s %-20s %-30s %-15d %-25s%n",
                    country.code,
                    country.name,
                    country.continent,
                    country.region,
                    country.population,
                    country.capital);
        }
        System.out.println("Total records displayed: " + countries.size() + "\n");
    }

    public static void main(String[] args) {
        // Create new Application
        App app = new App();

        // Read connection parameters from environment or default to local/compose settings
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "3307");
        String location = host + ":" + port;

        // If running inside docker container directly, port might be 3306 or passed as argument
        if (args.length > 0) {
            location = args[0];
        }

        // Connect to database (10 attempts, 3-second wait)
        app.connect(location, 3000);

        // Verification query: smoke test for initial database connectivity
        Country testCountry = app.getCountry("ESP");
        if (testCountry != null) {
            System.out.println("Smoke Test Successful! -> Code: " + testCountry.code +
                    ", Name: " + testCountry.name +
                    ", Continent: " + testCountry.continent +
                    ", Population: " + testCountry.population);
        } else {
            System.out.println("Connection established, but no country data found with code ESP (Check if world.sql is populated).");
        }

        // 1. REQ-01: All countries in the world ordered by population
        ArrayList<Country> worldCountries = app.getAllCountriesWorld();
        app.printCountries(worldCountries, "REQ-01: All Countries in the World by Population");

        // 2. REQ-02: Countries in Europe ordered by population
        ArrayList<Country> europeCountries = app.getCountriesByContinent("Europe");
        app.printCountries(europeCountries, "REQ-02: Countries in Europe by Population");

        // 3. REQ-03: Countries in Western Europe ordered by population
        ArrayList<Country> westernEuropeCountries = app.getCountriesByRegion("Western Europe");
        app.printCountries(westernEuropeCountries, "REQ-03: Countries in Western Europe by Population");

        // 4. REQ-04: Top N populated countries in the world (e.g., N = 10)
        ArrayList<Country> top10Countries = app.getTopNCountriesWorld(10);
        app.printCountries(top10Countries, "REQ-04: Top 10 Populated Countries in the World");

        // Disconnect
        app.disconnect();
    }
}