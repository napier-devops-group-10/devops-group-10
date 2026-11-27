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
 * Unit tests for REQ-04:
 * The top N populated countries in the world.
 *
 * Tests input validation, database connectivity, successful SQL queries,
 * population ordering, missing capital information, empty results,
 * SQL exceptions, resource management and formatted report output.
 *
 * Mockito is used to simulate database interactions without requiring
 * a running MySQL database.
 */
public class Requirement4Test {

    /**
     * Test 1:
     * Verifies that a null database connection returns an empty list.
     */
    @Test
    public void getTopNCountriesWorldNullConnectionTest() {

        Requirement4 req4 = new Requirement4(null);

        ArrayList<Country> countries =
                req4.getTopNCountriesWorld(10);

        assertNotNull(countries);
        assertTrue(countries.isEmpty());
    }

    /**
     * Test 2:
     * Verifies that zero and negative values of N are rejected.
     */
    @Test
    public void getTopNCountriesWorldInvalidNTest() {

        Requirement4 req4 = new Requirement4(null);

        assertTrue(req4.getTopNCountriesWorld(0).isEmpty());
        assertTrue(req4.getTopNCountriesWorld(-5).isEmpty());
    }

    /**
     * Test 3:
     * Verifies successful retrieval of multiple countries.
     *
     * Also checks SQL ordering and LIMIT parameter binding.
     */
    @Test
    public void getTopNCountriesWorldSuccessTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        // Simulate two database records.
        when(rset.next()).thenReturn(true, true, false);

        when(rset.getString("Code"))
                .thenReturn("CHN", "IND");

        when(rset.getString("Name"))
                .thenReturn("China", "India");

        when(rset.getString("Continent"))
                .thenReturn("Asia");

        when(rset.getString("Region"))
                .thenReturn("Eastern Asia", "Southern and Central Asia");

        when(rset.getDouble("SurfaceArea"))
                .thenReturn(9572900.0, 3287263.0);

        when(rset.getInt("Population"))
                .thenReturn(1277558000, 1013662000);

        when(rset.getString("Capital"))
                .thenReturn("Peking", "New Delhi");

        Requirement4 req4 = new Requirement4(con);

        ArrayList<Country> countries =
                req4.getTopNCountriesWorld(2);

        assertNotNull(countries);
        assertEquals(2, countries.size());

        // Verify China.
        Country china = countries.get(0);

        assertEquals("CHN", china.getCode());
        assertEquals("China", china.getName());
        assertEquals("Asia", china.getContinent());
        assertEquals("Eastern Asia", china.getRegion());
        assertEquals(9572900.0, china.getSurfaceArea(), 0.001);
        assertEquals(1277558000, china.getPopulation());
        assertEquals("Peking", china.getCapital());

        // Verify India.
        Country india = countries.get(1);

        assertEquals("IND", india.getCode());
        assertEquals("India", india.getName());
        assertEquals("Asia", india.getContinent());
        assertEquals("Southern and Central Asia", india.getRegion());
        assertEquals(3287263.0, india.getSurfaceArea(), 0.001);
        assertEquals(1013662000, india.getPopulation());
        assertEquals("New Delhi", india.getCapital());

        // Verify returned population order.
        assertTrue(
                china.getPopulation() >= india.getPopulation()
        );

        // Verify the SQL query requests descending population.
        verify(con).prepareStatement(
                argThat(sql ->
                        sql.contains("ORDER BY c.Population DESC")
                                && sql.contains("LIMIT ?")
                )
        );

        // Verify N is passed to the prepared statement.
        verify(pstmt).setInt(1, 2);

