
package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * REQ-26: Population of the World.
 *
 * Retrieves the total population of the world from
 * the MySQL world database.
 *
 * This class is responsible for implementing Requirement 26
 * independently of other population report requirements.
 *
 * The database connection is provided by App.java,
 * allowing the application to manage database connections
 * centrally.
 */
public class Requirement26 {

    /**
     * Existing database connection provided by the application.
     *
     * The final keyword prevents the connection reference
     * from being reassigned after construction.
     */
    private final Connection con;

    /**
     * Constructor receives the existing database connection.
     *
     * @param con Active MySQL database connection.
     */
    public Requirement26(Connection con) {
        this.con = con;
    }

    /**
     * Calculates the total population of the world.
     *
     * The query uses SQL SUM() to add the population
     * of every country in the world database.
     *
     * @return Total world population, or -1 if the
     *         database connection or query fails.
     */
    public long getWorldPopulation() {

        // Verify that the application has provided
        // a database connection before executing SQL.
        if (con == null) {
            System.err.println(
                    "Database connection is not established."
            );
            return -1;
        }

        // REQ-26: Sum the population of all countries.
        // The alias WorldPopulation identifies the
        // calculated value in the SQL result.
        String query =
                "SELECT SUM(Population) AS WorldPopulation "
                        + "FROM country";

        // PreparedStatement executes the SQL query.
        // Try-with-resources closes database resources
        // automatically after execution.
        try (PreparedStatement pstmt =
                     con.prepareStatement(query);

             ResultSet rset = pstmt.executeQuery()) {

            // The SUM query normally returns one result row.
            if (rset.next()) {

                // Read the calculated population as long
                // because the total exceeds the int range.
                long population =
                        rset.getLong("WorldPopulation");

                // SQL SUM may return NULL if no population
                // values are available in the database.
                if (!rset.wasNull()) {
                    return population;
                }
            }

        } catch (SQLException e) {

            // Report SQL errors without terminating
            // the entire application.
            System.err.println(
                    "Failed to retrieve world population: "
                            + e.getMessage()
            );
        }

        // Return -1 to indicate that the population
        // could not be retrieved successfully.
        return -1;
    }

    /**
     * Displays the total population of the world.
     *
     * Calls getWorldPopulation() to retrieve the
     * population before displaying the result.
     *
     * Population values are formatted with commas
     * to improve readability.
     */
    public void printWorldPopulation() {

        // Retrieve the population using REQ-26 logic.
        long population = getWorldPopulation();

        // Display an informative message if the
        // population could not be retrieved.
        if (population == -1) {
            System.out.println(
                    "World population unavailable."
            );
            return;
        }

        // Print the world population with thousands
        // separators for easier reading.
        System.out.printf(
                "Population of the World: %,d%n",
                population
        );
    }
}
