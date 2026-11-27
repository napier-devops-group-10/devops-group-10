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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for REQ-13:
 * The top N populated cities in a continent.
 *
 * Tests input validation, database connectivity, SQL execution,
 * city data mapping, empty results, exception handling,
 * resource management and formatted report output.
 *
 * Mockito is used to simulate JDBC database operations
 * without requiring an active MySQL connection.
 */
public class Requirement13Test {

    /**
     * Test 1:
     * Verifies that a null database connection returns an empty list.
     */
    @Test
    public void getTopNCitiesInContinentNullConnectionTest() {

        Requirement13 req13 = new Requirement13(null);

        ArrayList<City> result =
                req13.getTopNCitiesInContinent("Europe", 5);

        assertNotNull(result);
        assertTrue(
                result.isEmpty(),
                "Expected empty list when database connection is null"
        );
    }

    /**
     * Test 2:
     * Verifies that null, empty and whitespace-only
     * continent parameters are rejected.
     */
    @Test
    public void getTopNCitiesInContinentInvalidContinentTest() {

        Connection con = mock(Connection.class);
        Requirement13 req13 = new Requirement13(con);

        assertTrue(
                req13.getTopNCitiesInContinent(null, 5).isEmpty()
        );

        assertTrue(
                req13.getTopNCitiesInContinent("", 5).isEmpty()
        );

        assertTrue(
                req13.getTopNCitiesInContinent("   ", 5).isEmpty()
        );

        // Invalid input must not trigger database operations.
        verifyNoInteractions(con);
    }

    /**
     * Test 3:
     * Verifies that zero and negative N values are rejected.
     */
    @Test
    public void getTopNCitiesInContinentInvalidNTest() {

        Connection con = mock(Connection.class);
        Requirement13 req13 = new Requirement13(con);

        assertTrue(
                req13.getTopNCitiesInContinent("Europe", 0).isEmpty()
        );

        assertTrue(
                req13.getTopNCitiesInContinent("Asia", -3).isEmpty()
        );

        verifyNoInteractions(con);
    }

    /**
     * Test 4:
     * Verifies successful retrieval of multiple cities.
     *
     * Checks city data mapping, SQL continent filtering,
     * population ordering, LIMIT binding and resource closure.
     */
    @Test
    public void getTopNCitiesInContinentSuccessTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

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

        Requirement13 req13 = new Requirement13(con);

        ArrayList<City> cities =
                req13.getTopNCitiesInContinent("Europe", 2);

        assertNotNull(cities);
        assertEquals(2, cities.size());

        // Verify the first city.
        City london = cities.get(0);

        assertEquals(456, london.getId());
        assertEquals("London", london.getName());
        assertEquals("GBR", london.getCountryCode());
        assertEquals("England", london.getDistrict());
        assertEquals(7285000, london.getPopulation());

        // Verify the second city.
        City berlin = cities.get(1);

        assertEquals(457, berlin.getId());
        assertEquals("Berlin", berlin.getName());
        assertEquals("DEU", berlin.getCountryCode());
        assertEquals("Berlin", berlin.getDistrict());
        assertEquals(3386667, berlin.getPopulation());

        // Verify the mocked records are in descending order.
        assertTrue(
                london.getPopulation() >= berlin.getPopulation()
        );

        // Verify the SQL query structure.
        verify(con).prepareStatement(
                argThat(sql ->
                        sql.contains(
                                "SELECT ct.ID, ct.Name, ct.CountryCode, ct.District, ct.Population"
                        )
                                && sql.contains("FROM city ct")
                                && sql.contains(
                                "JOIN country c ON ct.CountryCode = c.Code"
                        )
                                && sql.contains("WHERE c.Continent = ?")
                                && sql.contains(
                                "ORDER BY ct.Population DESC"
                        )
                                && sql.contains("LIMIT ?")
                )
        );

        // Verify both SQL parameters.
        verify(pstmt).setString(1, "Europe");
        verify(pstmt).setInt(2, 2);
        verify(pstmt).executeQuery();

        // Verify JDBC resources are closed.
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 5:
     * Verifies successful retrieval when N equals 1.
     */
    @Test
    public void getTopNCitiesInContinentSingleCityTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(456);
        when(rset.getString("Name")).thenReturn("London");
        when(rset.getString("CountryCode")).thenReturn("GBR");
        when(rset.getString("District")).thenReturn("England");
        when(rset.getInt("Population")).thenReturn(7285000);

        Requirement13 req13 = new Requirement13(con);

        ArrayList<City> cities =
                req13.getTopNCitiesInContinent("Europe", 1);

        assertNotNull(cities);
        assertEquals(1, cities.size());

        City city = cities.get(0);

        assertEquals(456, city.getId());
        assertEquals("London", city.getName());
        assertEquals("GBR", city.getCountryCode());
        assertEquals("England", city.getDistrict());
        assertEquals(7285000, city.getPopulation());

        verify(pstmt).setString(1, "Europe");
        verify(pstmt).setInt(2, 1);
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 6:
     * Verifies that leading and trailing whitespace
     * is removed from the continent parameter.
     */
    @Test
    public void getTopNCitiesInContinentTrimmedParameterTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement13 req13 = new Requirement13(con);