        // Verify database resources are closed.
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 4:
     * Verifies that N = 1 is accepted and passed correctly
     * to the database query.
     */
    @Test
    public void getTopNCountriesWorldSingleCountryTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getString("Code")).thenReturn("CHN");
        when(rset.getString("Name")).thenReturn("China");
        when(rset.getString("Continent")).thenReturn("Asia");
        when(rset.getString("Region")).thenReturn("Eastern Asia");
        when(rset.getDouble("SurfaceArea")).thenReturn(9572900.0);
        when(rset.getInt("Population")).thenReturn(1277558000);
        when(rset.getString("Capital")).thenReturn("Peking");

        Requirement4 req4 = new Requirement4(con);

        ArrayList<Country> countries =
                req4.getTopNCountriesWorld(1);

        assertEquals(1, countries.size());
        assertEquals("China", countries.get(0).getName());

        verify(pstmt).setInt(1, 1);
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 5:
     * Verifies that countries without capital information
     * are assigned the default value "N/A".
     */
    @Test
    public void getTopNCountriesWorldNullCapitalTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getString("Code")).thenReturn("ATA");
        when(rset.getString("Name")).thenReturn("Antarctica");
        when(rset.getString("Continent")).thenReturn("Antarctica");
        when(rset.getString("Region")).thenReturn("Antarctica");
        when(rset.getDouble("SurfaceArea")).thenReturn(13120000.0);
        when(rset.getInt("Population")).thenReturn(0);

        // Simulate missing capital information.
        when(rset.getString("Capital"))
                .thenReturn((String) null);

        Requirement4 req4 = new Requirement4(con);

        ArrayList<Country> countries =
                req4.getTopNCountriesWorld(1);

        assertNotNull(countries);
        assertEquals(1, countries.size());
        assertEquals("ATA", countries.get(0).getCode());
        assertEquals("N/A", countries.get(0).getCapital());

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 6:
     * Verifies that an empty ResultSet produces an empty list.
     */
    @Test
    public void getTopNCountriesWorldEmptyResultTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(false);

        Requirement4 req4 = new Requirement4(con);

        ArrayList<Country> countries =
                req4.getTopNCountriesWorld(10);

        assertNotNull(countries);
        assertTrue(countries.isEmpty());

        verify(pstmt).setInt(1, 10);
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 7:
     * Verifies SQL exception handling when preparing
     * the database statement.
     */
    @Test
    public void getTopNCountriesWorldPrepareStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(new SQLException("Statement preparation failed"));

        Requirement4 req4 = new Requirement4(con);

        ArrayList<Country> countries =
                req4.getTopNCountriesWorld(10);

        assertNotNull(countries);
        assertTrue(countries.isEmpty());

        verify(con).prepareStatement(anyString());
    }

    /**
     * Test 8:
     * Verifies SQL exception handling during query execution.
     */
    @Test
    public void getTopNCountriesWorldQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(new SQLException("Query execution failed"));

        Requirement4 req4 = new Requirement4(con);

        ArrayList<Country> countries =
                req4.getTopNCountriesWorld(10);

        assertNotNull(countries);
        assertTrue(countries.isEmpty());

        verify(pstmt).setInt(1, 10);
        verify(pstmt).close();
    }

    /**
     * Test 9:
     * Verifies SQL exception handling when setting
     * the LIMIT parameter.
     */
    @Test
    public void getTopNCountriesWorldSetParameterExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid LIMIT parameter"))
                .when(pstmt).setInt(1, 10);

        Requirement4 req4 = new Requirement4(con);

        ArrayList<Country> countries =
                req4.getTopNCountriesWorld(10);

        assertNotNull(countries);
        assertTrue(countries.isEmpty());

        verify(pstmt).setInt(1, 10);
        verify(pstmt, never()).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 10:
     * Verifies formatted output for null and empty country lists.
     */
    @Test
    public void printCountriesEmptyListTest() {

        Requirement4 req4 = new Requirement4(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req4.printCountries(null, 10);
            req4.printCountries(new ArrayList<>(), 10);

            String printed = output.toString();

            assertTrue(
                    printed.contains(
                            "No countries found for Top 10 countries in the world."
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 11:
     * Verifies formatted output for a valid country list.
     */
    @Test
    public void printCountriesValidListTest() {

        Requirement4 req4 = new Requirement4(null);

        ArrayList<Country> countries = new ArrayList<>();

        Country country = new Country();

        country.setCode("IND");
        country.setName("India");
        country.setContinent("Asia");
        country.setRegion("Southern and Central Asia");
        country.setPopulation(1013662000);
        country.setCapital("New Delhi");

        countries.add(country);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req4.printCountries(countries, 1);

            String printed = output.toString();

            assertTrue(printed.contains("REQ-04"));
            assertTrue(printed.contains("Top 1"));
            assertTrue(printed.contains("IND"));
            assertTrue(printed.contains("India"));
            assertTrue(printed.contains("Asia"));
            assertTrue(printed.contains("Southern and Central Asia"));
            assertTrue(printed.contains("1013662000"));
            assertTrue(printed.contains("New Delhi"));

            assertTrue(
                    printed.contains("Total records displayed: 1")
            );

        } finally {
            System.setOut(originalOut);
        }
    }
}