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
 * Unit tests for REQ-05:
 * Top N Populated Countries in a Continent.
 *
 * Tests input validation, database retrieval,
 * SQL exception handling and report formatting.
 */
public class Requirement5Test {

    /**
     * Verifies that a null database connection
     * returns an empty country list.
     */
    @Test
    public void getTopNCountriesInContinentNullConnectionTest() {
        Requirement5 req5 = new Requirement5(null);

        assertTrue(req5.getTopNCountriesInContinent("Europe", 5).isEmpty());
    }

    /**
     * Tests invalid parameters using a non-null
     * mock database connection.
     */
    @Test
    public void getTopNCountriesInContinentInvalidParamsTest() {
        Connection con = mock(Connection.class);
        Requirement5 req5 = new Requirement5(con);

        assertTrue(req5.getTopNCountriesInContinent(null, 5).isEmpty());
        assertTrue(req5.getTopNCountriesInContinent("", 5).isEmpty());
        assertTrue(req5.getTopNCountriesInContinent("   ", 5).isEmpty());
        assertTrue(req5.getTopNCountriesInContinent("Europe", 0).isEmpty());
        assertTrue(req5.getTopNCountriesInContinent("Asia", -3).isEmpty());

        // Invalid inputs must not trigger a database query.
        verifyNoInteractions(con);
    }

    /**
     * Tests successful retrieval of two countries
     * from a simulated SQL result set.
     */
    @Test
    public void getTopNCountriesInContinentValidDataTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        // Simulate two records followed by the end of the results.
        when(rset.next()).thenReturn(true, true, false);

        when(rset.getString("Code")).thenReturn("FRA", "DEU");
        when(rset.getString("Name")).thenReturn("France", "Germany");
        when(rset.getString("Continent")).thenReturn("Europe", "Europe");
        when(rset.getString("Region"))
                .thenReturn("Western Europe", "Western Europe");
        when(rset.getDouble("SurfaceArea"))
                .thenReturn(551500.0, 357022.0);
        when(rset.getInt("Population"))
                .thenReturn(59225700, 82164700);
        when(rset.getString("Capital"))
                .thenReturn("Paris", "Berlin");

        Requirement5 req5 = new Requirement5(con);

        ArrayList<Country> countries =
                req5.getTopNCountriesInContinent("Europe", 2);

        assertEquals(2, countries.size());

        // Verify the first country.
        assertEquals("FRA", countries.get(0).getCode());
        assertEquals("France", countries.get(0).getName());
        assertEquals("Europe", countries.get(0).getContinent());
        assertEquals("Western Europe", countries.get(0).getRegion());
        assertEquals(551500.0, countries.get(0).getSurfaceArea(), 0.001);
        assertEquals(59225700, countries.get(0).getPopulation());
        assertEquals("Paris", countries.get(0).getCapital());

        // Verify the second country.
        assertEquals("DEU", countries.get(1).getCode());
        assertEquals("Germany", countries.get(1).getName());
        assertEquals(82164700, countries.get(1).getPopulation());
        assertEquals("Berlin", countries.get(1).getCapital());

        // Verify SQL parameter binding.
        verify(pstmt).setString(1, "Europe");
        verify(pstmt).setInt(2, 2);
        verify(pstmt).executeQuery();

        // Verify that the statement is closed.
        verify(pstmt).close();
    }

    /**
     * Verifies that surrounding whitespace is removed
     * before the continent is passed to SQL.
     */
    @Test
    public void getTopNCountriesInContinentTrimTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);
        when(rset.next()).thenReturn(false);

        Requirement5 req5 = new Requirement5(con);

        ArrayList<Country> countries =
                req5.getTopNCountriesInContinent("  Europe  ", 5);

        assertTrue(countries.isEmpty());

        verify(pstmt).setString(1, "Europe");
        verify(pstmt).setInt(2, 5);
    }

    /**
     * Tests a query that returns no matching countries.
     */
    @Test
    public void getTopNCountriesInContinentNoResultsTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);
        when(rset.next()).thenReturn(false);

        Requirement5 req5 = new Requirement5(con);

        ArrayList<Country> countries =
                req5.getTopNCountriesInContinent("Antarctica", 5);

        assertTrue(countries.isEmpty());

        verify(pstmt).executeQuery();
    }

    /**
     * Tests SQLException handling during statement preparation.
     */
    @Test
    public void getTopNCountriesInContinentSQLExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(new SQLException("Simulated database error"));

        Requirement5 req5 = new Requirement5(con);

        ArrayList<Country> countries =
                req5.getTopNCountriesInContinent("Europe", 5);

        assertTrue(countries.isEmpty());
    }

    /**
     * Tests SQLException handling during query execution.
     */
    @Test
    public void getTopNCountriesInContinentExecuteQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(new SQLException("Query execution failed"));

        Requirement5 req5 = new Requirement5(con);

        ArrayList<Country> countries =
                req5.getTopNCountriesInContinent("Europe", 5);

        assertTrue(countries.isEmpty());

        verify(pstmt).close();
    }

    /**
     * Tests printing when the country list is null or empty.
     */
    @Test
    public void printCountriesEmptyOrNullTest() {

        Requirement5 req5 = new Requirement5(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req5.printCountries(null, "Europe", 5);
            req5.printCountries(new ArrayList<>(), "Europe", 5);

            String printed = output.toString();

            assertTrue(printed.contains(
                    "No countries to display for continent: Europe"));

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Tests formatted output containing a valid country
     * and a null entry.
     */
    @Test
    public void printCountriesValidListTest() {

        Requirement5 req5 = new Requirement5(null);

        ArrayList<Country> countries = new ArrayList<>();

        Country c = new Country();
        c.setCode("RUS");
        c.setName("Russian Federation");
        c.setContinent("Europe");
        c.setRegion("Eastern Europe");
        c.setPopulation(146934000);
        c.setCapital("Moscow");

        countries.add(c);
        countries.add(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req5.printCountries(countries, "Europe", 1);

            String printed = output.toString();

            assertTrue(printed.contains("Russian Federation"));
            assertTrue(printed.contains("146934000"));
            assertTrue(printed.contains("Total records displayed: 2"));

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Tests the backward-compatible overloaded print method.
     */
    @Test
    public void printCountriesOverloadedMethodTest() {

        Requirement5 req5 = new Requirement5(null);

        ArrayList<Country> countries = new ArrayList<>();

        Country c = new Country();
        c.setCode("JPN");
        c.setName("Japan");
        c.setContinent("Asia");
        c.setRegion("Eastern Asia");
        c.setPopulation(126714000);
        c.setCapital("Tokyo");

        countries.add(c);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req5.printCountries(countries);

            String printed = output.toString();

            assertTrue(printed.contains("Specified Continent"));
            assertTrue(printed.contains("Japan"));
            assertTrue(printed.contains("Top 1"));

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Tests the overloaded print method with null input.
     */
    @Test
    public void printCountriesOverloadedNullTest() {

        Requirement5 req5 = new Requirement5(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req5.printCountries(null);

            assertTrue(output.toString().contains(
                    "No countries to display for continent: Specified Continent"));

        } finally {
            System.setOut(originalOut);
        }
    }
}