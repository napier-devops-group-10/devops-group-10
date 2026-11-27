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
 * Unit tests for REQ-16:
 * The top N populated cities in a district.
 *
 * Tests input validation, database connectivity, SQL execution,
 * city data mapping, empty results, exception handling,
 * JDBC resource management and formatted report output.
 *
 * Mockito simulates JDBC operations without requiring
 * an active MySQL database connection.
 */
public class Requirement16Test {

    /**
     * Test 1:
     * Verifies that a null database connection returns an empty list.
     */
    @Test
    public void getTopNCitiesInDistrictNullConnectionTest() {

        Requirement16 req16 = new Requirement16(null);

        ArrayList<City> result =
                req16.getTopNCitiesInDistrict("Gelderland", 5);

        assertNotNull(result);

        assertTrue(
                result.isEmpty(),
                "Expected empty list when database connection is null"
        );
    }

    /**
     * Test 2:
     * Verifies that null, empty and whitespace-only
     * district parameters are rejected.
     */
    @Test
    public void getTopNCitiesInDistrictInvalidDistrictTest() {

        Connection con = mock(Connection.class);
        Requirement16 req16 = new Requirement16(con);

        assertTrue(
                req16.getTopNCitiesInDistrict(null, 5).isEmpty()
        );

        assertTrue(
                req16.getTopNCitiesInDistrict("", 5).isEmpty()
        );

        assertTrue(
                req16.getTopNCitiesInDistrict("   ", 5).isEmpty()
        );

        // Invalid input must not access the database.
        verifyNoInteractions(con);
    }

    /**
     * Test 3:
     * Verifies that zero and negative N values are rejected.
     */
    @Test
    public void getTopNCitiesInDistrictInvalidNTest() {

        Connection con = mock(Connection.class);
        Requirement16 req16 = new Requirement16(con);

        assertTrue(
                req16.getTopNCitiesInDistrict("Gelderland", 0).isEmpty()
        );

        assertTrue(
                req16.getTopNCitiesInDistrict("Baleares", -3).isEmpty()
        );

        verifyNoInteractions(con);
    }

