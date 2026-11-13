package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * REQ-30: The population of a district.
 */
public class Requirement30 {

    /**
     * Active database connection reused from App.
     */
    private final Connection con;

    /**
     * Constructor receiving the active database connection.
     *
     * @param con Active SQL Connection.
     */
    public Requirement30(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves the total population of a specific district by summing the populations
     * of all cities belonging to that district.
     *
     * @param district The name of the district (e.g., 'Gelderland', 'Baleares').
     * @return Total district population, or -1 if the district is not found, input is invalid, or an error occurs.
     */
    public long getDistrictPopulation(String district) {
        // Validate district input parameter
        if (district == null || district.trim().isEmpty()) {
            System.err.println("District parameter cannot be null or empty.");
            return -1;
        }

        // Validate active database connection
        if (con == null) {
            System.err.println("Database connection is not established.");
            return -1;
        }

        // SQL query summing populations of all cities in the specified district
        String sql = "SELECT SUM(Population) AS TotalPopulation " +
                "FROM city " +
                "WHERE District = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, district.trim());

            try (ResultSet rset = pstmt.executeQuery()) {
                if (rset.next()) {
                    long population = rset.getLong("TotalPopulation");
                    // SUM() returns NULL in SQL when no matching rows are found
                    if (rset.wasNull()) {
                        return -1;
                    }
                    return population;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing REQ-30 query: " + e.getMessage());
        }

        return -1;
    }

    /**
     * Prints the total population of the specified district to standard output.
     *
     * @param district The name of the district to display.
     */
    public void printDistrictPopulation(String district) {
        long population = getDistrictPopulation(district);

        if (population == -1) {
            System.out.println("Population unavailable for district: " + district);
            return;
        }

        System.out.println("\n=================================================================================");
        System.out.println(" REQ-30: Population of District '" + district + "'");
        System.out.println("=================================================================================");
        System.out.printf("Total Population of %s: %,d%n%n", district, population);
    }
}