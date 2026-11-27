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
 * Unit tests for REQ-14:
 * The top N populated cities in a region.
 *
 * Tests input validation, database connectivity, SQL execution,
 * city data mapping, empty results, exception handling,
 * resource management and formatted report output.
 *
 * Mockito simulates JDBC database operations without requiring
 * an active MySQL connection.
 */
public class Requirement14Test {

    /**
     * Test 1:
     * Verifies that a null database connection returns an empty list.
     */
    @Test
    public void getTopNCitiesInRegionNullConnectionTest() {

        Requirement14 req14 = new Requirement14(null);

        ArrayList<City> result =
                req14.getTopNCitiesInRegion("Western Europe", 5);

        assertNotNull(result);
        assertTrue(
                result.isEmpty(),
                "Expected empty list when database connection is null"
        );
    }

    /**
     * Test 2:
     * Verifies that null, empty and whitespace-only region
     * parameters are rejected without accessing the database.
     */
    @Test
    public void getTopNCitiesInRegionInvalidRegionTest() {

        Connection con = mock(Connection.class);
        Requirement14 req14 = new Requirement14(con);

        assertTrue(
                req14.getTopNCitiesInRegion(null, 5).isEmpty()
        );

        assertTrue(
                req14.getTopNCitiesInRegion("", 5).isEmpty()
        );

        assertTrue(
                req14.getTopNCitiesInRegion("   ", 5).isEmpty()
        );

        verifyNoInteractions(con);
    }

    /**
     * Test 3:
     * Verifies that zero and negative N values are rejected.
     */
    @Test
    public void getTopNCitiesInRegionInvalidNTest() {

        Connection con = mock(Connection.class);
        Requirement14 req14 = new Requirement14(con);

        assertTrue(
                req14.getTopNCitiesInRegion("Western Europe", 0).isEmpty()
        );

        assertTrue(
                req14.getTopNCitiesInRegion("Caribbean", -2).isEmpty()
        );

        verifyNoInteractions(con);
    }

