package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * REQ-31: The population of a city.
 */
public class Requirement31 {

    /**
     * Active database connection reused from App.
     */
    private final Connection con;

    /**
     * Constructor receiving the active database connection.
     *
     * @param con Active SQL Connection.
     */
    public Requirement31(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves the population of a specific city from the database.
     *
     * @param cityName The name of the city (e.g., 'Palma de Mallorca', 'Madrid').
     * @return City population, or -1 if the city is not found, input is invalid, or an error occurs.
     */
    public long getCityPopulation(String cityName) {
        // Validate city name input parameter
        if (cityName == null || cityName.trim().isEmpty()) {
            System.err.println("City parameter cannot be null or empty.");
            return -1;
        }

        // Validate active database connection
        if (con == null) {
            System.err.println("Database connection is not established.");
            return -1;
        }

        // SQL query retrieving the population of the specified city
        String sql = "SELECT Population " +
                "FROM city " +
                "WHERE Name = ? " +
                "ORDER BY Population DESC " +
                "LIMIT 1";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, cityName.trim());

            try (ResultSet rset = pstmt.executeQuery()) {
                if (rset.next()) {
                    return rset.getLong("Population");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing REQ-31 query: " + e.getMessage());
        }

        return -1;
    }

    /**
     * Prints the population of the specified city to standard output.
     *
     * @param cityName The name of the city to display.
     */
    public void printCityPopulation(String cityName) {
        long population = getCityPopulation(cityName);

        if (population == -1) {
            System.out.println("Population unavailable for city: " + cityName);
            return;
        }

        System.out.println("\n=================================================================================");
        System.out.println(" REQ-31: Population of City '" + cityName + "'");
        System.out.println("=================================================================================");
        System.out.printf("Total Population of %s: %,d%n%n", cityName, population);
    }
}