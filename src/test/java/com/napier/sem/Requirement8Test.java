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
 * Unit tests for REQ-08:
 * All cities in a continent organised by largest population to smallest.
 *
 * Tests input validation, database connectivity, successful SQL queries,
 * city data mapping, population ordering, empty database results,
 * SQL exception handling, resource management and formatted report output.
 *
 * Mockito is used to simulate database interactions without requiring
 * a running MySQL database.
 */
public class Requirement8Test {

    /**
     * Test 1:
     * Verifies that a null database connection returns an empty list safely.
     */
    @Test
    public void getCitiesInContinentNullConnectionTest() {

        Requirement8 req8 = new Requirement8(null);

        ArrayList<City> result = req8.getCitiesInContinent("Europe");

        assertNotNull(result);
        assertTrue(
                result.isEmpty(),
                "Expected empty list when database connection is null"
        );
    }

    /**
     * Test 2:
     * Verifies that null, empty and whitespace-only continent
     * parameters are rejected safely.
     */
    @Test
    public void getCitiesInContinentInvalidParamTest() {

        Connection con = mock(Connection.class);
        Requirement8 req8 = new Requirement8(con);

        assertTrue(req8.getCitiesInContinent(null).isEmpty());
        assertTrue(req8.getCitiesInContinent("").isEmpty());
        assertTrue(req8.getCitiesInContinent("   ").isEmpty());

        // Invalid input must not trigger database operations.
        verifyNoInteractions(con);
    }

    /**
     * Test 3:
     * Verifies successful retrieval of multiple cities.
     *
     * Checks city data mapping, SQL filtering and
     * descending population ordering.
     */
    @Test
    public void getCitiesInContinentSuccessTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        // Simulate two database records.
        when(rset.next()).thenReturn(true, true, false);

        when(rset.getInt("ID"))
                .thenReturn(456, 457);

        when(rset.getString("Name"))
                .thenReturn("London", "Berlin");

        when(rset.getString("CountryCode"))
                .thenReturn("GBR", "DEU");

        when(rset.getString("District"))
                .thenReturn("England", "Berlin");

        when(rset.getInt("Population"))
                .thenReturn(7285000, 3386667);

        Requirement8 req8 = new Requirement8(con);

        ArrayList<City> cities =
                req8.getCitiesInContinent("Europe");

        assertNotNull(cities);
        assertEquals(2, cities.size());

        // Verify London.
        City london = cities.get(0);

        assertEquals(456, london.getId());
        assertEquals("London", london.getName());
        assertEquals("GBR", london.getCountryCode());
        assertEquals("England", london.getDistrict());
        assertEquals(7285000, london.getPopulation());

        // Verify Berlin.
        City berlin = cities.get(1);

        assertEquals(457, berlin.getId());
        assertEquals("Berlin", berlin.getName());
        assertEquals("DEU", berlin.getCountryCode());
        assertEquals("Berlin", berlin.getDistrict());
        assertEquals(3386667, berlin.getPopulation());

        // Verify the mocked records are in descending population order.
        assertTrue(
                london.getPopulation() >= berlin.getPopulation()
        );

        // Verify SQL joins city and country, filters by continent,
        // and requests descending population order.
        verify(con).prepareStatement(
                argThat(sql ->
                        sql.contains("FROM city ct")
                                && sql.contains(
                                "JOIN country c ON ct.CountryCode = c.Code"
                        )
                                && sql.contains("WHERE c.Continent = ?")
                                && sql.contains("ORDER BY ct.Population DESC")
                )
        );

        // Verify correct continent parameter binding.
        verify(pstmt).setString(1, "Europe");

        // Verify database resources are closed.
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 4:
     * Verifies successful retrieval of a single city.
     */
    @Test
    public void getCitiesInContinentSingleCityTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(1024);
        when(rset.getString("Name")).thenReturn("Mumbai (Bombay)");
        when(rset.getString("CountryCode")).thenReturn("IND");
        when(rset.getString("District")).thenReturn("Maharashtra");
        when(rset.getInt("Population")).thenReturn(10500000);

        Requirement8 req8 = new Requirement8(con);

        ArrayList<City> cities =
                req8.getCitiesInContinent("Asia");

        assertNotNull(cities);
        assertEquals(1, cities.size());

