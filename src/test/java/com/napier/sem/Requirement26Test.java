
package com.napier.sem;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * JUnit tests for REQ-26: Population of the World.
 *
 * This test class verifies that Requirement26:
 *
 * 1. Handles a missing database connection correctly.
 * 2. Retrieves the expected world population from MySQL.
 *
 * The integration test uses the MySQL world database
 * and obtains database credentials from environment
 * variables rather than hard-coded passwords.
 */
public class Requirement26Test {

    /**
     * Tests how Requirement26 handles a missing
     * database connection.
     *
     * The method should return -1 when no database
     * connection has been provided.
     *
     * This test does not require a running database.
     */
    @Test
    void testWithoutDatabaseConnection() {

        // Create the requirement with no database connection.
        Requirement26 report = new Requirement26(null);

        // Verify that the application returns the
        // expected failure value instead of crashing.
        assertEquals(
                -1L,
                report.getWorldPopulation(),
                "Expected -1 when no database connection exists."
        );
    }

    /**
     * Integration test for REQ-26.
     *
     * Connects to the MySQL world database and checks
     * whether the sum of country populations matches
     * the expected population in the project dataset.
     *
     * The test is skipped when database credentials
     * are not available.
     */
    @Test
    void testWorldPopulationIntegration() {

        // Read database credentials from environment variables.
        // This avoids storing passwords in source code.
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        // Skip the integration test when the required
        // database credentials have not been configured.
        Assumptions.assumeTrue(
                dbUser != null && !dbUser.isBlank()
                        && dbPassword != null && !dbPassword.isBlank(),
                "Database credentials not configured."
        );

        // Read connection settings, using the project's
        // local database configuration as the default.
        String host = System.getenv()
                .getOrDefault("DB_HOST", "localhost");

        String port = System.getenv()
                .getOrDefault("DB_PORT", "3307");

        String database = System.getenv()
                .getOrDefault("DB_NAME", "world");

        // Construct the JDBC connection URL.
        String url = "jdbc:mysql://" + host + ":"
                + port + "/" + database
                + "?allowPublicKeyRetrieval=true&useSSL=false";

        // Open the database connection.
        // Try-with-resources closes it after testing.
        try (Connection connection =
                     DriverManager.getConnection(
                             url, dbUser, dbPassword)) {

            // Pass the existing connection to Requirement26.
            Requirement26 report =
                    new Requirement26(connection);

            // Verify the total world population.
            // A long value is needed because the result
            // exceeds the maximum Java int value.
            assertEquals(
                    6078749450L,
                    report.getWorldPopulation(),
                    "World population must match the project database."
            );

        } catch (SQLException e) {

            // Fail the test if database connectivity
            // or SQL execution encounters an error.
            fail(
                    "Database integration test failed: "
                            + e.getMessage()
            );
        }
    }
}
