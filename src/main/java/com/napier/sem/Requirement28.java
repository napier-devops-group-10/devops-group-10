
package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * REQ-28: Population of a Region.
 *
 * Calculates and displays the total population of a selected
 * region using the MySQL world database.
 */
public class Requirement28 {

    private final Connection connection;

    /**
     * Creates the REQ-28 report using an existing connection.
     *
     * @param connection Active MySQL database connection.
     */
    public Requirement28(Connection connection) {
        this.connection = connection;
    }

    /**
     * Calculates the total population of the selected region.
     *
     * @param region Name of the region.
     * @return Total population, or -1 if the request fails
     *         or the region is invalid or not found.
     */
    public long getRegionPopulation(String region) {

        if (connection == null) {
            System.err.println(
                    "REQ-28: Database connection is not established.");
            return -1;
        }

        if (region == null || region.trim().isEmpty()) {
            System.err.println(
                    "REQ-28: Invalid region name.");
            return -1;
        }

        String sql =
                "SELECT SUM(Population) AS TotalPopulation "
                        + "FROM country WHERE Region = ?";

        try (PreparedStatement pstmt =
                     connection.prepareStatement(sql)) {

            pstmt.setString(1, region.trim());

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    long population =
                            rs.getLong("TotalPopulation");

                    if (!rs.wasNull()) {
                        return population;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "REQ-28: Error retrieving region population: "
                            + e.getMessage());
        }

        return -1;
    }

    /**
     * Displays the total population of the selected region.
     *
     * @param region Name of the region.
     */
    public void printRegionPopulation(String region) {

        long population = getRegionPopulation(region);

        if (population >= 0) {
            System.out.printf(
                    "Population of %s: %,d%n",
                    region.trim(),
                    population);
        } else {
            System.err.println(
                    "REQ-28: Unable to retrieve population for region: "
                            + region);
        }
    }
}