    /**
     * Test 4:
     * Verifies successful retrieval of multiple cities.
     *
     * Checks city field mapping, SQL region filtering,
     * descending population ordering, LIMIT binding
     * and JDBC resource closure.
     */
    @Test
    public void getTopNCitiesInRegionSuccessTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, true, false);

        when(rset.getInt("ID"))
                .thenReturn(2974, 2975);

        when(rset.getString("Name"))
                .thenReturn("Paris", "Berlin");

        when(rset.getString("CountryCode"))
                .thenReturn("FRA", "DEU");

        when(rset.getString("District"))
                .thenReturn("Île-de-France", "Berlin");

        when(rset.getInt("Population"))
                .thenReturn(2125246, 1800000);

        Requirement14 req14 = new Requirement14(con);

        ArrayList<City> cities =
                req14.getTopNCitiesInRegion("Western Europe", 2);

        assertNotNull(cities);
        assertEquals(2, cities.size());

        // Verify the first city.
        City paris = cities.get(0);

        assertEquals(2974, paris.getId());
        assertEquals("Paris", paris.getName());
        assertEquals("FRA", paris.getCountryCode());
        assertEquals("Île-de-France", paris.getDistrict());
        assertEquals(2125246, paris.getPopulation());

        // Verify the second city.
        City berlin = cities.get(1);

        assertEquals(2975, berlin.getId());
        assertEquals("Berlin", berlin.getName());
        assertEquals("DEU", berlin.getCountryCode());
        assertEquals("Berlin", berlin.getDistrict());
        assertEquals(1800000, berlin.getPopulation());

        // Verify the mocked result sequence.
        assertTrue(
                paris.getPopulation() >= berlin.getPopulation()
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
                                && sql.contains("WHERE c.Region = ?")
                                && sql.contains(
                                "ORDER BY ct.Population DESC"
                        )
                                && sql.contains("LIMIT ?")
                )
        );

        // Verify both SQL parameters.
        verify(pstmt).setString(1, "Western Europe");
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
    public void getTopNCitiesInRegionSingleCityTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(2974);
        when(rset.getString("Name")).thenReturn("Paris");
        when(rset.getString("CountryCode")).thenReturn("FRA");
        when(rset.getString("District"))
                .thenReturn("Île-de-France");
        when(rset.getInt("Population")).thenReturn(2125246);

        Requirement14 req14 = new Requirement14(con);

        ArrayList<City> cities =
                req14.getTopNCitiesInRegion("Western Europe", 1);

        assertNotNull(cities);
        assertEquals(1, cities.size());

        City city = cities.get(0);

        assertEquals(2974, city.getId());
        assertEquals("Paris", city.getName());
        assertEquals("FRA", city.getCountryCode());
        assertEquals("Île-de-France", city.getDistrict());
        assertEquals(2125246, city.getPopulation());

        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt).setInt(2, 1);
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 6:
     * Verifies that leading and trailing whitespace
     * is removed from the region parameter.
     */
    @Test
    public void getTopNCitiesInRegionTrimmedParameterTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement14 req14 = new Requirement14(con);

        ArrayList<City> cities =
                req14.getTopNCitiesInRegion("  Western Europe  ", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt).setInt(2, 5);

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 7:
     * Verifies that a large positive N is accepted
     * and available records are returned.
     */
    @Test
    public void getTopNCitiesInRegionLargeLimitTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(2974);
        when(rset.getString("Name")).thenReturn("Paris");
        when(rset.getString("CountryCode")).thenReturn("FRA");
        when(rset.getString("District"))
                .thenReturn("Île-de-France");
        when(rset.getInt("Population")).thenReturn(2125246);

        Requirement14 req14 = new Requirement14(con);

        ArrayList<City> cities =
                req14.getTopNCitiesInRegion("Western Europe", 1000);

        assertNotNull(cities);
        assertEquals(1, cities.size());
        assertEquals("Paris", cities.get(0).getName());

        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt).setInt(2, 1000);

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 8:
     * Verifies that an empty ResultSet returns an empty list.
     */
    @Test
    public void getTopNCitiesInRegionEmptyResultTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement14 req14 = new Requirement14(con);

        ArrayList<City> cities =
                req14.getTopNCitiesInRegion("Caribbean", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Caribbean");
        verify(pstmt).setInt(2, 5);

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 9:
     * Verifies SQL exception handling during statement preparation.
     */
    @Test
    public void getTopNCitiesInRegionPrepareStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(
                        new SQLException("Statement preparation failed")
                );

        Requirement14 req14 = new Requirement14(con);

        ArrayList<City> cities =
                req14.getTopNCitiesInRegion("Western Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(con).prepareStatement(anyString());
    }

    /**
     * Test 10:
     * Verifies SQL exception handling when binding the region parameter.
     */
    @Test
    public void getTopNCitiesInRegionSetRegionExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid region parameter"))
                .when(pstmt)
                .setString(1, "Western Europe");

        Requirement14 req14 = new Requirement14(con);

        ArrayList<City> cities =
                req14.getTopNCitiesInRegion("Western Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt, never()).setInt(2, 5);
        verify(pstmt, never()).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 11:
     * Verifies SQL exception handling when binding the LIMIT parameter.
     */
    @Test
    public void getTopNCitiesInRegionSetLimitExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid LIMIT parameter"))
                .when(pstmt)
                .setInt(2, 5);

        Requirement14 req14 = new Requirement14(con);

        ArrayList<City> cities =
                req14.getTopNCitiesInRegion("Western Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt).setInt(2, 5);
        verify(pstmt, never()).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 12:
     * Verifies SQL exception handling during query execution.
     */
    @Test
    public void getTopNCitiesInRegionQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement14 req14 = new Requirement14(con);

        ArrayList<City> cities =
                req14.getTopNCitiesInRegion("Western Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt).setInt(2, 5);
        verify(pstmt).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 13:
     * Verifies SQL exception handling while reading ResultSet records.
     */
    @Test
    public void getTopNCitiesInRegionResultSetExceptionTest()
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

        Requirement14 req14 = new Requirement14(con);

        ArrayList<City> cities =
                req14.getTopNCitiesInRegion("Western Europe", 5);

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
    public void getTopNCitiesInRegionFieldMappingExceptionTest()
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

        Requirement14 req14 = new Requirement14(con);

        ArrayList<City> cities =
                req14.getTopNCitiesInRegion("Western Europe", 5);

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
    public void getTopNCitiesInRegionResultSetCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("ResultSet close failed"))
                .when(rset)
                .close();

        Requirement14 req14 = new Requirement14(con);

        ArrayList<City> cities =
                req14.getTopNCitiesInRegion("Western Europe", 5);

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
    public void getTopNCitiesInRegionStatementCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("PreparedStatement close failed"))
                .when(pstmt)
                .close();

        Requirement14 req14 = new Requirement14(con);

        ArrayList<City> cities =
                req14.getTopNCitiesInRegion("Western Europe", 5);

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

        Requirement14 req14 = new Requirement14(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req14.printCities(null, "Western Europe", 5);
            req14.printCities(new ArrayList<>(), "Western Europe", 5);

            String printed = output.toString();

            assertEquals(
                    2,
                    printed.split(
                            "No cities found for Top 5 cities in region: Western Europe",
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

        Requirement14 req14 = new Requirement14(null);

        ArrayList<City> cities = new ArrayList<>();

        City city = new City();

        city.setId(2974);
        city.setName("Paris");
        city.setCountryCode("FRA");
        city.setDistrict("Île-de-France");
        city.setPopulation(2125246);

        cities.add(city);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req14.printCities(cities, "Western Europe", 1);

            String printed = output.toString();

            assertTrue(printed.contains("REQ-14"));
            assertTrue(printed.contains("Top 1"));
            assertTrue(printed.contains("Western Europe"));
            assertTrue(printed.contains("Paris"));
            assertTrue(printed.contains("FRA"));
            assertTrue(printed.contains("Île-de-France"));
            assertTrue(printed.contains("2125246"));
            assertTrue(printed.contains("Total cities displayed: 1"));

        } finally {
            System.setOut(originalOut);
        }
    }
}