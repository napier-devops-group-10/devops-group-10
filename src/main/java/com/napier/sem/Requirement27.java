
package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * REQ-27: Population of a Continent.
 *
 * Retrieves the total population of a specified continent
 * from the MySQL world database.
 *
 * Each country's population is added together to calculate
 * the total population of the selected continent.
 *
 * The database connection is provided by App.java,
 * allowing connection management to remain centralised.
 *
 * This class implements Requirement 27 independently
 * of other population reporting requirements.
 */
public class Requirement27 {

    /**
     * Existing MySQL connection supplied by the application.
     *
     * The final keyword prevents the connection reference
     * from being reassigned after construction.
     */
    private final Connection con;

    /**
     * Creates a new Requirement27 instance.
     *
     * @param con Active MySQL database connection.
     */
    public Requirement27(Connection con) {
        this.con = con;
    }

    /**
     * Calculates the total population of a continent.
     *
     * The SQL query uses SUM() to combine the population
     * values of all countries belonging to the selected
     * continent.
     *
     * @param continent Name of the continent to query.
     * @return Total continent population, or -1 if
     *         the connection, input or query is invalid.
     */
    public long getContinentPopulation(String continent) {

        // Check whether a database connection was supplied.
        // A missing connection prevents SQL execution.
        if (con == null) {
            System.err.println(
                    "Database connection is not established."
            );
            return -1;
        }

        // Reject null, empty or whitespace-only values
        // because they cannot identify a continent.
        if (continent == null || continent.trim().isEmpty()) {
            System.err.println("Invalid continent name.");
            return -1;
        }

        // REQ-27: Calculate the combined population of
        // countries belonging to the specified continent.
        //
        // The question mark is a parameter placeholder,
        // avoiding direct string concatenation into SQL.
        String query =
                "SELECT SUM(Population) AS ContinentPopulation "
                        + "FROM country WHERE Continent = ?";

        // PreparedStatement manages the parameterised query.
        // Try-with-resources closes the statement automatically.
        try (PreparedStatement pstmt =
                     con.prepareStatement(query)) {

            // Remove surrounding spaces before searching.
            pstmt.setString(1, continent.trim());

            // Execute the query and automatically close
            // the result set when processing finishes.
            try (ResultSet rset = pstmt.executeQuery()) {

                if (rset.next()) {

                    // Use long because continent populations
                    // may exceed the maximum Java int value.
                    long population =
                            rset.getLong("ContinentPopulation");

                    // SQL SUM returns NULL when there are no
                    // matching population records.
                    if (!rset.wasNull()) {
                        return population;
                    }
                }
            }

        } catch (SQLException e) {

            // Report database errors without terminating
            // the entire application.
            System.err.println(
                    "Failed to retrieve continent population: "
                            + e.getMessage()
            );
        }

        // Return -1 when a valid population result
        // could not be obtained.
        return -1;
    }

    /**
     * Displays the population of the selected continent.
     *
     * Calls getContinentPopulation() to retrieve the
     * calculated result from the database.
     *
     * @param continent Name of the continent to display.
     */
    public void printContinentPopulation(String continent) {

        // Retrieve the continent population.
        long population = getContinentPopulation(continent);

        // Display an informative message when the
        // population could not be retrieved.
        if (population == -1) {
            System.out.println(
                    "Population unavailable for: " + continent
            );
            return;
        }

        // Print the result using comma separators
        // so large population values are easier to read.
        System.out.printf(
                "Population of %s: %,d%n",
                continent,
                population
        );
    }
}