        City city = cities.get(0);

        assertEquals(1024, city.getId());
        assertEquals("Mumbai (Bombay)", city.getName());
        assertEquals("IND", city.getCountryCode());
        assertEquals("Maharashtra", city.getDistrict());
        assertEquals(10500000, city.getPopulation());

        verify(pstmt).setString(1, "Asia");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 5:
     * Verifies that leading and trailing whitespace
     * is removed from the continent parameter.
     */
    @Test
    public void getCitiesInContinentTrimmedParameterTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);
        when(rset.next()).thenReturn(false);

        Requirement8 req8 = new Requirement8(con);

        ArrayList<City> cities =
                req8.getCitiesInContinent("  Europe  ");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        // Verify that whitespace is removed before SQL execution.
        verify(pstmt).setString(1, "Europe");

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 6:
     * Verifies that an empty ResultSet produces an empty city list.
     */
    @Test
    public void getCitiesInContinentEmptyResultTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(false);

        Requirement8 req8 = new Requirement8(con);

        ArrayList<City> cities =
                req8.getCitiesInContinent("Africa");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Africa");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 7:
     * Verifies SQL exception handling when preparing
     * the database statement.
     */
    @Test
    public void getCitiesInContinentPrepareStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(
                        new SQLException("Statement preparation failed")
                );

        Requirement8 req8 = new Requirement8(con);

        ArrayList<City> cities =
                req8.getCitiesInContinent("Europe");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(con).prepareStatement(anyString());
    }

    /**
     * Test 8:
     * Verifies SQL exception handling when binding
     * the continent parameter.
     */
    @Test
    public void getCitiesInContinentSetParameterExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid continent parameter"))
                .when(pstmt).setString(1, "Europe");

        Requirement8 req8 = new Requirement8(con);

        ArrayList<City> cities =
                req8.getCitiesInContinent("Europe");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Europe");
        verify(pstmt, never()).executeQuery();

        // Statement must close after the exception.
        verify(pstmt).close();
    }

    /**
     * Test 9:
     * Verifies SQL exception handling during query execution.
     */
    @Test
    public void getCitiesInContinentQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement8 req8 = new Requirement8(con);

        ArrayList<City> cities =
                req8.getCitiesInContinent("Europe");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Europe");
        verify(pstmt).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 10:
     * Verifies SQL exception handling when reading ResultSet records.
     */
    @Test
    public void getCitiesInContinentResultSetExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        // Simulate a database error while reading records.
        when(rset.next())
                .thenThrow(
                        new SQLException("Failed to read city records")
                );

        Requirement8 req8 = new Requirement8(con);

        ArrayList<City> cities =
                req8.getCitiesInContinent("Europe");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).next();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 11:
     * Verifies that printCities handles null lists gracefully.
     */
    @Test
    public void printCitiesNullListTest() {

        Requirement8 req8 = new Requirement8(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req8.printCities(null, "Europe");

            assertTrue(
                    output.toString().contains(
                            "No cities found for continent: Europe"
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 12:
     * Verifies that printCities handles empty lists gracefully.
     */
    @Test
    public void printCitiesEmptyListTest() {

        Requirement8 req8 = new Requirement8(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req8.printCities(new ArrayList<>(), "Europe");

            assertTrue(
                    output.toString().contains(
                            "No cities found for continent: Europe"
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 13:
     * Verifies that printCities formats valid City records properly.
     */
    @Test
    public void printCitiesValidListTest() {

        Requirement8 req8 = new Requirement8(null);

        ArrayList<City> cities = new ArrayList<>();

        City city = new City();

        city.setId(456);
        city.setName("London");
        city.setCountryCode("GBR");
        city.setDistrict("England");
        city.setPopulation(7285000);

        cities.add(city);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req8.printCities(cities, "Europe");

            String printed = output.toString();

            assertTrue(printed.contains("REQ-08"));
            assertTrue(printed.contains("Europe"));
            assertTrue(printed.contains("London"));
            assertTrue(printed.contains("GBR"));
            assertTrue(printed.contains("England"));
            assertTrue(printed.contains("7285000"));

            assertTrue(
                    printed.contains("Total cities displayed: 1")
            );

        } finally {
            System.setOut(originalOut);
        }
    }
}