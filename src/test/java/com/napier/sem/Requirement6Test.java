package com.napier.sem;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for REQ-06:
 * Top N Populated Countries in a Region.
 *
 * Tests input validation, database retrieval,
 * SQL exception handling and report formatting.
 */
public class Requirement6Test {

    /**
     * Verifies that a null database connection
     * returns an empty country list.
     */
    @Test
    public void getTopCountriesInRegionNullConnectionTest() {
        Requirement6 req6 = new Requirement6(null);

        assertTrue(req6.getTopCountriesInRegion("Caribbean", 5).isEmpty());
    }

    /**
     * Tests invalid input parameters using a valid
     * mock connection so each validation condition
     * can be evaluated independently.
     */
    @Test
    public void getTopCountriesInRegionInvalidParamsTest() {
        Connection con = mock(Connection.class);
        Requirement6 req6 = new Requirement6(con);

        assertTrue(req6.getTopCountriesInRegion(null, 5).isEmpty());
        assertTrue(req6.getTopCountriesInRegion("", 5).isEmpty());
        assertTrue(req6.getTopCountriesInRegion("   ", 5).isEmpty());
        assertTrue(req6.getTopCountriesInRegion("Caribbean", 0).isEmpty());
        assertTrue(req6.getTopCountriesInRegion("Western Europe", -2).isEmpty());

        // Invalid parameters should prevent SQL execution.
        verifyNoInteractions(con);
    }

    /**
     * Tests successful retrieval of countries
     * from a simulated database result set.
     */
    @Test
    public void getTopCountriesInRegionValidDataTest() throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        // Simulate two countries followed by the end of the results.
        when(rset.next()).thenReturn(true, true, false);

        when(rset.getString("Code")).thenReturn("CUB", "JAM");
        when(rset.getString("Name")).thenReturn("Cuba", "Jamaica");
        when(rset.getString("Continent"))
                .thenReturn("North America", "North America");
        when(rset.getString("Region"))
                .thenReturn("Caribbean", "Caribbean");
        when(rset.getDouble("SurfaceArea"))
                .thenReturn(110861.0, 10990.0);
        when(rset.getInt("Population"))
                .thenReturn(11201000, 2660000);
        when(rset.getString("Capital"))
                .thenReturn("La Habana", "Kingston");

        Requirement6 req6 = new Requirement6(con);

        ArrayList<Country> countries =
                req6.getTopCountriesInRegion("Caribbean", 2);

        assertEquals(2, countries.size());

        // Verify the first country.
        assertEquals("CUB", countries.get(0).getCode());
        assertEquals("Cuba", countries.get(0).getName());
        assertEquals("North America", countries.get(0).getContinent());
        assertEquals("Caribbean", countries.get(0).getRegion());
        assertEquals(110861.0, countries.get(0).getSurfaceArea(), 0.001);
        assertEquals(11201000, countries.get(0).getPopulation());
        assertEquals("La Habana", countries.get(0).getCapital());

        // Verify the second country.
        assertEquals("JAM", countries.get(1).getCode());
        assertEquals("Jamaica", countries.get(1).getName());
        assertEquals(2660000, countries.get(1).getPopulation());
        assertEquals("Kingston", countries.get(1).getCapital());

        // Verify SQL parameter binding.
        verify(pstmt).setString(1, "Caribbean");
        verify(pstmt).setInt(2, 2);
        verify(pstmt).executeQuery();

        // Verify that the PreparedStatement is closed.
        verify(pstmt).close();
    }

    /**
     * Verifies that surrounding whitespace in the
     * region name is removed before SQL execution.
     */
    @Test
    public void getTopCountriesInRegionTrimRegionTest() throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);
        when(rset.next()).thenReturn(false);

        Requirement6 req6 = new Requirement6(con);

        ArrayList<Country> countries =
                req6.getTopCountriesInRegion("  Caribbean  ", 5);

        assertTrue(countries.isEmpty());

        verify(pstmt).setString(1, "Caribbean");
        verify(pstmt).setInt(2, 5);
    }

    /**
     * Verifies that a query returning no records
     * produces an empty list.
     */
    @Test
    public void getTopCountriesInRegionNoResultsTest() throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);
        when(rset.next()).thenReturn(false);

        Requirement6 req6 = new Requirement6(con);

        ArrayList<Country> countries =
                req6.getTopCountriesInRegion("Unknown Region", 5);

        assertTrue(countries.isEmpty());

        verify(pstmt).executeQuery();
    }

    /**
     * Verifies that SQL errors during statement
     * preparation are handled gracefully.
     */
    @Test
    public void getTopCountriesInRegionSQLExceptionTest() throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(new SQLException("Simulated database error"));

        Requirement6 req6 = new Requirement6(con);

        ArrayList<Country> countries =
                req6.getTopCountriesInRegion("Caribbean", 5);

        assertTrue(countries.isEmpty());
    }

    /**
     * Verifies that SQL errors during query execution
     * are handled without crashing the application.
     */
    @Test
    public void getTopCountriesInRegionExecuteQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(new SQLException("Query execution failed"));

        Requirement6 req6 = new Requirement6(con);

        ArrayList<Country> countries =
                req6.getTopCountriesInRegion("Caribbean", 5);

        assertTrue(countries.isEmpty());

        verify(pstmt).close();
    }

    /**
     * Tests report printing when the supplied
     * list is null or empty.
     */
    @Test
    public void printReportEmptyOrNullTest() {

        Requirement6 req6 = new Requirement6(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req6.printReport(null, "Caribbean", 5);
            req6.printReport(new ArrayList<>(), "Caribbean", 5);

            String printed = output.toString();

            assertTrue(printed.contains(
                    "No countries found for region: Caribbean"));

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Tests formatted report output containing
     * a valid country and a null entry.
     */
    @Test
    public void printReportValidListTest() {

        Requirement6 req6 = new Requirement6(null);

        ArrayList<Country> countries = new ArrayList<>();

        Country c = new Country();
        c.setCode("CUB");
        c.setName("Cuba");
        c.setContinent("North America");
        c.setRegion("Caribbean");
        c.setPopulation(11201000);
        c.setCapital("La Habana");

        countries.add(c);
        countries.add(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req6.printReport(countries, "Caribbean", 1);

            String printed = output.toString();

            assertTrue(printed.contains(
                    "Top 1 Populated Countries in Region: Caribbean"));
            assertTrue(printed.contains("Cuba"));
            assertTrue(printed.contains("La Habana"));
            assertTrue(printed.contains("Population"));
            assertTrue(printed.contains("Capital"));

        } finally {
            System.setOut(originalOut);
        }
    }
}