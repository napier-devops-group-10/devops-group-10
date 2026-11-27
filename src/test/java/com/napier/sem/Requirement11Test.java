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
 * Unit tests for REQ-11:
 * All cities in a district organised by largest population to smallest.
 *
 * Tests input validation, database connectivity, SQL execution,
 * city data mapping, empty results, exception handling,
 * resource management and formatted report output.
 *
 * Mockito is used to simulate JDBC database operations.
 */
public class Requirement11Test {

    /**
     * Test 1:
     * Verifies that a null database connection returns an empty list.
     */
    @Test
    public void getCitiesInDistrictNullConnectionTest() {

        Requirement11 req11 = new Requirement11(null);

        ArrayList<City> result =
                req11.getCitiesInDistrict("Gelderland");

        assertNotNull(result);
        assertTrue(
                result.isEmpty(),
                "Expected empty list when database connection is null"
        );
    }

    /**
     * Test 2:
     * Verifies that a null district parameter is rejected.
     */
    @Test
    public void getCitiesInDistrictNullParamTest() {

        Connection con = mock(Connection.class);
        Requirement11 req11 = new Requirement11(con);

        ArrayList<City> result =
                req11.getCitiesInDistrict(null);

        assertNotNull(result);
        assertTrue(
                result.isEmpty(),
                "Expected empty list when district is null"
        );

        verifyNoInteractions(con);
    }

    /**
     * Test 3:
     * Verifies that empty and whitespace-only district
     * parameters are rejected.
     */
    @Test
    public void getCitiesInDistrictEmptyParamTest() {

        Connection con = mock(Connection.class);
        Requirement11 req11 = new Requirement11(con);

        assertTrue(req11.getCitiesInDistrict("").isEmpty());
        assertTrue(req11.getCitiesInDistrict("   ").isEmpty());

        verifyNoInteractions(con);
    }

