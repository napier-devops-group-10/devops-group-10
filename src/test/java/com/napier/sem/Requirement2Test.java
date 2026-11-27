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
 * Unit tests for REQ-02:
 * All countries in a continent organised by
 * largest population to smallest.
 *
 * Tests input validation, successful database retrieval,
 * SQL exception handling, capital city validation,
 * resource management and report formatting.
 */
public class Requirement2Test {

    /**
     * Tests behaviour when the database connection is null.
     */
    @Test
    public void getCountriesByContinentNullConnectionTest() {

        Requirement2 req2 = new Requirement2(null);

        assertTrue(req2.getCountriesByContinent("Europe").isEmpty());
    }

    /**
     * Tests invalid continent parameters.
     */
    @Test
    public void getCountriesByContinentInvalidParamTest() {

        Connection con = mock(Connection.class);
        Requirement2 req2 = new Requirement2(con);

        assertTrue(req2.getCountriesByContinent(null).isEmpty());
        assertTrue(req2.getCountriesByContinent("").isEmpty());
        assertTrue(req2.getCountriesByContinent("   ").isEmpty());

        // Invalid parameters should not execute SQL.
        verifyNoInteractions(con);
    }

    /**
     * Tests successful retrieval of multiple countries.
     *
     * Also verifies the handling of countries
     * with and without capital city information.
     */
    @Test
    public void getCountriesByContinentValidDataTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        // Simulate two records followed by the end of the result set.
        when(rset.next()).thenReturn(true, true, false);

        when(rset.getString("Code"))
                .thenReturn("DEU", "FRA");

        when(rset.getString("Name"))
                .thenReturn("Germany", "France");

        when(rset.getString("Continent"))
                .thenReturn("Europe", "Europe");

        when(rset.getString("Region"))
                .thenReturn("Western Europe", "Western Europe");

        when(rset.getDouble("SurfaceArea"))
                .thenReturn(357022.0, 551500.0);

        when(rset.getInt("Population"))
                .thenReturn(82164700, 59225700);

        // First country has a capital; second has no capital.
        when(rset.getString("Capital"))
                .thenReturn("Berlin", (String) null);

        Requirement2 req2 = new Requirement2(con);

        ArrayList<Country> countries =
                req2.getCountriesByContinent("Europe");

        // Verify the number of countries returned.
        assertEquals(2, countries.size());

        // Verify Germany.
        Country germany = countries.get(0);

        assertEquals("DEU", germany.getCode());
        assertEquals("Germany", germany.getName());
        assertEquals("Europe", germany.getContinent());
        assertEquals("Western Europe", germany.getRegion());
        assertEquals(357022.0, germany.getSurfaceArea(), 0.001);
        assertEquals(82164700, germany.getPopulation());
        assertEquals("Berlin", germany.getCapital());

        // Verify France.
        Country france = countries.get(1);

        assertEquals("FRA", france.getCode());
        assertEquals("France", france.getName());
        assertEquals("Europe", france.getContinent());
        assertEquals("Western Europe", france.getRegion());
        assertEquals(551500.0, france.getSurfaceArea(), 0.001);
        assertEquals(59225700, france.getPopulation());

        // Null capital must be replaced with N/A.
        assertEquals("N/A", france.getCapital());

        // Verify SQL parameter binding.
        verify(pstmt).setString(1, "Europe");
        verify(pstmt).executeQuery();

        // Verify that database resources are closed.
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Tests that whitespace is removed from
     * the continent parameter before SQL execution.
     */
    @Test
    public void getCountriesByContinentTrimTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);
        when(rset.next()).thenReturn(false);

        Requirement2 req2 = new Requirement2(con);

        ArrayList<Country> countries =
                req2.getCountriesByContinent("  Europe  ");

        assertTrue(countries.isEmpty());

        verify(pstmt).setString(1, "Europe");
        verify(pstmt).executeQuery();

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Tests a query that returns no matching countries.
     */
    @Test
    public void getCountriesByContinentNoResultsTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);
        when(rset.next()).thenReturn(false);

        Requirement2 req2 = new Requirement2(con);

        ArrayList<Country> countries =
                req2.getCountriesByContinent("Antarctica");

        assertTrue(countries.isEmpty());

        verify(pstmt).setString(1, "Antarctica");
        verify(pstmt).executeQuery();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Tests SQLException handling during
     * PreparedStatement creation.
     */
    @Test
    public void getCountriesByContinentSQLExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(new SQLException(
                        "Simulated database connection error"));

        Requirement2 req2 = new Requirement2(con);

        ArrayList<Country> countries =
                req2.getCountriesByContinent("Europe");

        assertTrue(countries.isEmpty());

        verify(con).prepareStatement(anyString());
    }

    /**
     * Tests SQLException handling during query execution.
     */
    @Test
    public void getCountriesByContinentExecuteQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(new SQLException(
                        "Simulated SQL execution failure"));

        Requirement2 req2 = new Requirement2(con);

        ArrayList<Country> countries =
                req2.getCountriesByContinent("Europe");

        assertTrue(countries.isEmpty());

        verify(pstmt).close();
    }

    /**
     * Tests report printing with a null or empty list.
     */
    @Test
    public void printCountriesEmptyListTest() {

        Requirement2 req2 = new Requirement2(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req2.printCountries(null, "Europe");
            req2.printCountries(new ArrayList<>(), "Europe");

            String printed = output.toString();

            assertTrue(printed.contains(
                    "No countries found for continent: Europe"));

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Tests formatted output with a valid country.
     */
    @Test
    public void printCountriesValidListTest() {

        Requirement2 req2 = new Requirement2(null);

        ArrayList<Country> countries = new ArrayList<>();

        Country c = new Country();

        c.setCode("DEU");
        c.setName("Germany");
        c.setContinent("Europe");
        c.setRegion("Western Europe");
        c.setPopulation(82164700);
        c.setCapital("Berlin");

        countries.add(c);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req2.printCountries(countries, "Europe");

            String printed = output.toString();

            assertTrue(printed.contains("Germany"));
            assertTrue(printed.contains("Berlin"));
            assertTrue(printed.contains("82164700"));
            assertTrue(printed.contains(
                    "REQ-02: Countries in Continent 'Europe'"));
            assertTrue(printed.contains(
                    "Total records displayed: 1"));

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Tests report formatting with multiple countries.
     */
    @Test
    public void printCountriesMultipleRecordsTest() {

        Requirement2 req2 = new Requirement2(null);

        ArrayList<Country> countries = new ArrayList<>();

        Country germany = new Country();
        germany.setCode("DEU");
        germany.setName("Germany");
        germany.setContinent("Europe");
        germany.setRegion("Western Europe");
        germany.setPopulation(82164700);
        germany.setCapital("Berlin");

        Country france = new Country();
        france.setCode("FRA");
        france.setName("France");
        france.setContinent("Europe");
        france.setRegion("Western Europe");
        france.setPopulation(59225700);
        france.setCapital("Paris");

        countries.add(germany);
        countries.add(france);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req2.printCountries(countries, "Europe");

            String printed = output.toString();

            assertTrue(printed.contains("Germany"));
            assertTrue(printed.contains("France"));
            assertTrue(printed.contains(
                    "Total records displayed: 2"));

        } finally {
            System.setOut(originalOut);
        }
    }
}