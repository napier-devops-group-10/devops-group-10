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
 * Unit tests for REQ-25: Population Living and Not Living in Cities by Country.
 */
public class Requirement25Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getCountryPopulationBreakdownNullConnectionTest() {
        Requirement25 req25 = new Requirement25(null);
        ArrayList<String> result = req25.getCountryPopulationBreakdown();

        assertTrue(result.isEmpty(),
                "Expected empty list when database connection is null");
    }

    /**
     * Verifies that printReport handles null lists gracefully.
     */
    @Test
    public void printReportNullListTest() {
        Requirement25 req25 = new Requirement25(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req25.printReport(null);

            assertTrue(output.toString()
                    .contains("No country population breakdown data available."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printReport handles empty lists gracefully.
     */
    @Test
    public void printReportEmptyListTest() {
        Requirement25 req25 = new Requirement25(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req25.printReport(new ArrayList<>());

            assertTrue(output.toString()
                    .contains("No country population breakdown data available."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printReport formats valid breakdown rows properly.
     */
    @Test
    public void printReportValidListTest() {
        Requirement25 req25 = new Requirement25(null);

        ArrayList<String> rows = new ArrayList<>();
        rows.add("Spain                                          39,441,700         16,669,189 (42.26%)         22,772,511 (57.74%)");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req25.printReport(rows);

            String printed = output.toString();

            assertTrue(printed.contains("Spain"));
            assertTrue(printed.contains("39,441,700"));
            assertTrue(printed.contains("Total countries displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that a successful database query returns
     * the correct country population breakdown.
     *
     * The test checks total population, city population,
     * non-city population and calculated percentages.
     */
    @Test
    public void getCountryPopulationBreakdownSuccessTest()
            throws SQLException {

        // Create mocked JDBC objects.
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        // Configure the database operations.
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        // Simulate one country record.
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("Name")).thenReturn("Spain");

        when(resultSet.getLong("TotalPopulation"))
                .thenReturn(39441700L);

        when(resultSet.getLong("CityPopulation"))
                .thenReturn(16669189L);

        Requirement25 req25 = new Requirement25(connection);

        ArrayList<String> result =
                req25.getCountryPopulationBreakdown();

        // Verify the returned population information.
        assertEquals(1, result.size());

        String row = result.get(0);

        assertTrue(row.contains("Spain"));
        assertTrue(row.contains("39,441,700"));
        assertTrue(row.contains("16,669,189"));
        assertTrue(row.contains("22,772,511"));

        // Verify percentages using the production calculation.
        assertTrue(row.contains(String.format(Locale.getDefault(),
                "%.2f%%", 16669189L * 100.0 / 39441700L)));

        assertTrue(row.contains(String.format(Locale.getDefault(),
                "%.2f%%", 22772511L * 100.0 / 39441700L)));

        // Verify the query was executed.
        verify(statement).executeQuery(anyString());
    }

    /**
     * Verifies that multiple country records are processed
     * and returned in the same order as the database results.
     */
    @Test
    public void getCountryPopulationBreakdownMultipleCountriesTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        // Simulate three country records.
        when(resultSet.next()).thenReturn(true, true, true, false);

        when(resultSet.getString("Name"))
                .thenReturn("China", "India", "Spain");

        when(resultSet.getLong("TotalPopulation"))
                .thenReturn(1277558000L, 1013662000L, 39441700L);

        when(resultSet.getLong("CityPopulation"))
                .thenReturn(400000000L, 300000000L, 16669189L);

        Requirement25 req25 = new Requirement25(connection);

        ArrayList<String> result =
                req25.getCountryPopulationBreakdown();

        // Verify all records were processed.
        assertEquals(3, result.size());

        // Verify the order is preserved.
        assertTrue(result.get(0).contains("China"));
        assertTrue(result.get(1).contains("India"));
        assertTrue(result.get(2).contains("Spain"));

        // Verify population values are formatted.
        assertTrue(result.get(0).contains("1,277,558,000"));
        assertTrue(result.get(1).contains("1,013,662,000"));
        assertTrue(result.get(2).contains("39,441,700"));
    }

    /**
     * Verifies that a query returning no country records
     * produces an empty population breakdown.
     */
    @Test
    public void getCountryPopulationBreakdownNoResultsTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        // Simulate an empty result set.
        when(resultSet.next()).thenReturn(false);

        Requirement25 req25 = new Requirement25(connection);

        ArrayList<String> result =
                req25.getCountryPopulationBreakdown();

        assertTrue(result.isEmpty(),
                "Expected an empty list when no country records exist");
    }

    /**
     * Verifies that countries with zero population
     * do not cause division-by-zero errors.
     *
     * Both city and non-city percentages should be zero.
     */
    @Test
    public void getCountryPopulationBreakdownZeroPopulationTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("Name")).thenReturn("Empty Country");

        when(resultSet.getLong("TotalPopulation"))
                .thenReturn(0L);

        when(resultSet.getLong("CityPopulation"))
                .thenReturn(0L);

        Requirement25 req25 = new Requirement25(connection);

        ArrayList<String> result =
                req25.getCountryPopulationBreakdown();

        assertEquals(1, result.size());

        String row = result.get(0);

        assertTrue(row.contains("Empty Country"));

        // Both percentages must be zero.
        assertEquals(2, row.split("0\\.00%", -1).length - 1);
    }

    /**
     * Verifies that the calculated non-city population
     * cannot become negative.
     *
     * This tests the Math.max() safeguard when city
     * population exceeds the recorded total population.
     */
    @Test
    public void getCountryPopulationBreakdownCityExceedsTotalTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("Name")).thenReturn("Test Country");

        when(resultSet.getLong("TotalPopulation"))
                .thenReturn(1000L);

        when(resultSet.getLong("CityPopulation"))
                .thenReturn(1500L);

        Requirement25 req25 = new Requirement25(connection);

        ArrayList<String> result =
                req25.getCountryPopulationBreakdown();

        assertEquals(1, result.size());

        String row = result.get(0);

        assertTrue(row.contains("Test Country"));

        // City population is 150% of total.
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
    public void getCountryPopulationBreakdownStatementExceptionTest()
            throws SQLException {

        Connection connection = mock(Connection.class);

        // Simulate failure while creating the SQL statement.
        when(connection.createStatement())
                .thenThrow(new SQLException("Statement creation failed"));

        Requirement25 req25 = new Requirement25(connection);

        ArrayList<String> result =
                req25.getCountryPopulationBreakdown();

        assertTrue(result.isEmpty(),
                "Expected an empty list when statement creation fails");
    }

    /**
     * Verifies that SQL exceptions during query execution
     * return an empty report.
     */
    @Test
    public void getCountryPopulationBreakdownQueryExceptionTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);

        when(connection.createStatement()).thenReturn(statement);

        // Simulate a database query failure.
        when(statement.executeQuery(anyString()))
                .thenThrow(new SQLException("Query execution failed"));

        Requirement25 req25 = new Requirement25(connection);

        ArrayList<String> result =
                req25.getCountryPopulationBreakdown();

        assertTrue(result.isEmpty(),
                "Expected an empty list when query execution fails");
    }

    /**
     * Verifies that SQL exceptions during ResultSet processing
     * are handled safely.
     */
    @Test
    public void getCountryPopulationBreakdownResultSetExceptionTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        // Simulate an error when reading database results.
        when(resultSet.next())
                .thenThrow(new SQLException("ResultSet processing failed"));

        Requirement25 req25 = new Requirement25(connection);

        ArrayList<String> result =
                req25.getCountryPopulationBreakdown();

        assertTrue(result.isEmpty(),
                "Expected an empty list when ResultSet processing fails");
    }

    /**
     * Verifies that the SQL query aggregates city populations,
     * includes countries without matching cities,
     * and orders countries by total population.
     */
    @Test
    public void getCountryPopulationBreakdownSQLQueryTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Requirement25 req25 = new Requirement25(connection);

        req25.getCountryPopulationBreakdown();

        // Capture the SQL query supplied to the database.
        org.mockito.ArgumentCaptor<String> sqlCaptor =
                org.mockito.ArgumentCaptor.forClass(String.class);

        verify(statement).executeQuery(sqlCaptor.capture());

        String sql = sqlCaptor.getValue();

        // Verify the expected SQL structure.
        assertTrue(sql.contains("FROM country c"));
        assertTrue(sql.contains("LEFT JOIN"));
        assertTrue(sql.contains("SUM(Population)"));
        assertTrue(sql.contains("GROUP BY CountryCode"));
        assertTrue(sql.contains("COALESCE"));
        assertTrue(sql.contains("ORDER BY TotalPopulation DESC"));
    }

    /**
     * Verifies that a country without recorded city population
     * is reported with zero city population and 100% non-city population.
     */
    @Test
    public void getCountryPopulationBreakdownNoCitiesTest()
            throws SQLException {

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("Name")).thenReturn("Rural Country");

        when(resultSet.getLong("TotalPopulation"))
                .thenReturn(1000L);

        when(resultSet.getLong("CityPopulation"))
                .thenReturn(0L);

        Requirement25 req25 = new Requirement25(connection);

        ArrayList<String> result =
                req25.getCountryPopulationBreakdown();

        assertEquals(1, result.size());

        String row = result.get(0);

        assertTrue(row.contains("Rural Country"));
        assertTrue(row.contains("0 (0.00%)"));
        assertTrue(row.contains("1,000 (100.00%)"));
    }

    /**
     * Verifies that printReport displays multiple rows
     * and reports the correct total number of countries.
     */
    @Test
    public void printReportMultipleCountriesTest() {

        Requirement25 req25 = new Requirement25(null);

        ArrayList<String> rows = new ArrayList<>();
        rows.add("China 1,277,558,000");
        rows.add("India 1,013,662,000");
        rows.add("Spain 39,441,700");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req25.printReport(rows);

            String printed = output.toString();

            // Verify the report heading and column labels.
            assertTrue(printed.contains("REQ-25: Population Living and Not Living in Cities by Country"));
            assertTrue(printed.contains("Total Population"));
            assertTrue(printed.contains("In Cities (%)"));
            assertTrue(printed.contains("Not in Cities (%)"));

            // Verify all three countries are displayed.
            assertTrue(printed.contains("China"));
            assertTrue(printed.contains("India"));
            assertTrue(printed.contains("Spain"));

            // Verify the total number of displayed countries.
            assertTrue(printed.contains("Total countries displayed: 3"));

        } finally {
            System.setOut(originalOut);
        }
    }
}