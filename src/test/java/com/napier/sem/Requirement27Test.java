
package com.napier.sem;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for REQ-27: Population of a Continent.
 *
 * Verifies the behaviour of Requirement27 when:
 *
 * 1. No database connection is provided.
 * 2. A null continent name is supplied.
 * 3. An empty continent name is supplied.
 * 4. A valid continent is queried in the MySQL world database.
 *
 * Integration testing uses environment variables to avoid
 * storing database credentials directly in source code.
 */
public class Requirement27Test {

    /**
     * REQ-27: Verify behaviour when no database
     * connection is available.
     *
     * Expected result: -1.
     *
     * This test does not require MySQL.
     */
    @Test
    void testWithoutDatabaseConnection() {

        // Create the requirement without a database connection.
        Requirement27 report = new Requirement27(null);

        // Verify that missing connections are handled safely.
        assertEquals(
                -1L,
                report.getContinentPopulation("Asia"),
                "Expected -1 when no database connection exists."
        );
    }

    /**
     * REQ-27: Verify that requesting a null continent
     * without a connection returns the failure value.
     *
     * Input validation with an active connection is
     * additionally checked by the integration test.
     */
    @Test
    void testNullContinent() {

        Requirement27 report = new Requirement27(null);

        assertEquals(
                -1L,
                report.getContinentPopulation(null),
                "Expected -1 for a null continent."
        );
    }

    /**
     * REQ-27: Verify that an empty continent value
     * without a connection is handled safely.
     *
     * The integration test also checks this input
     * while connected to MySQL.
     */
    @Test
    void testEmptyContinent() {

        Requirement27 report = new Requirement27(null);

        assertEquals(
                -1L,
                report.getContinentPopulation(""),
                "Expected -1 for an empty continent."
        );
    }

    /**
     * Integration test for REQ-27.
     *
     * Connects to the MySQL world database and verifies
     * the total population of Asia against the expected
     * value in the project's sample dataset.
     *
     * Also verifies that invalid continent inputs
     * are rejected when a valid connection exists.
     *
     * The test is skipped if database credentials
     * are not configured.
     */
    @Test
    void testAsiaPopulationIntegration() {

        // Retrieve the database credentials securely
        // from the execution environment.
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        // Skip this test if credentials are missing.
        Assumptions.assumeTrue(
                dbUser != null && !dbUser.isBlank()
                        && dbPassword != null && !dbPassword.isBlank(),
                "Database credentials not configured."
        );

        // Retrieve the database host, port and name.
        // Defaults match the Group 10 local Docker setup.
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

        // Open the connection and ensure it is closed
        // automatically after the test completes.
        try (Connection connection =
                     DriverManager.getConnection(
                             url, dbUser, dbPassword)) {

            // Supply the connection to the new requirement class.
            Requirement27 report = new Requirement27(connection);

            // Verify Asia's population using the project's
            // known MySQL world database values.
            assertEquals(
                    3705025700L,
                    report.getContinentPopulation("Asia"),
                    "Asia population must match the world database."
            );

            // Check that null input is rejected even when
            // a valid database connection is available.
            assertEquals(
                    -1L,
                    report.getContinentPopulation(null),
                    "Null continent should be rejected."
            );

            // Check that an empty continent is rejected.
            assertEquals(
                    -1L,
                    report.getContinentPopulation(""),
                    "Empty continent should be rejected."
            );

            // Verify whitespace-only input is rejected.
            assertEquals(
                    -1L,
                    report.getContinentPopulation("   "),
                    "Whitespace-only continent should be rejected."
            );

        } catch (SQLException e) {

            // Fail the integration test when the database
            // cannot be accessed or the SQL operation fails.
            fail(
                    "Database integration test failed: "
                            + e.getMessage()
            );
        }
    }
}
