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
 * Unit tests for REQ-24: Population Living and Not Living in Cities by Region.
 */
public class Requirement24Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getRegionPopulationBreakdownNullConnectionTest() {
        Requirement24 req24 = new Requirement24(null);
        ArrayList<String> result = req24.getRegionPopulationBreakdown();
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that printReport handles null lists gracefully.
     */
    @Test
    public void printReportNullListTest() {
        Requirement24 req24 = new Requirement24(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req24.printReport(null);
            assertTrue(output.toString().contains("No region population breakdown data available."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printReport handles empty lists gracefully.
     */
    @Test
    public void printReportEmptyListTest() {
        Requirement24 req24 = new Requirement24(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req24.printReport(new ArrayList<>());
            assertTrue(output.toString().contains("No region population breakdown data available."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printReport formats valid breakdown rows properly.
     */
    @Test
    public void printReportValidListTest() {
        Requirement24 req24 = new Requirement24(null);
        ArrayList<String> rows = new ArrayList<>();
        rows.add("Western Europe                      183,247,600         45,683,298 (24.93%)        137,564,302 (75.07%)");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req24.printReport(rows);
            String printed = output.toString();
            assertTrue(printed.contains("Western Europe"));
            assertTrue(printed.contains("183,247,600"));
            assertTrue(printed.contains("Total regions displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that a successful database query returns
     * the correct regional population breakdown.
     *
     * The test checks total population, city population,
     * non-city population and calculated percentages.
     */
    @Test
    public void getRegionPopulationBreakdownSuccessTest()
            throws SQLException {

        // Create mocked JDBC objects.
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        // Configure the database operations.
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        // Simulate one region record.
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("Name")).thenReturn("Western Europe");

        when(resultSet.getLong("TotalPopulation"))
                .thenReturn(183247600L);

        when(resultSet.getLong("CityPopulation"))
                .thenReturn(45683298L);

        Requirement24 req24 = new Requirement24(connection);

        ArrayList<String> result =
                req24.getRegionPopulationBreakdown();

        // Verify one region was returned.
        assertEquals(1, result.size());

        String row = result.get(0);

        // Verify the population values.
        assertTrue(row.contains("Western Europe"));
        assertTrue(row.contains("183,247,600"));
        assertTrue(row.contains("45,683,298"));
        assertTrue(row.contains("137,564,302"));

        // Verify the calculated city population percentage.
        assertTrue(row.contains(
                String.format(
                        Locale.getDefault(),
                        "%.2f%%",
                        45683298L * 100.0 / 183247600L
                )
        ));

        // Verify the calculated non-city population percentage.
        assertTrue(row.contains(
                String.format(
                        Locale.getDefault(),
                        "%.2f%%",
                        137564302L * 100.0 / 183247600L
                )
        ));

        // Verify the database query was executed.
        verify(statement).executeQuery(anyString());
    }

    /**
     * Verifies that multiple region records are processed
     * and returned in the order supplied by the database.
     */
    @Test
    public void getRegionPopulationBreakdownMultipleRegionsTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        // Simulate three region records.
        when(resultSet.next())
                .thenReturn(true, true, true, false);

        when(resultSet.getString("Name"))
                .thenReturn(
                        "Eastern Asia",
                        "Southern Asia",
                        "Western Europe"
                );

        when(resultSet.getLong("TotalPopulation"))
                .thenReturn(
                        1500000000L,
                        1200000000L,
                        183247600L
                );

        when(resultSet.getLong("CityPopulation"))
                .thenReturn(
                        600000000L,
                        400000000L,
                        45683298L
                );

        Requirement24 req24 = new Requirement24(connection);

        ArrayList<String> result =
                req24.getRegionPopulationBreakdown();

        // Verify all three regions were processed.
        assertEquals(3, result.size());

        // Verify region order.
        assertTrue(result.get(0).contains("Eastern Asia"));
        assertTrue(result.get(1).contains("Southern Asia"));
        assertTrue(result.get(2).contains("Western Europe"));

        // Verify formatted population values.
        assertTrue(result.get(0).contains("1,500,000,000"));
        assertTrue(result.get(1).contains("1,200,000,000"));
        assertTrue(result.get(2).contains("183,247,600"));
    }

    /**
     * Verifies that a query returning no region records
     * produces an empty population breakdown.
     */
    @Test
    public void getRegionPopulationBreakdownNoResultsTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        // Simulate an empty result set.
        when(resultSet.next()).thenReturn(false);

        Requirement24 req24 = new Requirement24(connection);

        ArrayList<String> result =
                req24.getRegionPopulationBreakdown();

        assertTrue(
                result.isEmpty(),
                "Expected an empty list when no region records exist"
        );
    }

    /**
     * Verifies that regions with zero population
     * do not cause division-by-zero errors.
     *
     * Both city and non-city percentages should be zero.
     */
    @Test
    public void getRegionPopulationBreakdownZeroPopulationTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("Name"))
                .thenReturn("Empty Region");

        when(resultSet.getLong("TotalPopulation"))
                .thenReturn(0L);

        when(resultSet.getLong("CityPopulation"))
                .thenReturn(0L);

        Requirement24 req24 = new Requirement24(connection);

        ArrayList<String> result =
                req24.getRegionPopulationBreakdown();

        assertEquals(1, result.size());

        String row = result.get(0);

        assertTrue(row.contains("Empty Region"));

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
    public void getRegionPopulationBreakdownCityExceedsTotalTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("Name"))
                .thenReturn("Test Region");

        when(resultSet.getLong("TotalPopulation"))
                .thenReturn(1000L);

        when(resultSet.getLong("CityPopulation"))
                .thenReturn(1500L);

        Requirement24 req24 = new Requirement24(connection);

        ArrayList<String> result =
                req24.getRegionPopulationBreakdown();

        assertEquals(1, result.size());

        String row = result.get(0);

        assertTrue(row.contains("Test Region"));

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
    public void getRegionPopulationBreakdownStatementExceptionTest()
            throws SQLException {

        Connection connection = mock(Connection.class);

        // Simulate failure while creating the SQL statement.
        when(connection.createStatement())
                .thenThrow(
                        new SQLException("Statement creation failed")
                );

        Requirement24 req24 = new Requirement24(connection);

        ArrayList<String> result =
                req24.getRegionPopulationBreakdown();

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
    public void getRegionPopulationBreakdownQueryExceptionTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);

        when(connection.createStatement()).thenReturn(statement);

        // Simulate a database query failure.
        when(statement.executeQuery(anyString()))
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement24 req24 = new Requirement24(connection);

        ArrayList<String> result =
                req24.getRegionPopulationBreakdown();

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
    public void getRegionPopulationBreakdownResultSetExceptionTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        // Simulate an error while reading database results.
        when(resultSet.next())
                .thenThrow(
                        new SQLException("ResultSet processing failed")
                );

        Requirement24 req24 = new Requirement24(connection);

        ArrayList<String> result =
                req24.getRegionPopulationBreakdown();

        assertTrue(
                result.isEmpty(),
                "Expected an empty list when ResultSet processing fails"
        );
    }

    /**
     * Verifies that the SQL query aggregates city populations,
     * includes regions containing countries without matching cities,
     * and groups results by region.
     */
    @Test
    public void getRegionPopulationBreakdownSQLQueryTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Requirement24 req24 = new Requirement24(connection);

        req24.getRegionPopulationBreakdown();

        // Capture the SQL query supplied to the database.
        org.mockito.ArgumentCaptor<String> sqlCaptor =
                org.mockito.ArgumentCaptor.forClass(String.class);

        verify(statement)
                .executeQuery(sqlCaptor.capture());

        String sql = sqlCaptor.getValue();

        // Verify the expected SQL structure.
        assertTrue(sql.contains("c.Region AS Name"));
        assertTrue(sql.contains("SUM(c.Population)"));
        assertTrue(sql.contains("FROM country c"));
        assertTrue(sql.contains("LEFT JOIN"));
        assertTrue(sql.contains("SUM(Population)"));
        assertTrue(sql.contains("GROUP BY CountryCode"));
        assertTrue(sql.contains("GROUP BY c.Region"));
        assertTrue(sql.contains("COALESCE"));
        assertTrue(sql.contains("ORDER BY TotalPopulation DESC"));
    }

    /**
     * Verifies that a region without recorded city population
     * is reported with zero city population and 100% non-city population.
     */
    @Test
    public void getRegionPopulationBreakdownNoCitiesTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("Name"))
                .thenReturn("Rural Region");

        when(resultSet.getLong("TotalPopulation"))
                .thenReturn(1000L);

        when(resultSet.getLong("CityPopulation"))
                .thenReturn(0L);

        Requirement24 req24 = new Requirement24(connection);

        ArrayList<String> result =
                req24.getRegionPopulationBreakdown();

        assertEquals(1, result.size());

        String row = result.get(0);

        assertTrue(row.contains("Rural Region"));
        assertTrue(row.contains("0 (0.00%)"));
        assertTrue(row.contains("1,000 (100.00%)"));
    }

    /**
     * Verifies that printReport displays multiple rows
     * and reports the correct total number of regions.
     */
    @Test
    public void printReportMultipleRegionsTest() {

        Requirement24 req24 = new Requirement24(null);

        ArrayList<String> rows = new ArrayList<>();

        rows.add("Eastern Asia 1,500,000,000");
        rows.add("Southern Asia 1,200,000,000");
        rows.add("Western Europe 183,247,600");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req24.printReport(rows);

            String printed = output.toString();

            // Verify the report heading and column labels.
            assertTrue(printed.contains(
                    "REQ-24: Population Living and Not Living in Cities by Region"
            ));

            assertTrue(printed.contains("Total Population"));
            assertTrue(printed.contains("In Cities (%)"));
            assertTrue(printed.contains("Not in Cities (%)"));

            // Verify all three regions are displayed.
            assertTrue(printed.contains("Eastern Asia"));
            assertTrue(printed.contains("Southern Asia"));
            assertTrue(printed.contains("Western Europe"));

            // Verify the total number of displayed regions.
            assertTrue(printed.contains("Total regions displayed: 3"));

        } finally {
            System.setOut(originalOut);
        }
    }
}