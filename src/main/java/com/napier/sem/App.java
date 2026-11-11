
package com.napier.sem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Main application class to manage database connection and execute population reports.
 */
public class App {

    /**
     * Connection object to connect to the MySQL database.
     * Kept private to follow encapsulation guidelines.
     */
    private Connection con = null;

    /**
     * Gets the active database connection.
     *
     * @return Connection object.
     */
    public Connection getConnection() {
        return this.con;
    }

    /**
     * Connect to the MySQL database with retry logic.
     *
     * @param location The host:port location of the database.
     * @param delay    Delay in milliseconds between retries.
     */
    public void connect(String location, int delay) {
        try {
            // Load the MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Could not load SQL driver: " + e.getMessage());
            System.exit(-1);
        }

        // Connection retry settings
        int retries = 10;
        String user = System.getenv().getOrDefault("DB_USER", "population_user");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "your_database_password");
        String database = System.getenv().getOrDefault("DB_NAME", "world");
        String url = "jdbc:mysql://" + location + "/" + database + "?allowPublicKeyRetrieval=true&useSSL=false";

        // Try connecting in a loop to wait for the database container to start
        for (int i = 0; i < retries; ++i) {
            System.out.println("Connecting to database at " + url + " (Attempt " + (i + 1) + "/" + retries + ")...");
            try {
                // Wait for the DB service to be ready
                Thread.sleep(delay);

                // Try to establish connection
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
     * Disconnect cleanly from the MySQL database.
     */
    public void disconnect() {
        if (con != null) {
            try {
                // Close database connection
                con.close();
                System.out.println("Database connection closed cleanly.");
            } catch (Exception e) {
                System.err.println("Error closing connection to database: " + e.getMessage());
            }
        }
    }

    /**
     * Test query to retrieve a single country by its 3-letter code.
     *
     * @param code The country code (e.g. 'ESP').
     * @return Country object or null if not found.
     */
    public Country getCountry(String code) {
        if (con == null) {
            System.err.println("Database connection is not established.");
            return null;
        }

        try {
            // Simple SQL query to find one country
            Statement stmt = con.createStatement();
            String strSelect = "SELECT Code, Name, Continent, Region, SurfaceArea, Population FROM country WHERE Code = '" + code + "'";
            ResultSet rset = stmt.executeQuery(strSelect);

            if (rset.next()) {
                Country c = new Country();
                c.setCode(rset.getString("Code"));
                c.setName(rset.getString("Name"));
                c.setContinent(rset.getString("Continent"));
                c.setRegion(rset.getString("Region"));
                c.setSurfaceArea(rset.getDouble("SurfaceArea"));
                c.setPopulation(rset.getInt("Population"));
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
     * @return List of Country objects.
     */
    public ArrayList<Country> getAllCountriesWorld() {
        // Query joining country with city to get capital city name
        String sql = "SELECT c.Code, c.Name, c.Continent, c.Region, c.SurfaceArea, c.Population, ct.Name AS Capital " +
                "FROM country c " +
                "LEFT JOIN city ct ON c.Capital = ct.ID " +
                "ORDER BY c.Population DESC";
        return executeCountryQuery(sql, null);
    }

    /**
     * REQ-02: Retrieves all countries in a continent organised by largest population to smallest.
     *
     * @param continent The continent name to filter by (e.g. 'Europe').
     * @return List of Country objects.
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
     * @param region The region name to filter by (e.g. 'Western Europe').
     * @return List of Country objects.
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
     * @return List of Country objects.
     */
    public ArrayList<Country> getTopNCountriesWorld(int n) {
        ArrayList<Country> countries = new ArrayList<>();

        // Validate that N is positive
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
            // Using prepared statement for the LIMIT parameter
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, n);
            ResultSet rset = pstmt.executeQuery();

            // Loop through results and populate country objects
            while (rset.next()) {
                Country country = new Country();
                country.setCode(rset.getString("Code"));
                country.setName(rset.getString("Name"));
                country.setContinent(rset.getString("Continent"));
                country.setRegion(rset.getString("Region"));
                country.setSurfaceArea(rset.getDouble("SurfaceArea"));
                country.setPopulation(rset.getInt("Population"));

                String capital = rset.getString("Capital");
                if (capital == null) {
                    capital = "N/A";
                }
                country.setCapital(capital);

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
     * @param parameter Optional parameter for WHERE clause.
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
                country.setCode(rset.getString("Code"));
                country.setName(rset.getString("Name"));
                country.setContinent(rset.getString("Continent"));
                country.setRegion(rset.getString("Region"));
                country.setSurfaceArea(rset.getDouble("SurfaceArea"));
                country.setPopulation(rset.getInt("Population"));

                String capital = rset.getString("Capital");
                if (capital == null) {
                    capital = "N/A";
                }
                country.setCapital(capital);

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

        // Table header
        System.out.println("\n=================================================================================================================================");
        System.out.println(" REPORT: " + reportTitle);
        System.out.println("=================================================================================================================================");
        System.out.printf("%-6s %-40s %-20s %-30s %-15s %-25s%n", "Code", "Name", "Continent", "Region", "Population", "Capital");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

        // Print each row
        for (Country country : countries) {
            System.out.printf("%-6s %-40s %-20s %-30s %-15d %-25s%n",
                    country.getCode(),
                    country.getName(),
                    country.getContinent(),
                    country.getRegion(),
                    country.getPopulation(),
                    country.getCapital());
        }
        System.out.println("Total records displayed: " + countries.size() + "\n");
    }

    public static void main(String[] args) {
        // Create new Application instance
        App app = new App();

        // Database connection settings
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "3307");
        String location = host + ":" + port;

        // Allow command line argument to override location (e.g. inside docker)
        if (args.length > 0) {
            location = args[0];
        }

        // Connect to database (10 attempts, 3-second delay)
        app.connect(location, 3000);

        // Verification query: smoke test for initial database connectivity
        Country testCountry = app.getCountry("ESP");
        if (testCountry != null) {
            System.out.println("Smoke Test Successful! -> Code: " + testCountry.getCode() +
                    ", Name: " + testCountry.getName() +
                    ", Continent: " + testCountry.getContinent() +
                    ", Population: " + testCountry.getPopulation());
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

        // 4. REQ-04: Top N populated countries in the world (N = 10)
        ArrayList<Country> top10Countries = app.getTopNCountriesWorld(10);
        app.printCountries(top10Countries, "REQ-04: Top 10 Populated Countries in the World");

        // ==========================================
        // TEST: Requirement 5
        // ==========================================
        System.out.println("\n--- Testing Requirement 5 ---");
        // Using app.getConnection() because con is encapsulated with private access
        Requirement5 req5 = new Requirement5(app.getConnection());

        // Test with Continent = "Europe" and N = 5
        ArrayList<Country> topEurope = req5.getTopNCountriesInContinent("Europe", 5);
        req5.printCountries(topEurope);

        // Test with Continent = "Asia" and N = 3
        ArrayList<Country> topAsia = req5.getTopNCountriesInContinent("Asia", 3);
        req5.printCountries(topAsia);
        // ==========================================

        // REQ-27: Total population of a continent.
        // Reuse the existing database connection.
        Requirement27 req27 = new Requirement27(app.getConnection());
        req27.printContinentPopulation("Asia");

        // Disconnect cleanly from database
        app.disconnect();
    }
}
