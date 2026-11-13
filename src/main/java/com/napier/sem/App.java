package com.napier.sem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
        // Step 1: Load the MySQL JDBC Driver class
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Could not load SQL driver: " + e.getMessage());
            System.exit(-1);
        }

        // Step 2: Read database credentials from environment variables or use safe defaults
        int retries = 10;
        String user = System.getenv().getOrDefault("DB_USER", "population_user");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "your_database_password");
        String database = System.getenv().getOrDefault("DB_NAME", "world");
        String url = "jdbc:mysql://" + location + "/" + database + "?allowPublicKeyRetrieval=true&useSSL=false";

        // Step 3: Loop and retry connecting to allow MySQL container enough time to boot up
        for (int i = 0; i < retries; ++i) {
            System.out.println("Connecting to database at " + url + " (Attempt " + (i + 1) + "/" + retries + ")...");
            try {
                // Wait before attempting connection
                Thread.sleep(delay);

                // Open the database connection
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

        try {
            // Build simple select query
            Statement stmt = con.createStatement();
            String sql = "SELECT Code, Name, Continent, Region, SurfaceArea, Population " +
                    "FROM country " +
                    "WHERE Code = '" + code + "'";
            ResultSet rset = stmt.executeQuery(sql);

            // If a row is returned, extract and set the country attributes
            if (rset.next()) {
                Country country = new Country();
                country.setCode(rset.getString("Code"));
                country.setName(rset.getString("Name"));
                country.setContinent(rset.getString("Continent"));
                country.setRegion(rset.getString("Region"));
                country.setSurfaceArea(rset.getDouble("SurfaceArea"));
                country.setPopulation(rset.getInt("Population"));
                return country;
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Failed to get country details: " + e.getMessage());
            return null;
        }
    }

    /**
     * REQ-01: Retrieves all countries in the world organised by largest population to smallest.
     *
     * @return List of all Country objects ordered by descending population.
     */
    public ArrayList<Country> getAllCountriesWorld() {
        ArrayList<Country> countries = new ArrayList<>();

        if (con == null) {
            System.err.println("Database connection is not established.");
            return countries;
        }

        // Query joining country with city to resolve the capital city name
        String sql = "SELECT c.Code, c.Name, c.Continent, c.Region, c.SurfaceArea, c.Population, ct.Name AS Capital " +
                "FROM country c " +
                "LEFT JOIN city ct ON c.Capital = ct.ID " +
                "ORDER BY c.Population DESC";

        try {
            Statement stmt = con.createStatement();
            ResultSet rset = stmt.executeQuery(sql);

            // Loop through all database records
            while (rset.next()) {
                Country country = new Country();
                country.setCode(rset.getString("Code"));
                country.setName(rset.getString("Name"));
                country.setContinent(rset.getString("Continent"));
                country.setRegion(rset.getString("Region"));
                country.setSurfaceArea(rset.getDouble("SurfaceArea"));
                country.setPopulation(rset.getInt("Population"));

                // Fallback for countries without an assigned capital city
                String capitalName = rset.getString("Capital");
                if (capitalName == null) {
                    capitalName = "N/A";
                }
                country.setCapital(capitalName);

                countries.add(country);
            }
        } catch (SQLException e) {
            System.err.println("Error executing REQ-01 query: " + e.getMessage());
        }

        return countries;
    }

    /**
     * REQ-02: Retrieves all countries in a specific continent organised by largest population to smallest.
     *
     * @param continent The name of the continent (e.g., 'Europe', 'Asia').
     * @return List of Country objects in the continent ordered by descending population.
     */
    public ArrayList<Country> getCountriesByContinent(String continent) {
        ArrayList<Country> countries = new ArrayList<>();

        // Validate input parameter
        if (continent == null || continent.trim().isEmpty()) {
            System.err.println("Continent parameter cannot be null or empty.");
            return countries;
        }

        if (con == null) {
            System.err.println("Database connection is not established.");
            return countries;
        }

        String sql = "SELECT c.Code, c.Name, c.Continent, c.Region, c.SurfaceArea, c.Population, ct.Name AS Capital " +
                "FROM country c " +
                "LEFT JOIN city ct ON c.Capital = ct.ID " +
                "WHERE c.Continent = ? " +
                "ORDER BY c.Population DESC";

        try {
            // Using PreparedStatement to prevent SQL injection
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, continent);
            ResultSet rset = pstmt.executeQuery();

            while (rset.next()) {
                Country country = new Country();
                country.setCode(rset.getString("Code"));
                country.setName(rset.getString("Name"));
                country.setContinent(rset.getString("Continent"));
                country.setRegion(rset.getString("Region"));
                country.setSurfaceArea(rset.getDouble("SurfaceArea"));
                country.setPopulation(rset.getInt("Population"));

                String capitalName = rset.getString("Capital");
                if (capitalName == null) {
                    capitalName = "N/A";
                }
                country.setCapital(capitalName);

                countries.add(country);
            }
        } catch (SQLException e) {
            System.err.println("Error executing REQ-02 query: " + e.getMessage());
        }

        return countries;
    }

    /**
     * REQ-03: Retrieves all countries in a specific region organised by largest population to smallest.
     *
     * @param region The name of the region (e.g., 'Western Europe', 'Caribbean').
     * @return List of Country objects in the region ordered by descending population.
     */
    public ArrayList<Country> getCountriesByRegion(String region) {
        ArrayList<Country> countries = new ArrayList<>();

        // Validate input parameter
        if (region == null || region.trim().isEmpty()) {
            System.err.println("Region parameter cannot be null or empty.");
            return countries;
        }

        if (con == null) {
            System.err.println("Database connection is not established.");
            return countries;
        }

        String sql = "SELECT c.Code, c.Name, c.Continent, c.Region, c.SurfaceArea, c.Population, ct.Name AS Capital " +
                "FROM country c " +
                "LEFT JOIN city ct ON c.Capital = ct.ID " +
                "WHERE c.Region = ? " +
                "ORDER BY c.Population DESC";

        try {
            // Using PreparedStatement for safe filtering
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, region);
            ResultSet rset = pstmt.executeQuery();

            while (rset.next()) {
                Country country = new Country();
                country.setCode(rset.getString("Code"));
                country.setName(rset.getString("Name"));
                country.setContinent(rset.getString("Continent"));
                country.setRegion(rset.getString("Region"));
                country.setSurfaceArea(rset.getDouble("SurfaceArea"));
                country.setPopulation(rset.getInt("Population"));

                String capitalName = rset.getString("Capital");
                if (capitalName == null) {
                    capitalName = "N/A";
                }
                country.setCapital(capitalName);

                countries.add(country);
            }
        } catch (SQLException e) {
            System.err.println("Error executing REQ-03 query: " + e.getMessage());
        }

        return countries;
    }

    /**
     * REQ-04: Retrieves the top N populated countries in the world where N is provided by the user.
     *
     * @param n The number of countries to retrieve. Must be greater than 0.
     * @return List containing up to N Country objects ordered by descending population.
     */
    public ArrayList<Country> getTopNCountriesWorld(int n) {
        ArrayList<Country> countries = new ArrayList<>();

        // Validate that N is a positive integer
        if (n <= 0) {
            System.err.println("Invalid value for N: " + n + ". N must be greater than zero.");
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
            // Parameterized LIMIT clause
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, n);
            ResultSet rset = pstmt.executeQuery();

            while (rset.next()) {
                Country country = new Country();
                country.setCode(rset.getString("Code"));
                country.setName(rset.getString("Name"));
                country.setContinent(rset.getString("Continent"));
                country.setRegion(rset.getString("Region"));
                country.setSurfaceArea(rset.getDouble("SurfaceArea"));
                country.setPopulation(rset.getInt("Population"));

                String capitalName = rset.getString("Capital");
                if (capitalName == null) {
                    capitalName = "N/A";
                }
                country.setCapital(capitalName);

                countries.add(country);
            }
        } catch (SQLException e) {
            System.err.println("Error executing REQ-04 query: " + e.getMessage());
        }

        return countries;
    }

    /**
     * Prints a formatted table of Country records to the console.
     *
     * @param countries   The list of countries to display.
     * @param reportTitle The header title for the printed report.
     */
    public void printCountries(ArrayList<Country> countries, String reportTitle) {
        // Defensive check for null or empty list
        if (countries == null || countries.isEmpty()) {
            System.out.println("No countries found for report: " + reportTitle);
            return;
        }

        // Print table header
        System.out.println("\n=================================================================================================================================");
        System.out.println(" REPORT: " + reportTitle);
        System.out.println("=================================================================================================================================");
        System.out.printf("%-6s %-40s %-20s %-30s %-15s %-25s%n", "Code", "Name", "Continent", "Region", "Population", "Capital");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

        // Print each country row
        for (Country country : countries) {
            System.out.printf("%-6s %-40s %-20s %-30s %-15d %-25s%n",
                    country.getCode(),
                    country.getName(),
                    country.getContinent(),
                    country.getRegion(),
                    country.getPopulation(),
                    country.getCapital());
        }

        // Print footer summary
        System.out.println("Total records displayed: " + countries.size() + "\n");
    }

    /**
     * Application entry point.
     *
     * @param args Optional command-line arguments. First argument can override host:port location.
     */
    public static void main(String[] args) {
        // Create new Application instance
        App app = new App();

        // Configure connection target from environment or defaults
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "3307");
        String location = host + ":" + port;

        // Command-line argument override (useful when running inside docker or CI runner)
        if (args.length > 0) {
            location = args[0];
        }

        // Connect to database (10 attempts, 3000ms delay between attempts)
        app.connect(location, 3000);

        // Verification smoke test
        Country testCountry = app.getCountry("ESP");
        if (testCountry != null) {
            System.out.println("Smoke Test Successful! -> Code: " + testCountry.getCode() +
                    ", Name: " + testCountry.getName() +
                    ", Continent: " + testCountry.getContinent() +
                    ", Population: " + testCountry.getPopulation());
        } else {
            System.out.println("Smoke test: ESP not found. Verifying database population...");
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
        Requirement5 req5 = new Requirement5(app.getConnection());

        // Test with Continent = "Europe" and N = 5
        ArrayList<Country> topEurope = req5.getTopNCountriesInContinent("Europe", 5);
        req5.printCountries(topEurope, "Europe", 5);

        // Test with Continent = "Asia" and N = 3
        ArrayList<Country> topAsia = req5.getTopNCountriesInContinent("Asia", 3);
        req5.printCountries(topAsia, "Asia", 3);

        // ==========================================
        // TEST: Requirement 6
        // ==========================================
        System.out.println("\n--- Testing Requirement 6 ---");
        Requirement6 req6 = new Requirement6(app.getConnection());

        String region = "Caribbean";
        int n = 5;

        ArrayList<Country> topCountries = req6.getTopCountriesInRegion(region, n);
        req6.printReport(topCountries, region, n);

        // Disconnect cleanly
        app.disconnect();
    }
}