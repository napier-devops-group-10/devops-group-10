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
 * Unit tests for REQ-09:
 * All cities in a region organised by largest population to smallest.
 *
 * Tests input validation, database connectivity, SQL execution,
 * city data mapping, empty results, exception handling,
 * resource management and formatted report output.
 *
 * Mockito simulates JDBC interactions without requiring MySQL.
 */
public class Requirement9Test {

    /**
     * Test 1:
     * A null database connection must return an empty list.
     */
    @Test
    public void getCitiesInRegionNullConnectionTest() {

        Requirement9 req9 = new Requirement9(null);

        ArrayList<City> result =
                req9.getCitiesInRegion("Western Europe");

        assertNotNull(result);
        assertTrue(
                result.isEmpty(),
                "Expected empty list when database connection is null"
        );
    }

    /**
     * Test 2:
     * Null, empty and whitespace-only region parameters are rejected.
     */
    @Test
    public void getCitiesInRegionInvalidParamTest() {

        Connection con = mock(Connection.class);
        Requirement9 req9 = new Requirement9(con);

        assertTrue(req9.getCitiesInRegion(null).isEmpty());
        assertTrue(req9.getCitiesInRegion("").isEmpty());
        assertTrue(req9.getCitiesInRegion("   ").isEmpty());

        // Invalid parameters must not access the database.
        verifyNoInteractions(con);
    }

    /**
     * Test 3:
     * Successfully retrieves multiple cities and maps their fields.
     */
    @Test
    public void getCitiesInRegionSuccessTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, true, false);

        when(rset.getInt("ID"))
                .thenReturn(2974, 3068);

        when(rset.getString("Name"))
                .thenReturn("Paris", "Brussels");

        when(rset.getString("CountryCode"))
                .thenReturn("FRA", "BEL");

        when(rset.getString("District"))
                .thenReturn("Île-de-France", "Bruxelles");

        when(rset.getInt("Population"))
                .thenReturn(2125246, 133859);

        Requirement9 req9 = new Requirement9(con);

        ArrayList<City> cities =
                req9.getCitiesInRegion("Western Europe");

        assertNotNull(cities);
        assertEquals(2, cities.size());

        City paris = cities.get(0);

        assertEquals(2974, paris.getId());
        assertEquals("Paris", paris.getName());
        assertEquals("FRA", paris.getCountryCode());
        assertEquals("Île-de-France", paris.getDistrict());
        assertEquals(2125246, paris.getPopulation());

        City brussels = cities.get(1);

        assertEquals(3068, brussels.getId());
        assertEquals("Brussels", brussels.getName());
        assertEquals("BEL", brussels.getCountryCode());
        assertEquals("Bruxelles", brussels.getDistrict());
        assertEquals(133859, brussels.getPopulation());

        // Confirm the mocked results are in descending order.
        assertTrue(
                paris.getPopulation() >= brussels.getPopulation()
        );

        // Verify the SQL query structure and ordering.
        verify(con).prepareStatement(
                argThat(sql ->
                        sql.contains("FROM city ct")
                                && sql.contains(
                                "JOIN country c ON ct.CountryCode = c.Code"
                        )
                                && sql.contains("WHERE c.Region = ?")
                                && sql.contains("ORDER BY ct.Population DESC")
                )
        );

        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt).executeQuery();

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 4:
     * Successfully retrieves a single city.
     */
    @Test
    public void getCitiesInRegionSingleCityTest()
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
        when(rset.getString("District")).thenReturn("Île-de-France");
        when(rset.getInt("Population")).thenReturn(2125246);

        Requirement9 req9 = new Requirement9(con);

        ArrayList<City> cities =
                req9.getCitiesInRegion("Western Europe");

        assertEquals(1, cities.size());

        City city = cities.get(0);

        assertEquals(2974, city.getId());
        assertEquals("Paris", city.getName());
        assertEquals("FRA", city.getCountryCode());
        assertEquals("Île-de-France", city.getDistrict());
        assertEquals(2125246, city.getPopulation());

        verify(pstmt).setString(1, "Western Europe");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 5:
     * Leading and trailing whitespace is removed from the region.
     */
    @Test
    public void getCitiesInRegionTrimmedParameterTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);
        when(rset.next()).thenReturn(false);

        Requirement9 req9 = new Requirement9(con);

        ArrayList<City> cities =
                req9.getCitiesInRegion("  Western Europe  ");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Western Europe");

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 6:
     * An empty ResultSet returns an empty city list.
     */
    @Test
    public void getCitiesInRegionEmptyResultTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);
        when(rset.next()).thenReturn(false);

        Requirement9 req9 = new Requirement9(con);

        ArrayList<City> cities =
                req9.getCitiesInRegion("Caribbean");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Caribbean");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 7:
     * Handles SQLException during statement preparation.
     */
    @Test
    public void getCitiesInRegionPrepareStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(
                        new SQLException("Statement preparation failed")
                );

        Requirement9 req9 = new Requirement9(con);

        ArrayList<City> cities =
                req9.getCitiesInRegion("Western Europe");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(con).prepareStatement(anyString());
    }

    /**
     * Test 8:
     * Handles SQLException while binding the region parameter.
     */
    @Test
    public void getCitiesInRegionSetParameterExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid region parameter"))
                .when(pstmt)
                .setString(1, "Western Europe");

        Requirement9 req9 = new Requirement9(con);

        ArrayList<City> cities =
                req9.getCitiesInRegion("Western Europe");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt, never()).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 9:
     * Handles SQLException during query execution.
     */
    @Test
    public void getCitiesInRegionQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement9 req9 = new Requirement9(con);

        ArrayList<City> cities =
                req9.getCitiesInRegion("Western Europe");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 10:
     * Handles SQLException while reading ResultSet records.
     */
    @Test
    public void getCitiesInRegionResultSetExceptionTest()
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

        Requirement9 req9 = new Requirement9(con);

        ArrayList<City> cities =
                req9.getCitiesInRegion("Western Europe");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).next();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 11:
     * Handles SQLException when closing the ResultSet.
     */
    @Test
    public void getCitiesInRegionResultSetCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(false);

        doThrow(new SQLException("ResultSet close failed"))
                .when(rset)
                .close();

        Requirement9 req9 = new Requirement9(con);

        ArrayList<City> cities =
                req9.getCitiesInRegion("Western Europe");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 12:
     * printCities handles a null list.
     */
    @Test
    public void printCitiesNullListTest() {

        Requirement9 req9 = new Requirement9(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req9.printCities(null, "Western Europe");

            assertTrue(
                    output.toString().contains(
                            "No cities found for region: Western Europe"
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 13:
     * printCities handles an empty list.
     */
    @Test
    public void printCitiesEmptyListTest() {

        Requirement9 req9 = new Requirement9(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req9.printCities(new ArrayList<>(), "Western Europe");

            assertTrue(
                    output.toString().contains(
                            "No cities found for region: Western Europe"
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 14:
     * printCities formats valid City records correctly.
     */
    @Test
    public void printCitiesValidListTest() {

        Requirement9 req9 = new Requirement9(null);

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

            req9.printCities(cities, "Western Europe");

            String printed = output.toString();

            assertTrue(printed.contains("REQ-09"));
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