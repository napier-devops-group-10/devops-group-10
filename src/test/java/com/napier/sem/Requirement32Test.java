package com.napier.sem;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for REQ-32: Language Population Report.
 *
 * Tests successful database retrieval, report formatting,
 * empty results, database connection failures, and SQL exceptions.
 *
 * Mockito is used to simulate database operations without
 * requiring an active MySQL connection.
 */
public class Requirement32Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getLanguagePopulationReportNullConnectionTest() {
        Requirement32 req32 = new Requirement32(null);

        ArrayList<String> result = req32.getLanguagePopulationReport();

        assertTrue(result.isEmpty(),
                "Expected empty list when database connection is null");
    }

    /**
     * Verifies that printReport handles null lists gracefully.
     */
    @Test
    public void printReportNullListTest() {
        Requirement32 req32 = new Requirement32(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req32.printReport(null);

            assertTrue(output.toString()
                    .contains("No language population data available."));

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printReport handles empty lists gracefully.
     */
    @Test
    public void printReportEmptyListTest() {
        Requirement32 req32 = new Requirement32(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req32.printReport(new ArrayList<>());

            assertTrue(output.toString()
                    .contains("No language population data available."));

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printReport formats valid language rows properly.
     */
    @Test
    public void printReportValidListTest() {
        Requirement32 req32 = new Requirement32(null);

        ArrayList<String> rows = new ArrayList<>();
        rows.add("Chinese                1,191,843,539              19.61%");
        rows.add("Spanish                  355,029,462               5.84%");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req32.printReport(rows);

            String printed = output.toString();

            assertTrue(printed.contains("Chinese"));
            assertTrue(printed.contains("1,191,843,539"));
            assertTrue(printed.contains("Spanish"));
            assertTrue(printed.contains("Total languages displayed: 2"));

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that a successful database query returns
     * correctly formatted language population information.
     */
    @Test
    public void getLanguagePopulationReportSuccessTest() throws SQLException {

        // Create mocked database objects.
        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        // Configure the database connection and SQL statement.
        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        // Simulate one language record followed by the end of the results.
        when(rset.next()).thenReturn(true, false);
        when(rset.getString("Language")).thenReturn("Chinese");
        when(rset.getLong("Speakers")).thenReturn(1191843539L);
        when(rset.getDouble("WorldPercentage")).thenReturn(19.61);

        Requirement32 req32 = new Requirement32(con);

        ArrayList<String> result = req32.getLanguagePopulationReport();

        // Verify the returned report contains the expected information.
        assertEquals(1, result.size());
        assertTrue(result.get(0).contains("Chinese"));
        assertTrue(result.get(0).contains("1,191,843,539"));
        assertTrue(result.get(0).contains("19.61%"));

        // Verify the SQL query was executed.
        verify(stmt).executeQuery(anyString());
    }

    /**
     * Verifies that multiple language records are processed
     * and returned in the same order as the database results.
     */
    @Test
    public void getLanguagePopulationReportMultipleLanguagesTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        // Simulate three language records.
        when(rset.next()).thenReturn(true, true, true, false);

        when(rset.getString("Language"))
                .thenReturn("Chinese", "English", "Hindi");

        when(rset.getLong("Speakers"))
                .thenReturn(1191843539L, 470000000L, 405000000L);

        when(rset.getDouble("WorldPercentage"))
                .thenReturn(19.61, 7.73, 6.66);

        Requirement32 req32 = new Requirement32(con);

        ArrayList<String> result = req32.getLanguagePopulationReport();

        // Verify all three records were processed.
        assertEquals(3, result.size());

        // Verify the language order is preserved.
        assertTrue(result.get(0).contains("Chinese"));
        assertTrue(result.get(1).contains("English"));
        assertTrue(result.get(2).contains("Hindi"));

        // Verify population formatting.
        assertTrue(result.get(0).contains("1,191,843,539"));
        assertTrue(result.get(1).contains("470,000,000"));
        assertTrue(result.get(2).contains("405,000,000"));
    }

    /**
     * Verifies that an empty database result returns
     * an empty language population report.
     */
    @Test
    public void getLanguagePopulationReportNoResultsTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        // Simulate a query that returns no records.
        when(rset.next()).thenReturn(false);

        Requirement32 req32 = new Requirement32(con);

        ArrayList<String> result = req32.getLanguagePopulationReport();

        assertTrue(result.isEmpty(),
                "Expected an empty report when no records are returned");
    }

    /**
     * Verifies that SQL exceptions during statement creation
     * are handled safely without crashing the application.
     */
    @Test
    public void getLanguagePopulationReportStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        // Simulate failure when creating the SQL statement.
        when(con.createStatement())
                .thenThrow(new SQLException("Statement creation failed"));

        Requirement32 req32 = new Requirement32(con);

        ArrayList<String> result = req32.getLanguagePopulationReport();

        assertTrue(result.isEmpty(),
                "Expected an empty report when statement creation fails");
    }

    /**
     * Verifies that SQL exceptions during query execution
     * are handled safely and return an empty report.
     */
    @Test
    public void getLanguagePopulationReportQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);

        when(con.createStatement()).thenReturn(stmt);

        // Simulate a database error during query execution.
        when(stmt.executeQuery(anyString()))
                .thenThrow(new SQLException("Query execution failed"));

        Requirement32 req32 = new Requirement32(con);

        ArrayList<String> result = req32.getLanguagePopulationReport();

        assertTrue(result.isEmpty(),
                "Expected an empty report when query execution fails");
    }

    /**
     * Verifies that SQL exceptions while reading database
     * results are handled safely.
     */
    @Test
    public void getLanguagePopulationReportResultSetExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        // Simulate an error when reading the result set.
        when(rset.next())
                .thenThrow(new SQLException("ResultSet processing failed"));

        Requirement32 req32 = new Requirement32(con);

        ArrayList<String> result = req32.getLanguagePopulationReport();

        assertTrue(result.isEmpty(),
                "Expected an empty report when result processing fails");
    }

    /**
     * Verifies that the SQL query includes all five required
     * languages and orders the results by speaker population.
     */
    @Test
    public void getLanguagePopulationReportSQLQueryTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);
        when(rset.next()).thenReturn(false);

        Requirement32 req32 = new Requirement32(con);

        req32.getLanguagePopulationReport();

        // Capture the SQL query sent to the database.
        org.mockito.ArgumentCaptor<String> sqlCaptor =
                org.mockito.ArgumentCaptor.forClass(String.class);

        verify(stmt).executeQuery(sqlCaptor.capture());

        String sql = sqlCaptor.getValue();

        // Verify all five required languages are included.
        assertTrue(sql.contains("'Chinese'"));
        assertTrue(sql.contains("'English'"));
        assertTrue(sql.contains("'Hindi'"));
        assertTrue(sql.contains("'Spanish'"));
        assertTrue(sql.contains("'Arabic'"));

        // Verify descending ordering and population calculations.
        assertTrue(sql.contains("ORDER BY Speakers DESC"));
        assertTrue(sql.contains("SUM(c.Population"));
        assertTrue(sql.contains("WorldPercentage"));
    }

    /**
     * Verifies that the report displays the correct number
     * of languages when all five required languages are supplied.
     */
    @Test
    public void printReportFiveLanguagesTest() {

        Requirement32 req32 = new Requirement32(null);

        ArrayList<String> rows = new ArrayList<>();

        rows.add("Chinese 1191843539 19.61%");
        rows.add("English 470000000 7.73%");
        rows.add("Hindi 405000000 6.66%");
        rows.add("Spanish 355029462 5.84%");
        rows.add("Arabic 250000000 4.11%");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req32.printReport(rows);

            String printed = output.toString();

            // Verify the report contains all five languages.
            assertTrue(printed.contains("Chinese"));
            assertTrue(printed.contains("English"));
            assertTrue(printed.contains("Hindi"));
            assertTrue(printed.contains("Spanish"));
            assertTrue(printed.contains("Arabic"));

            // Verify the total number of displayed languages.
            assertTrue(printed.contains("Total languages displayed: 5"));

            // Verify the report heading.
            assertTrue(printed.contains("REQ-32: Language Population Report"));

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that a valid report contains formatted output
     * rather than an unavailable-data message.
     */
    @Test
    public void printReportValidOutputTest() {

        Requirement32 req32 = new Requirement32(null);

        ArrayList<String> rows = new ArrayList<>();
        rows.add("English 470000000 7.73%");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req32.printReport(rows);

            String printed = output.toString();

            assertTrue(printed.contains("Language"));
            assertTrue(printed.contains("Total Speakers"));
            assertTrue(printed.contains("% World Pop."));
            assertFalse(printed.contains("No language population data available."));

        } finally {
            System.setOut(originalOut);
        }
    }
}