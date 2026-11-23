package com.napier.sem;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
 *
 * Additional Mockito unit tests verify successful queries,
 * NULL results, missing records, SQL exceptions,
 * and formatted population reports.
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

    /**
     * Verifies that a successful database query returns
     * the expected total world population.
     *
     * Mockito simulates the JDBC connection,
     * prepared statement and result set.
     */
    @Test
    void testValidWorldPopulation() throws SQLException {

        // Create mocked JDBC objects.
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        // Configure the database operations.
        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        when(statement.executeQuery())
                .thenReturn(resultSet);

        // Simulate a successful SQL SUM() result.
        when(resultSet.next())
                .thenReturn(true);

        when(resultSet.getLong("WorldPopulation"))
                .thenReturn(6078749450L);

        when(resultSet.wasNull())
                .thenReturn(false);

        Requirement26 report = new Requirement26(connection);

        long population = report.getWorldPopulation();

        // Verify the population is returned correctly.
        assertEquals(6078749450L, population);

        // Verify that SQL was executed.
        verify(statement).executeQuery();
    }

    /**
     * Verifies that a SQL SUM() result containing NULL
     * returns -1 rather than treating NULL as zero.
     */
    @Test
    void testNullWorldPopulation() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        when(statement.executeQuery())
                .thenReturn(resultSet);

        // Simulate a row containing a NULL population.
        when(resultSet.next())
                .thenReturn(true);

        when(resultSet.getLong("WorldPopulation"))
                .thenReturn(0L);

        when(resultSet.wasNull())
                .thenReturn(true);

        Requirement26 report = new Requirement26(connection);

        assertEquals(
                -1L,
                report.getWorldPopulation(),
                "Expected -1 when SQL SUM() returns NULL."
        );
    }

    /**
     * Verifies that a query returning no rows
     * produces the expected failure value.
     */
    @Test
    void testNoPopulationResults() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        when(statement.executeQuery())
                .thenReturn(resultSet);

        // Simulate an empty database result.
        when(resultSet.next())
                .thenReturn(false);

        Requirement26 report = new Requirement26(connection);

        assertEquals(
                -1L,
                report.getWorldPopulation()
        );
    }

    /**
     * Verifies that SQL exceptions during statement
     * preparation are handled safely.
     */
    @Test
    void testPreparedStatementException() throws SQLException {

        Connection connection = mock(Connection.class);

        // Simulate an error preparing the SQL statement.
        when(connection.prepareStatement(anyString()))
                .thenThrow(
                        new SQLException("Statement preparation failed")
                );

        Requirement26 report = new Requirement26(connection);

        assertEquals(
                -1L,
                report.getWorldPopulation()
        );
    }

    /**
     * Verifies that SQL exceptions during query
     * execution return the expected failure value.
     */
    @Test
    void testQueryExecutionException() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        // Simulate a query execution failure.
        when(statement.executeQuery())
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement26 report = new Requirement26(connection);

        assertEquals(
                -1L,
                report.getWorldPopulation()
        );
    }

    /**
     * Verifies that SQL exceptions while reading
     * the result set are handled safely.
     */
    @Test
    void testResultSetException() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        when(statement.executeQuery())
                .thenReturn(resultSet);

        // Simulate a failure when reading the results.
        when(resultSet.next())
                .thenThrow(
                        new SQLException("ResultSet processing failed")
                );

        Requirement26 report = new Requirement26(connection);

        assertEquals(
                -1L,
                report.getWorldPopulation()
        );
    }

    /**
     * Verifies that a zero population is returned
     * successfully when the SQL result is not NULL.
     */
    @Test
    void testZeroWorldPopulation() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        when(statement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true);

        when(resultSet.getLong("WorldPopulation"))
                .thenReturn(0L);

        when(resultSet.wasNull())
                .thenReturn(false);

        Requirement26 report = new Requirement26(connection);

        assertEquals(
                0L,
                report.getWorldPopulation()
        );
    }

    /**
     * Verifies that the correct SQL query is used
     * to calculate the world's total population.
     */
    @Test
    void testWorldPopulationSQLQuery() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        when(statement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(false);

        Requirement26 report = new Requirement26(connection);

        report.getWorldPopulation();

        // Capture the SQL query supplied to PreparedStatement.
        org.mockito.ArgumentCaptor<String> queryCaptor =
                org.mockito.ArgumentCaptor.forClass(String.class);

        verify(connection)
                .prepareStatement(queryCaptor.capture());

        String sql = queryCaptor.getValue();

        // Verify the expected population calculation.
        assertTrue(sql.contains("SUM(Population)"));
        assertTrue(sql.contains("AS WorldPopulation"));
        assertTrue(sql.contains("FROM country"));
    }

    /**
     * Verifies that a successful world population report
     * prints the population using comma separators.
     */
    @Test
    void testPrintWorldPopulationSuccess() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        when(statement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true);

        when(resultSet.getLong("WorldPopulation"))
                .thenReturn(6078749450L);

        when(resultSet.wasNull())
                .thenReturn(false);

        Requirement26 report = new Requirement26(connection);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            report.printWorldPopulation();

            String printed = output.toString();

            // Verify the successful report output.
            assertTrue(
                    printed.contains("Population of the World:")
            );

            assertTrue(
                    printed.contains("6,078,749,450")
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that an unavailable world population
     * produces an informative message.
     */
    @Test
    void testPrintWorldPopulationUnavailable() {

        Requirement26 report = new Requirement26(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            report.printWorldPopulation();

            String printed = output.toString();

            assertTrue(
                    printed.contains("World population unavailable.")
            );

        } finally {
            System.setOut(originalOut);
        }
    }
}