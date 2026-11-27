package com.napier.sem;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for REQ-23: Population Living and Not Living in Cities by Continent.
 */
public class Requirement23Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getContinentPopulationBreakdownNullConnectionTest() {

        Requirement23 req23 = new Requirement23(null);

        ArrayList<String> result =
                req23.getContinentPopulationBreakdown();

        assertTrue(
                result.isEmpty(),
                "Expected empty list when database connection is null"
        );
    }

    /**
     * Verifies that printReport handles null lists gracefully.
     */
    @Test
    public void printReportNullListTest() {

        Requirement23 req23 = new Requirement23(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req23.printReport(null);

            assertTrue(
                    output.toString().contains(
                            "No continent population breakdown data available."
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printReport handles empty lists gracefully.
     */
    @Test
    public void printReportEmptyListTest() {

        Requirement23 req23 = new Requirement23(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req23.printReport(new ArrayList<>());

            assertTrue(
                    output.toString().contains(
                            "No continent population breakdown data available."
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printReport formats valid breakdown rows properly.
     */
    @Test
    public void printReportValidListTest() {

        Requirement23 req23 = new Requirement23(null);

        ArrayList<String> rows = new ArrayList<>();

        rows.add(
                "Europe                      730,074,600        241,942,813 (33.14%)        488,131,787 (66.86%)"
        );

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req23.printReport(rows);

            String printed = output.toString();

            assertTrue(printed.contains("Europe"));
            assertTrue(printed.contains("730,074,600"));
            assertTrue(printed.contains("Total continents displayed: 1"));

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that a successful database query returns
     * the correct continent population breakdown.
     *
     * The test checks total population, city population,
     * non-city population and calculated percentages.
     */
    @Test
    public void getContinentPopulationBreakdownSuccessTest()
            throws SQLException {

        // Create mocked JDBC objects.
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        // Configure the database operations.
        when(connection.createStatement())
                .thenReturn(statement);

        when(statement.executeQuery(anyString()))
                .thenReturn(resultSet);

        // Simulate one continent record.
        when(resultSet.next())
                .thenReturn(true, false);

        when(resultSet.getString("Name"))
                .thenReturn("Europe");

        when(resultSet.getLong("TotalPopulation"))
                .thenReturn(730074600L);

        when(resultSet.getLong("CityPopulation"))
                .thenReturn(241942813L);

        Requirement23 req23 = new Requirement23(connection);

        ArrayList<String> result =
                req23.getContinentPopulationBreakdown();

        // Verify one continent was returned.
        assertEquals(1, result.size());

        String row = result.get(0);

        // Verify the population values.
        assertTrue(row.contains("Europe"));
        assertTrue(row.contains("730,074,600"));
        assertTrue(row.contains("241,942,813"));
        assertTrue(row.contains("488,131,787"));

        // Verify the calculated city population percentage.
        assertTrue(
                row.contains(
                        String.format(
                                Locale.getDefault(),
                                "%.2f%%",
                                241942813L * 100.0 / 730074600L
                        )
                )
        );

        // Verify the calculated non-city population percentage.
        assertTrue(
                row.contains(
                        String.format(
                                Locale.getDefault(),
                                "%.2f%%",
                                488131787L * 100.0 / 730074600L
                        )
                )
        );

        // Verify the database query was executed.
        verify(statement).executeQuery(anyString());
    }

    /**
     * Verifies that multiple continent records are processed
     * and returned in the order supplied by the database.
     */
    @Test
    public void getContinentPopulationBreakdownMultipleContinentsTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement())
                .thenReturn(statement);

        when(statement.executeQuery(anyString()))
                .thenReturn(resultSet);

        // Simulate three continent records.
        when(resultSet.next())
                .thenReturn(true, true, true, false);

        when(resultSet.getString("Name"))
                .thenReturn(
                        "Asia",
                        "Africa",
                        "Europe"
                );

        when(resultSet.getLong("TotalPopulation"))
                .thenReturn(
                        3705025700L,
                        784475000L,
                        730074600L
                );

        when(resultSet.getLong("CityPopulation"))
                .thenReturn(
                        1200000000L,
                        250000000L,
                        241942813L
                );

        Requirement23 req23 = new Requirement23(connection);

        ArrayList<String> result =
                req23.getContinentPopulationBreakdown();

        // Verify all three continents were processed.
        assertEquals(3, result.size());

        // Verify continent order.
        assertTrue(result.get(0).contains("Asia"));
        assertTrue(result.get(1).contains("Africa"));
        assertTrue(result.get(2).contains("Europe"));

        // Verify formatted population values.
        assertTrue(result.get(0).contains("3,705,025,700"));
        assertTrue(result.get(1).contains("784,475,000"));
        assertTrue(result.get(2).contains("730,074,600"));
    }

    /**
     * Verifies that a query returning no continent records
     * produces an empty population breakdown.
     */
    @Test
    public void getContinentPopulationBreakdownNoResultsTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement())
                .thenReturn(statement);

        when(statement.executeQuery(anyString()))
                .thenReturn(resultSet);

        // Simulate an empty result set.
        when(resultSet.next())
                .thenReturn(false);

        Requirement23 req23 = new Requirement23(connection);

        ArrayList<String> result =
                req23.getContinentPopulationBreakdown();

        assertTrue(
                result.isEmpty(),
                "Expected an empty list when no continent records exist"
        );
    }

    /**
     * Verifies that continents with zero population
     * do not cause division-by-zero errors.
     *
     * Both city and non-city percentages should be zero.
     */
    @Test
    public void getContinentPopulationBreakdownZeroPopulationTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement())
                .thenReturn(statement);

        when(statement.executeQuery(anyString()))
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true, false);

        when(resultSet.getString("Name"))
                .thenReturn("Empty Continent");

        when(resultSet.getLong("TotalPopulation"))
                .thenReturn(0L);

        when(resultSet.getLong("CityPopulation"))
                .thenReturn(0L);

        Requirement23 req23 = new Requirement23(connection);

        ArrayList<String> result =
                req23.getContinentPopulationBreakdown();

        assertEquals(1, result.size());

        String row = result.get(0);

        assertTrue(row.contains("Empty Continent"));

        // Both percentages must be zero.
        assertEquals(
                2,
                row.split("0\\.00%", -1).length - 1
        );
    }

    /**
     * Verifies that the calculated non-city population
     * cannot become negative.
     *
     * This tests the Math.max() safeguard when city
     * population exceeds the recorded total population.
     */
    @Test
    public void getContinentPopulationBreakdownCityExceedsTotalTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement())
                .thenReturn(statement);

        when(statement.executeQuery(anyString()))
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true, false);

        when(resultSet.getString("Name"))
                .thenReturn("Test Continent");

        when(resultSet.getLong("TotalPopulation"))
                .thenReturn(1000L);

        when(resultSet.getLong("CityPopulation"))
                .thenReturn(1500L);

        Requirement23 req23 = new Requirement23(connection);

        ArrayList<String> result =
                req23.getContinentPopulationBreakdown();

        assertEquals(1, result.size());

        String row = result.get(0);

        assertTrue(row.contains("Test Continent"));

        // City population is 150% of total population.
        assertTrue(row.contains("150.00%"));

        // Non-city population must be clamped to zero.
        assertTrue(row.contains("0 (0.00%)"));

        assertFalse(row.contains("-500"));
    }

    /**
     * Verifies that SQL exceptions during statement creation
     * are handled safely without terminating the application.
     */
    @Test
    public void getContinentPopulationBreakdownStatementExceptionTest()
            throws SQLException {

        Connection connection = mock(Connection.class);

        // Simulate failure while creating the SQL statement.
        when(connection.createStatement())
                .thenThrow(
                        new SQLException("Statement creation failed")
                );

        Requirement23 req23 = new Requirement23(connection);

        ArrayList<String> result =
                req23.getContinentPopulationBreakdown();

        assertTrue(
                result.isEmpty(),
                "Expected an empty list when statement creation fails"
        );
    }

    /**
     * Verifies that SQL exceptions during query execution
     * return an empty report.
     */
    @Test
    public void getContinentPopulationBreakdownQueryExceptionTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);

        when(connection.createStatement())
                .thenReturn(statement);

        // Simulate a database query failure.
        when(statement.executeQuery(anyString()))
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement23 req23 = new Requirement23(connection);

        ArrayList<String> result =
                req23.getContinentPopulationBreakdown();

        assertTrue(
                result.isEmpty(),
                "Expected an empty list when query execution fails"
        );
    }

    /**
     * Verifies that SQL exceptions during ResultSet processing
     * are handled safely.
     */
    @Test
    public void getContinentPopulationBreakdownResultSetExceptionTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement())
                .thenReturn(statement);

        when(statement.executeQuery(anyString()))
                .thenReturn(resultSet);

        // Simulate an error while reading database results.
        when(resultSet.next())
                .thenThrow(
                        new SQLException("ResultSet processing failed")
                );

        Requirement23 req23 = new Requirement23(connection);

        ArrayList<String> result =
                req23.getContinentPopulationBreakdown();

        assertTrue(
                result.isEmpty(),
                "Expected an empty list when ResultSet processing fails"
        );
    }

    /**
     * Verifies that the SQL query aggregates city populations,
     * includes continents containing countries without matching cities,
     * and groups results by continent.
     */
    @Test
    public void getContinentPopulationBreakdownSQLQueryTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement())
                .thenReturn(statement);

        when(statement.executeQuery(anyString()))
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(false);

        Requirement23 req23 = new Requirement23(connection);

        req23.getContinentPopulationBreakdown();

        // Capture the SQL query supplied to the database.
        org.mockito.ArgumentCaptor<String> sqlCaptor =
                org.mockito.ArgumentCaptor.forClass(String.class);

        verify(statement)
                .executeQuery(sqlCaptor.capture());

        String sql = sqlCaptor.getValue();

        // Verify the expected SQL structure.
        assertTrue(sql.contains("c.Continent AS Name"));
        assertTrue(sql.contains("SUM(c.Population)"));
        assertTrue(sql.contains("FROM country c"));
        assertTrue(sql.contains("LEFT JOIN"));
        assertTrue(sql.contains("SUM(Population)"));
        assertTrue(sql.contains("GROUP BY CountryCode"));
        assertTrue(sql.contains("GROUP BY c.Continent"));
        assertTrue(sql.contains("COALESCE"));
        assertTrue(sql.contains("ORDER BY TotalPopulation DESC"));
    }

    /**
     * Verifies that a continent without recorded city population
     * is reported with zero city population and 100% non-city population.
     */
    @Test
    public void getContinentPopulationBreakdownNoCitiesTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement())
                .thenReturn(statement);

        when(statement.executeQuery(anyString()))
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true, false);

        when(resultSet.getString("Name"))
                .thenReturn("Rural Continent");

        when(resultSet.getLong("TotalPopulation"))
                .thenReturn(1000L);

        when(resultSet.getLong("CityPopulation"))
                .thenReturn(0L);

        Requirement23 req23 = new Requirement23(connection);

        ArrayList<String> result =
                req23.getContinentPopulationBreakdown();

        assertEquals(1, result.size());

        String row = result.get(0);

        assertTrue(row.contains("Rural Continent"));
        assertTrue(row.contains("0 (0.00%)"));
        assertTrue(row.contains("1,000 (100.00%)"));
    }

    /**
     * Verifies that printReport displays multiple rows
     * and reports the correct total number of continents.
     */
    @Test
    public void printReportMultipleContinentsTest() {

        Requirement23 req23 = new Requirement23(null);

        ArrayList<String> rows = new ArrayList<>();

        rows.add("Asia 3,705,025,700");
        rows.add("Africa 784,475,000");
        rows.add("Europe 730,074,600");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req23.printReport(rows);

            String printed = output.toString();

            // Verify the report heading and column labels.
            assertTrue(
                    printed.contains(
                            "REQ-23: Population Living and Not Living in Cities by Continent"
                    )
            );

            assertTrue(printed.contains("Total Population"));
            assertTrue(printed.contains("In Cities (%)"));
            assertTrue(printed.contains("Not in Cities (%)"));

            // Verify all three continents are displayed.
            assertTrue(printed.contains("Asia"));
            assertTrue(printed.contains("Africa"));
            assertTrue(printed.contains("Europe"));

            // Verify the total number of displayed continents.
            assertTrue(
                    printed.contains("Total continents displayed: 3")
            );

        } finally {
            System.setOut(originalOut);
        }
    }
}