    /**
     * Test 4:
     * Verifies successful retrieval of multiple cities.
     *
     * Checks city field mapping, SQL district filtering,
     * descending population ordering, LIMIT binding
     * and JDBC resource closure.
     */
    @Test
    public void getTopNCitiesInDistrictSuccessTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, true, false);

        when(rset.getInt("ID"))
                .thenReturn(658, 659);

        when(rset.getString("Name"))
                .thenReturn("Palma de Mallorca", "Calvià");

        when(rset.getString("CountryCode"))
                .thenReturn("ESP", "ESP");

        when(rset.getString("District"))
                .thenReturn("Baleares", "Baleares");

        when(rset.getInt("Population"))
                .thenReturn(326993, 40000);

        Requirement16 req16 = new Requirement16(con);

        ArrayList<City> cities =
                req16.getTopNCitiesInDistrict("Baleares", 2);

        assertNotNull(cities);
        assertEquals(2, cities.size());

        // Verify the first city.
        City palma = cities.get(0);

        assertEquals(658, palma.getId());
        assertEquals("Palma de Mallorca", palma.getName());
        assertEquals("ESP", palma.getCountryCode());
        assertEquals("Baleares", palma.getDistrict());
        assertEquals(326993, palma.getPopulation());

        // Verify the second city.
        City calvia = cities.get(1);

        assertEquals(659, calvia.getId());
        assertEquals("Calvià", calvia.getName());
        assertEquals("ESP", calvia.getCountryCode());
        assertEquals("Baleares", calvia.getDistrict());
        assertEquals(40000, calvia.getPopulation());

        // Verify the mocked result sequence.
        assertTrue(
                palma.getPopulation() >= calvia.getPopulation()
        );

        // Verify the SQL query structure.
        verify(con).prepareStatement(
                argThat(sql ->
                        sql.contains(
                                "SELECT ID, Name, CountryCode, District, Population"
                        )
                                && sql.contains("FROM city")
                                && sql.contains("WHERE District = ?")
                                && sql.contains(
                                "ORDER BY Population DESC"
                        )
                                && sql.contains("LIMIT ?")
                )
        );

        // Verify both SQL parameters.
        verify(pstmt).setString(1, "Baleares");
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
    public void getTopNCitiesInDistrictSingleCityTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(658);
        when(rset.getString("Name"))
                .thenReturn("Palma de Mallorca");
        when(rset.getString("CountryCode")).thenReturn("ESP");
        when(rset.getString("District")).thenReturn("Baleares");
        when(rset.getInt("Population")).thenReturn(326993);

        Requirement16 req16 = new Requirement16(con);

        ArrayList<City> cities =
                req16.getTopNCitiesInDistrict("Baleares", 1);

        assertNotNull(cities);
        assertEquals(1, cities.size());

        City city = cities.get(0);

        assertEquals(658, city.getId());
        assertEquals("Palma de Mallorca", city.getName());
        assertEquals("ESP", city.getCountryCode());
        assertEquals("Baleares", city.getDistrict());
        assertEquals(326993, city.getPopulation());

        verify(pstmt).setString(1, "Baleares");
        verify(pstmt).setInt(2, 1);

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 6:
     * Verifies that leading and trailing whitespace
     * is removed from the district parameter.
     */
    @Test
    public void getTopNCitiesInDistrictTrimmedParameterTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement16 req16 = new Requirement16(con);

        ArrayList<City> cities =
                req16.getTopNCitiesInDistrict("  Gelderland  ", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Gelderland");
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
    public void getTopNCitiesInDistrictLargeLimitTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(658);
        when(rset.getString("Name"))
                .thenReturn("Palma de Mallorca");
        when(rset.getString("CountryCode")).thenReturn("ESP");
        when(rset.getString("District")).thenReturn("Baleares");
        when(rset.getInt("Population")).thenReturn(326993);

        Requirement16 req16 = new Requirement16(con);

        ArrayList<City> cities =
                req16.getTopNCitiesInDistrict("Baleares", 1000);

        assertNotNull(cities);
        assertEquals(1, cities.size());
        assertEquals("Palma de Mallorca", cities.get(0).getName());

        verify(pstmt).setString(1, "Baleares");
        verify(pstmt).setInt(2, 1000);

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 8:
     * Verifies that an empty ResultSet returns an empty list.
     */
    @Test
    public void getTopNCitiesInDistrictEmptyResultTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement16 req16 = new Requirement16(con);

        ArrayList<City> cities =
                req16.getTopNCitiesInDistrict("Gelderland", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Gelderland");
        verify(pstmt).setInt(2, 5);

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 9:
     * Verifies SQL exception handling during statement preparation.
     */
    @Test
    public void getTopNCitiesInDistrictPrepareStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(
                        new SQLException("Statement preparation failed")
                );

        Requirement16 req16 = new Requirement16(con);

        ArrayList<City> cities =
                req16.getTopNCitiesInDistrict("Gelderland", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(con).prepareStatement(anyString());
    }

    /**
     * Test 10:
     * Verifies SQL exception handling when binding the district parameter.
     */
    @Test
    public void getTopNCitiesInDistrictSetDistrictExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid district parameter"))
                .when(pstmt)
                .setString(1, "Gelderland");

        Requirement16 req16 = new Requirement16(con);

        ArrayList<City> cities =
                req16.getTopNCitiesInDistrict("Gelderland", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Gelderland");
        verify(pstmt, never()).setInt(2, 5);
        verify(pstmt, never()).executeQuery();

        verify(pstmt).close();
    }

    /**
     * Test 11:
     * Verifies SQL exception handling when binding the LIMIT parameter.
     */
    @Test
    public void getTopNCitiesInDistrictSetLimitExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid LIMIT parameter"))
                .when(pstmt)
                .setInt(2, 5);

        Requirement16 req16 = new Requirement16(con);

        ArrayList<City> cities =
                req16.getTopNCitiesInDistrict("Gelderland", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Gelderland");
        verify(pstmt).setInt(2, 5);
        verify(pstmt, never()).executeQuery();

        verify(pstmt).close();
    }

    /**
     * Test 12:
     * Verifies SQL exception handling during query execution.
     */
    @Test
    public void getTopNCitiesInDistrictQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement16 req16 = new Requirement16(con);

        ArrayList<City> cities =
                req16.getTopNCitiesInDistrict("Gelderland", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Gelderland");
        verify(pstmt).setInt(2, 5);
        verify(pstmt).executeQuery();

        verify(pstmt).close();
    }

    /**
     * Test 13:
     * Verifies SQL exception handling while reading ResultSet records.
     */
    @Test
    public void getTopNCitiesInDistrictResultSetExceptionTest()
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

        Requirement16 req16 = new Requirement16(con);

        ArrayList<City> cities =
                req16.getTopNCitiesInDistrict("Gelderland", 5);

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
    public void getTopNCitiesInDistrictFieldMappingExceptionTest()
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

        Requirement16 req16 = new Requirement16(con);

        ArrayList<City> cities =
                req16.getTopNCitiesInDistrict("Gelderland", 5);

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
    public void getTopNCitiesInDistrictResultSetCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("ResultSet close failed"))
                .when(rset)
                .close();

        Requirement16 req16 = new Requirement16(con);

        ArrayList<City> cities =
                req16.getTopNCitiesInDistrict("Gelderland", 5);

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
    public void getTopNCitiesInDistrictStatementCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("PreparedStatement close failed"))
                .when(pstmt)
                .close();

        Requirement16 req16 = new Requirement16(con);

        ArrayList<City> cities =
                req16.getTopNCitiesInDistrict("Gelderland", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 17:
     * Verifies that valid records already collected are retained
     * when a subsequent ResultSet read fails.
     */
    @Test
    public void getTopNCitiesInDistrictPartialResultsExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next())
                .thenReturn(true)
                .thenThrow(new SQLException("Error reading next record"));

        when(rset.getInt("ID")).thenReturn(658);
        when(rset.getString("Name"))
                .thenReturn("Palma de Mallorca");
        when(rset.getString("CountryCode")).thenReturn("ESP");
        when(rset.getString("District")).thenReturn("Baleares");
        when(rset.getInt("Population")).thenReturn(326993);

        Requirement16 req16 = new Requirement16(con);

        ArrayList<City> cities =
                req16.getTopNCitiesInDistrict("Baleares", 5);

        assertNotNull(cities);
        assertEquals(1, cities.size());
        assertEquals("Palma de Mallorca", cities.get(0).getName());
        assertEquals(326993, cities.get(0).getPopulation());

        verify(rset, times(2)).next();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 18:
     * Verifies that printCities handles a null list.
     */
    @Test
    public void printCitiesNullListTest() {

        Requirement16 req16 = new Requirement16(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req16.printCities(null, "Gelderland", 5);

            assertTrue(
                    output.toString().contains(
                            "No cities found for Top 5 cities in district: Gelderland"
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 19:
     * Verifies that printCities handles an empty list.
     */
    @Test
    public void printCitiesEmptyListTest() {

        Requirement16 req16 = new Requirement16(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req16.printCities(new ArrayList<>(), "Gelderland", 5);

            assertTrue(
                    output.toString().contains(
                            "No cities found for Top 5 cities in district: Gelderland"
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 20:
     * Verifies that printCities formats valid City records correctly.
     */
    @Test
    public void printCitiesValidListTest() {

        Requirement16 req16 = new Requirement16(null);

        ArrayList<City> cities = new ArrayList<>();

        City city = new City();

        city.setId(658);
        city.setName("Palma de Mallorca");
        city.setCountryCode("ESP");
        city.setDistrict("Baleares");
        city.setPopulation(326993);

        cities.add(city);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req16.printCities(cities, "Baleares", 1);

            String printed = output.toString();

            assertTrue(printed.contains("REQ-16"));
            assertTrue(printed.contains("Top 1"));
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