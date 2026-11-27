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
 *
 * Additional Mockito tests verify successful queries,
 * invalid inputs, missing results, SQL exceptions,
 * and formatted population reports.
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

    /**
     * Verifies that a valid continent returns the correct
     * population when the database query succeeds.
     *
     * Mockito simulates the database connection,
     * prepared statement and result set.
     */
    @Test
    void testValidContinentPopulation() throws SQLException {

        // Create mocked JDBC objects.
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        // Configure the expected database operations.
        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        when(statement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true);

        when(resultSet.getLong("ContinentPopulation"))
                .thenReturn(3705025700L);

        when(resultSet.wasNull())
                .thenReturn(false);

        Requirement27 report = new Requirement27(connection);

        // Verify that the population is returned correctly.
        long population = report.getContinentPopulation("Asia");

        assertEquals(3705025700L, population);

        // Verify the correct continent was supplied.
        verify(statement).setString(1, "Asia");

        // Verify that the database query was executed.
        verify(statement).executeQuery();
    }

    /**
     * Verifies that a null continent is rejected when
     * a database connection is available.
     *
     * This exercises the input validation branch
     * independently of the null connection check.
     */
    @Test
    void testNullContinentWithConnection() throws SQLException {

        Connection connection = mock(Connection.class);

        Requirement27 report = new Requirement27(connection);

        assertEquals(
                -1L,
                report.getContinentPopulation(null)
        );

        // Invalid input must not execute SQL.
        verify(connection, never())
                .prepareStatement(anyString());
    }

    /**
     * Verifies that an empty continent name is rejected
     * before attempting database access.
     */
    @Test
    void testEmptyContinentWithConnection() throws SQLException {

        Connection connection = mock(Connection.class);

        Requirement27 report = new Requirement27(connection);

        assertEquals(
                -1L,
                report.getContinentPopulation("")
        );

        verify(connection, never())
                .prepareStatement(anyString());
    }

    /**
     * Verifies that whitespace-only continent names
     * are rejected as invalid input.
     */
    @Test
    void testWhitespaceContinentWithConnection() throws SQLException {

        Connection connection = mock(Connection.class);

        Requirement27 report = new Requirement27(connection);

        assertEquals(
                -1L,
                report.getContinentPopulation("   ")
        );

        verify(connection, never())
                .prepareStatement(anyString());
    }

    /**
     * Verifies that surrounding whitespace is removed
     * before the continent name is passed to SQL.
     */
    @Test
    void testContinentNameTrimmed() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        when(statement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true);

        when(resultSet.getLong("ContinentPopulation"))
                .thenReturn(3705025700L);

        when(resultSet.wasNull())
                .thenReturn(false);

        Requirement27 report = new Requirement27(connection);

        long population =
                report.getContinentPopulation("  Asia  ");

        assertEquals(3705025700L, population);

        // Verify that whitespace was removed.
        verify(statement).setString(1, "Asia");
    }

    /**
     * Verifies that a NULL population result from
     * SQL SUM() is handled correctly.
     *
     * SQL SUM() can return NULL when no matching
     * country population records exist.
     */
    @Test
    void testNullPopulationResult() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        when(statement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true);

        when(resultSet.getLong("ContinentPopulation"))
                .thenReturn(0L);

        when(resultSet.wasNull())
                .thenReturn(true);

        Requirement27 report = new Requirement27(connection);

        assertEquals(
                -1L,
                report.getContinentPopulation("Unknown")
        );
    }

    /**
     * Verifies that a query returning no result rows
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

        Requirement27 report = new Requirement27(connection);

        assertEquals(
                -1L,
                report.getContinentPopulation("Unknown")
        );
    }

    /**
     * Verifies that SQL exceptions during statement
     * preparation are handled without crashing.
     */
    @Test
    void testPreparedStatementException() throws SQLException {

        Connection connection = mock(Connection.class);

        // Simulate failure while preparing SQL.
        when(connection.prepareStatement(anyString()))
                .thenThrow(
                        new SQLException("Statement preparation failed")
                );

        Requirement27 report = new Requirement27(connection);

        assertEquals(
                -1L,
                report.getContinentPopulation("Asia")
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

        // Simulate failure during SQL execution.
        when(statement.executeQuery())
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement27 report = new Requirement27(connection);

        assertEquals(
                -1L,
                report.getContinentPopulation("Asia")
        );
    }

    /**
     * Verifies that SQL exceptions while reading
     * database results are handled safely.
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

        // Simulate a database error while reading results.
        when(resultSet.next())
                .thenThrow(
                        new SQLException("ResultSet processing failed")
                );

        Requirement27 report = new Requirement27(connection);

        assertEquals(
                -1L,
                report.getContinentPopulation("Asia")
        );
    }

    /**
     * Verifies that a zero population is treated
     * as a valid SQL result when it is not NULL.
     */
    @Test
    void testZeroPopulation() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        when(statement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true);

        when(resultSet.getLong("ContinentPopulation"))
                .thenReturn(0L);

        when(resultSet.wasNull())
                .thenReturn(false);

        Requirement27 report = new Requirement27(connection);

        assertEquals(
                0L,
                report.getContinentPopulation("Asia")
        );
    }

    /**
     * Verifies that the SQL query uses a parameterised
     * PreparedStatement and the expected population calculation.
     */
    @Test
    void testPopulationSQLQuery() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        when(statement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(false);

        Requirement27 report = new Requirement27(connection);

        report.getContinentPopulation("Europe");

        // Capture the SQL query supplied to PreparedStatement.
        org.mockito.ArgumentCaptor<String> queryCaptor =
                org.mockito.ArgumentCaptor.forClass(String.class);

        verify(connection)
                .prepareStatement(queryCaptor.capture());

        String sql = queryCaptor.getValue();

        // Verify the population aggregation and parameter.
        assertTrue(sql.contains("SUM(Population)"));
        assertTrue(sql.contains("FROM country"));
        assertTrue(sql.contains("Continent = ?"));

        verify(statement).setString(1, "Europe");
    }

    /**
     * Verifies that a successful population report
     * prints the continent and formatted population.
     */
    @Test
    void testPrintContinentPopulationSuccess() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        when(statement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true);

        when(resultSet.getLong("ContinentPopulation"))
                .thenReturn(3705025700L);

        when(resultSet.wasNull())
                .thenReturn(false);

        Requirement27 report = new Requirement27(connection);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            report.printContinentPopulation("Asia");

            String printed = output.toString();

            // Verify the successful report output.
            assertTrue(
                    printed.contains("Population of Asia:")
            );

            assertTrue(
                    printed.contains("3,705,025,700")
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that an unavailable population
     * produces an informative message.
     */
    @Test
    void testPrintContinentPopulationUnavailable() {

        Requirement27 report = new Requirement27(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            report.printContinentPopulation("Asia");

            String printed = output.toString();

            assertTrue(
                    printed.contains("Population unavailable for: Asia")
            );

        } finally {
            System.setOut(originalOut);
        }
    }
}