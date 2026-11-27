package com.napier.sem;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for REQ-01:
 * All countries in the world organised by largest population to smallest.
 *
 * Tests database connectivity, successful queries, empty results,
 * missing capital information, SQL exceptions and formatted output.
 *
 * These tests use Mockito to simulate database interactions without
 * requiring a running MySQL database.
 */
public class Requirement1Test {

    /**
     * Test 1:
     * Verifies that a null database connection returns an empty list.
     */
    @Test
    public void getAllCountriesWorldNullConnectionTest() {

        Requirement1 req1 = new Requirement1(null);

        ArrayList<Country> countries = req1.getAllCountriesWorld();

        assertNotNull(countries);
        assertTrue(countries.isEmpty());
    }

    /**
     * Test 2:
     * Verifies successful retrieval of multiple countries.
     *
     * Also checks that the SQL query requests countries in
     * descending population order.
     */
    @Test
    public void getAllCountriesWorldSuccessTest() throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        // Simulate two database records.
        when(rset.next()).thenReturn(true, true, false);

        // Country codes.
        when(rset.getString("Code"))
                .thenReturn("CHN", "IND");

        // Country names.
        when(rset.getString("Name"))
                .thenReturn("China", "India");

        // Continents.
        when(rset.getString("Continent"))
                .thenReturn("Asia");

        // Regions.
        when(rset.getString("Region"))
                .thenReturn("Eastern Asia", "Southern Asia");

        // Surface areas.
        when(rset.getDouble("SurfaceArea"))
                .thenReturn(9572900.0, 3287263.0);

        // Populations.
        when(rset.getInt("Population"))
                .thenReturn(1277558000, 1013662000);

        // Capital cities.
        when(rset.getString("Capital"))
                .thenReturn("Peking", "New Delhi");

        Requirement1 req1 = new Requirement1(con);

        ArrayList<Country> countries = req1.getAllCountriesWorld();

        // Verify the number of records returned.
        assertNotNull(countries);
        assertEquals(2, countries.size());

        // Verify the first country.
        Country china = countries.get(0);

        assertEquals("CHN", china.getCode());
        assertEquals("China", china.getName());
        assertEquals("Asia", china.getContinent());
        assertEquals("Eastern Asia", china.getRegion());
        assertEquals(9572900.0, china.getSurfaceArea(), 0.001);
        assertEquals(1277558000, china.getPopulation());
        assertEquals("Peking", china.getCapital());

        // Verify the second country.
        Country india = countries.get(1);

        assertEquals("IND", india.getCode());
        assertEquals("India", india.getName());
        assertEquals("Asia", india.getContinent());
        assertEquals("Southern Asia", india.getRegion());
        assertEquals(3287263.0, india.getSurfaceArea(), 0.001);
        assertEquals(1013662000, india.getPopulation());
        assertEquals("New Delhi", india.getCapital());

        // Verify the returned records are in descending population order.
        assertTrue(
                countries.get(0).getPopulation()
                        >= countries.get(1).getPopulation()
        );

        // Verify the SQL query contains the required ordering.
        verify(stmt).executeQuery(
                argThat(sql ->
                        sql.contains("ORDER BY c.Population DESC")
                )
        );

        // Verify database resources are closed.
        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 3:
     * Verifies that countries without a capital city
     * are assigned the default value "N/A".
     */
    @Test
    public void getAllCountriesWorldNullCapitalTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        // Simulate one database record.
        when(rset.next()).thenReturn(true, false);

        when(rset.getString("Code"))
                .thenReturn("ATA");

        when(rset.getString("Name"))
                .thenReturn("Antarctica");

        when(rset.getString("Continent"))
                .thenReturn("Antarctica");

        when(rset.getString("Region"))
                .thenReturn("Antarctica");

        when(rset.getDouble("SurfaceArea"))
                .thenReturn(13120000.0);

        when(rset.getInt("Population"))
                .thenReturn(0);

        // Simulate a missing capital city.
        when(rset.getString("Capital"))
                .thenReturn((String) null);

        Requirement1 req1 = new Requirement1(con);

        ArrayList<Country> countries = req1.getAllCountriesWorld();

        assertNotNull(countries);
        assertEquals(1, countries.size());

        assertEquals("ATA", countries.get(0).getCode());
        assertEquals("Antarctica", countries.get(0).getName());
        assertEquals("N/A", countries.get(0).getCapital());

        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 4:
     * Verifies that an empty database result
     * produces an empty country list.
     */
    @Test
    public void getAllCountriesWorldEmptyResultTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        // No records returned.
        when(rset.next()).thenReturn(false);

        Requirement1 req1 = new Requirement1(con);

        ArrayList<Country> countries = req1.getAllCountriesWorld();

        assertNotNull(countries);
        assertTrue(countries.isEmpty());

        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 5:
     * Verifies SQL exception handling when creating a statement.
     */
    @Test
    public void getAllCountriesWorldStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.createStatement())
                .thenThrow(new SQLException("Connection failure"));

        Requirement1 req1 = new Requirement1(con);

        ArrayList<Country> countries = req1.getAllCountriesWorld();

        assertNotNull(countries);
        assertTrue(countries.isEmpty());

        verify(con).createStatement();
    }

    /**
     * Test 6:
     * Verifies SQL exception handling during query execution.
     */
    @Test
    public void getAllCountriesWorldQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);

        when(con.createStatement()).thenReturn(stmt);

        when(stmt.executeQuery(anyString()))
                .thenThrow(new SQLException("Query execution failed"));

        Requirement1 req1 = new Requirement1(con);

        ArrayList<Country> countries = req1.getAllCountriesWorld();

        assertNotNull(countries);
        assertTrue(countries.isEmpty());

        verify(stmt).close();
    }

    /**
     * Test 7:
     * Verifies formatted output when the country list is null or empty.
     */
    @Test
    public void printCountriesEmptyOrNullTest() {

        Requirement1 req1 = new Requirement1(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            // Test null country list.
            req1.printCountries(null);

            // Test empty country list.
            req1.printCountries(new ArrayList<>());

            String printed = output.toString();

            assertTrue(
                    printed.contains("No countries found in the world.")
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 8:
     * Verifies formatted output for a valid country list.
     */
    @Test
    public void printCountriesValidListTest() {

        Requirement1 req1 = new Requirement1(null);

        ArrayList<Country> countries = new ArrayList<>();

        Country country = new Country();

        country.setCode("CHN");
        country.setName("China");
        country.setContinent("Asia");
        country.setRegion("Eastern Asia");
        country.setPopulation(1277558000);
        country.setCapital("Peking");

        countries.add(country);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req1.printCountries(countries);

            String printed = output.toString();

            // Verify report heading.
            assertTrue(printed.contains("REQ-01"));

            // Verify country information.
            assertTrue(printed.contains("CHN"));
            assertTrue(printed.contains("China"));
            assertTrue(printed.contains("Asia"));
            assertTrue(printed.contains("Eastern Asia"));
            assertTrue(printed.contains("1277558000"));
            assertTrue(printed.contains("Peking"));

            // Verify total records.
            assertTrue(
                    printed.contains("Total records displayed: 1")
            );

        } finally {
            System.setOut(originalOut);
        }
    }
}