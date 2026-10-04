package com.napier.sem;

import java.sql.*;

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
     * Test query to retrieve a country by code.
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
            String strSelect = "SELECT Code, Name, Continent, Region, Population FROM country WHERE Code = '" + code + "'";
            ResultSet rset = stmt.executeQuery(strSelect);

            if (rset.next()) {
                Country c = new Country();
                c.code = rset.getString("Code");
                c.name = rset.getString("Name");
                c.continent = rset.getString("Continent");
                c.region = rset.getString("Region");
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

        // Perform test query
        Country country = app.getCountry("ESP");
        if (country != null) {
            System.out.println("Test Query Successful! -> Code: " + country.code +
                    ", Name: " + country.name +
                    ", Continent: " + country.continent +
                    ", Population: " + country.population);
        } else {
            System.out.println("Connection established, but no country data found with code ESP (Check if world.sql is populated).");
        }

        // Disconnect
        app.disconnect();
    }
}