        ArrayList<City> cities =
                req13.getTopNCitiesInContinent("  Europe  ", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Europe");
        verify(pstmt).setInt(2, 5);

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 7:
     * Verifies that a positive N larger than the available
     * records returns the available cities.
     */
    @Test
    public void getTopNCitiesInContinentLargeLimitTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(456);
        when(rset.getString("Name")).thenReturn("London");
        when(rset.getString("CountryCode")).thenReturn("GBR");
        when(rset.getString("District")).thenReturn("England");
        when(rset.getInt("Population")).thenReturn(7285000);

        Requirement13 req13 = new Requirement13(con);

        ArrayList<City> cities =
                req13.getTopNCitiesInContinent("Europe", 1000);

        assertNotNull(cities);
        assertEquals(1, cities.size());
        assertEquals("London", cities.get(0).getName());

        verify(pstmt).setString(1, "Europe");
        verify(pstmt).setInt(2, 1000);
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 8:
     * Verifies that an empty ResultSet returns an empty list.
     */
    @Test
    public void getTopNCitiesInContinentEmptyResultTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement13 req13 = new Requirement13(con);

        ArrayList<City> cities =
                req13.getTopNCitiesInContinent("Antarctica", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Antarctica");
        verify(pstmt).setInt(2, 5);

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 9:
     * Verifies SQL exception handling during statement preparation.
     */
    @Test
    public void getTopNCitiesInContinentPrepareStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(
                        new SQLException("Statement preparation failed")
                );

        Requirement13 req13 = new Requirement13(con);

        ArrayList<City> cities =
                req13.getTopNCitiesInContinent("Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(con).prepareStatement(anyString());
    }

    /**
     * Test 10:
     * Verifies SQL exception handling when binding the continent parameter.
     */
    @Test
    public void getTopNCitiesInContinentSetContinentExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid continent parameter"))
                .when(pstmt)
                .setString(1, "Europe");

        Requirement13 req13 = new Requirement13(con);

        ArrayList<City> cities =
                req13.getTopNCitiesInContinent("Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Europe");
        verify(pstmt, never()).setInt(2, 5);
        verify(pstmt, never()).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 11:
     * Verifies SQL exception handling when binding the LIMIT parameter.
     */
    @Test
    public void getTopNCitiesInContinentSetLimitExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid LIMIT parameter"))
                .when(pstmt)
                .setInt(2, 5);

        Requirement13 req13 = new Requirement13(con);

        ArrayList<City> cities =
                req13.getTopNCitiesInContinent("Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Europe");
        verify(pstmt).setInt(2, 5);
        verify(pstmt, never()).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 12:
     * Verifies SQL exception handling during query execution.
     */
    @Test
    public void getTopNCitiesInContinentQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement13 req13 = new Requirement13(con);

        ArrayList<City> cities =
                req13.getTopNCitiesInContinent("Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Europe");
        verify(pstmt).setInt(2, 5);
        verify(pstmt).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 13:
     * Verifies SQL exception handling while reading ResultSet records.
     */
    @Test
    public void getTopNCitiesInContinentResultSetExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next())
                .thenThrow(
                        new SQLException("Failed to read city records")
                );

        Requirement13 req13 = new Requirement13(con);

        ArrayList<City> cities =
                req13.getTopNCitiesInContinent("Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).next();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 14:
     * Verifies SQL exception handling when mapping city fields.
     */
    @Test
    public void getTopNCitiesInContinentFieldMappingExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true);

        when(rset.getInt("ID"))
                .thenThrow(
                        new SQLException("Unable to read city ID")
                );

        Requirement13 req13 = new Requirement13(con);

        ArrayList<City> cities =
                req13.getTopNCitiesInContinent("Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).getInt("ID");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 15:
     * Verifies SQL exception handling when closing the ResultSet.
     */
    @Test
    public void getTopNCitiesInContinentResultSetCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("ResultSet close failed"))
                .when(rset)
                .close();

        Requirement13 req13 = new Requirement13(con);

        ArrayList<City> cities =
                req13.getTopNCitiesInContinent("Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 16:
     * Verifies SQL exception handling when closing the PreparedStatement.
     */
    @Test
    public void getTopNCitiesInContinentStatementCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("PreparedStatement close failed"))
                .when(pstmt)
                .close();

        Requirement13 req13 = new Requirement13(con);

        ArrayList<City> cities =
                req13.getTopNCitiesInContinent("Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 17:
     * Verifies that printCities handles null and empty lists.
     */
    @Test
    public void printCitiesEmptyListTest() {

        Requirement13 req13 = new Requirement13(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req13.printCities(null, "Europe", 5);
            req13.printCities(new ArrayList<>(), "Europe", 5);

            String printed = output.toString();

            assertEquals(
                    2,
                    printed.split(
                            "No cities found for Top 5 cities in continent: Europe",
                            -1
                    ).length - 1
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 18:
     * Verifies that printCities formats valid City records correctly.
     */
    @Test
    public void printCitiesValidListTest() {

        Requirement13 req13 = new Requirement13(null);

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

            req13.printCities(cities, "Europe", 1);

            String printed = output.toString();

            assertTrue(printed.contains("REQ-13"));
            assertTrue(printed.contains("Top 1"));
            assertTrue(printed.contains("Europe"));
            assertTrue(printed.contains("London"));
            assertTrue(printed.contains("GBR"));
            assertTrue(printed.contains("England"));
            assertTrue(printed.contains("7285000"));
            assertTrue(printed.contains("Total cities displayed: 1"));

        } finally {
            System.setOut(originalOut);
        }
    }
}