    /**
     * Test 4:
     * Verifies successful retrieval of multiple cities.
     *
     * Checks city data mapping, SQL filtering and
     * descending population ordering.
     */
    @Test
    public void getCitiesInDistrictSuccessTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, true, false);

        when(rset.getInt("ID"))
                .thenReturn(1, 2);

        when(rset.getString("Name"))
                .thenReturn("Palma de Mallorca", "Calvià");

        when(rset.getString("CountryCode"))
                .thenReturn("ESP", "ESP");

        when(rset.getString("District"))
                .thenReturn("Baleares", "Baleares");

        when(rset.getInt("Population"))
                .thenReturn(326993, 35000);

        Requirement11 req11 = new Requirement11(con);

        ArrayList<City> cities =
                req11.getCitiesInDistrict("Baleares");

        assertNotNull(cities);
        assertEquals(2, cities.size());

        // Verify the first city.
        City palma = cities.get(0);

        assertEquals(1, palma.getId());
        assertEquals("Palma de Mallorca", palma.getName());
        assertEquals("ESP", palma.getCountryCode());
        assertEquals("Baleares", palma.getDistrict());
        assertEquals(326993, palma.getPopulation());

        // Verify the second city.
        City calvia = cities.get(1);

        assertEquals(2, calvia.getId());
        assertEquals("Calvià", calvia.getName());
        assertEquals("ESP", calvia.getCountryCode());
        assertEquals("Baleares", calvia.getDistrict());
        assertEquals(35000, calvia.getPopulation());

        // Verify that the returned mock records are in descending order.
        assertTrue(
                palma.getPopulation() >= calvia.getPopulation()
        );

        // Verify the SQL query structure.
        verify(con).prepareStatement(
                argThat(sql ->
                        sql.contains("SELECT ID, Name, CountryCode, District, Population")
                                && sql.contains("FROM city")
                                && sql.contains("WHERE District = ?")
                                && sql.contains("ORDER BY Population DESC")
                )
        );

        verify(pstmt).setString(1, "Baleares");
        verify(pstmt).executeQuery();

        // Verify that database resources are closed.
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 5:
     * Verifies successful retrieval of a single city.
     */
    @Test
    public void getCitiesInDistrictSingleCityTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(1);
        when(rset.getString("Name")).thenReturn("Palma de Mallorca");
        when(rset.getString("CountryCode")).thenReturn("ESP");
        when(rset.getString("District")).thenReturn("Baleares");
        when(rset.getInt("Population")).thenReturn(326993);

        Requirement11 req11 = new Requirement11(con);

        ArrayList<City> cities =
                req11.getCitiesInDistrict("Baleares");

        assertNotNull(cities);
        assertEquals(1, cities.size());

        City city = cities.get(0);

        assertEquals(1, city.getId());
        assertEquals("Palma de Mallorca", city.getName());
        assertEquals("ESP", city.getCountryCode());
        assertEquals("Baleares", city.getDistrict());
        assertEquals(326993, city.getPopulation());

        verify(pstmt).setString(1, "Baleares");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 6:
     * Verifies that leading and trailing whitespace
     * is removed from the district parameter.
     */
    @Test
    public void getCitiesInDistrictTrimmedParameterTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement11 req11 = new Requirement11(con);

        ArrayList<City> cities =
                req11.getCitiesInDistrict("  Gelderland  ");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Gelderland");

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 7:
     * Verifies that an empty ResultSet returns an empty city list.
     */
    @Test
    public void getCitiesInDistrictEmptyResultTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement11 req11 = new Requirement11(con);

        ArrayList<City> cities =
                req11.getCitiesInDistrict("UnknownDistrict");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "UnknownDistrict");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 8:
     * Verifies SQL exception handling during statement preparation.
     */
    @Test
    public void getCitiesInDistrictPrepareStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(
                        new SQLException("Statement preparation failed")
                );

        Requirement11 req11 = new Requirement11(con);

        ArrayList<City> cities =
                req11.getCitiesInDistrict("Baleares");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(con).prepareStatement(anyString());
    }

    /**
     * Test 9:
     * Verifies SQL exception handling during parameter binding.
     */
    @Test
    public void getCitiesInDistrictSetParameterExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid district parameter"))
                .when(pstmt)
                .setString(1, "Baleares");

        Requirement11 req11 = new Requirement11(con);

        ArrayList<City> cities =
                req11.getCitiesInDistrict("Baleares");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Baleares");
        verify(pstmt, never()).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 10:
     * Verifies SQL exception handling during query execution.
     */
    @Test
    public void getCitiesInDistrictQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement11 req11 = new Requirement11(con);

        ArrayList<City> cities =
                req11.getCitiesInDistrict("Baleares");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Baleares");
        verify(pstmt).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 11:
     * Verifies SQL exception handling while reading ResultSet records.
     */
    @Test
    public void getCitiesInDistrictResultSetExceptionTest()
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

        Requirement11 req11 = new Requirement11(con);

        ArrayList<City> cities =
                req11.getCitiesInDistrict("Baleares");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).next();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 12:
     * Verifies SQL exception handling when mapping city fields.
     */
    @Test
    public void getCitiesInDistrictFieldMappingExceptionTest()
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

        Requirement11 req11 = new Requirement11(con);

        ArrayList<City> cities =
                req11.getCitiesInDistrict("Baleares");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).getInt("ID");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 13:
     * Verifies SQL exception handling when closing the ResultSet.
     */
    @Test
    public void getCitiesInDistrictResultSetCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("ResultSet close failed"))
                .when(rset)
                .close();

        Requirement11 req11 = new Requirement11(con);

        ArrayList<City> cities =
                req11.getCitiesInDistrict("Baleares");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 14:
     * Verifies that printCities handles null lists.
     */
    @Test
    public void printCitiesNullListTest() {

        Requirement11 req11 = new Requirement11(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req11.printCities(null, "UnknownDistrict");

            assertTrue(
                    output.toString().contains(
                            "No cities found for district: UnknownDistrict"
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 15:
     * Verifies that printCities handles empty lists.
     */
    @Test
    public void printCitiesEmptyListTest() {

        Requirement11 req11 = new Requirement11(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req11.printCities(new ArrayList<>(), "UnknownDistrict");

            assertTrue(
                    output.toString().contains(
                            "No cities found for district: UnknownDistrict"
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 16:
     * Verifies that printCities formats valid City records correctly.
     */
    @Test
    public void printCitiesValidListTest() {

        Requirement11 req11 = new Requirement11(null);

        ArrayList<City> cities = new ArrayList<>();

        City city = new City();

        city.setId(1);
        city.setName("Palma de Mallorca");
        city.setCountryCode("ESP");
        city.setDistrict("Baleares");
        city.setPopulation(326993);

        cities.add(city);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req11.printCities(cities, "Baleares");

            String printed = output.toString();

            assertTrue(printed.contains("REQ-11"));
            assertTrue(printed.contains("Baleares"));
            assertTrue(printed.contains("Palma de Mallorca"));
            assertTrue(printed.contains("ESP"));
            assertTrue(printed.contains("326993"));
            assertTrue(printed.contains("Total cities displayed: 1"));

        } finally {
            System.setOut(originalOut);
        }
    }
}