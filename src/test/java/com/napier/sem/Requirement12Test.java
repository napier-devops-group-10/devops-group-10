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
 * Unit tests for REQ-12:
 * The top N populated cities in the world.
 *
 * Tests input validation, database connectivity, SQL execution,
 * city data mapping, empty results, exception handling,
 * resource management and formatted report output.
 *
 * Mockito is used to simulate JDBC database operations
 * without requiring an active MySQL connection.
 */
public class Requirement12Test {

    /**
     * Test 1:
     * Verifies that a null database connection returns an empty list.
     */
    @Test
    public void getTopNCitiesWorldNullConnectionTest() {

        Requirement12 req12 = new Requirement12(null);

        ArrayList<City> result =
                req12.getTopNCitiesWorld(10);

        assertNotNull(result);
        assertTrue(
                result.isEmpty(),
                "Expected empty list when database connection is null"
        );
    }

    /**
     * Test 2:
     * Verifies that zero is rejected as an invalid N value.
     */
    @Test
    public void getTopNCitiesWorldZeroParamTest() {

        Connection con = mock(Connection.class);
        Requirement12 req12 = new Requirement12(con);

        ArrayList<City> result =
                req12.getTopNCitiesWorld(0);

        assertNotNull(result);
        assertTrue(
                result.isEmpty(),
                "Expected empty list when N is zero"
        );

        verifyNoInteractions(con);
    }

    /**
     * Test 3:
     * Verifies that negative N values are rejected.
     */
    @Test
    public void getTopNCitiesWorldNegativeParamTest() {

        Connection con = mock(Connection.class);
        Requirement12 req12 = new Requirement12(con);

        ArrayList<City> result =
                req12.getTopNCitiesWorld(-5);

        assertNotNull(result);
        assertTrue(
                result.isEmpty(),
                "Expected empty list when N is negative"
        );

        verifyNoInteractions(con);
    }

    /**
     * Test 4:
     * Verifies successful retrieval of multiple cities.
     *
     * Checks city data mapping, SQL population ordering,
     * LIMIT parameter binding and resource closure.
     */
    @Test
    public void getTopNCitiesWorldSuccessTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, true, false);

        when(rset.getInt("ID"))
                .thenReturn(1024, 2331);

        when(rset.getString("Name"))
                .thenReturn("Mumbai (Bombay)", "Shanghai");

        when(rset.getString("CountryCode"))
                .thenReturn("IND", "CHN");

        when(rset.getString("District"))
                .thenReturn("Maharashtra", "Shanghai");

        when(rset.getInt("Population"))
                .thenReturn(10500000, 9696300);

        Requirement12 req12 = new Requirement12(con);

        ArrayList<City> cities =
                req12.getTopNCitiesWorld(2);

        assertNotNull(cities);
        assertEquals(2, cities.size());

        // Verify the first city.
        City mumbai = cities.get(0);

        assertEquals(1024, mumbai.getId());
        assertEquals("Mumbai (Bombay)", mumbai.getName());
        assertEquals("IND", mumbai.getCountryCode());
        assertEquals("Maharashtra", mumbai.getDistrict());
        assertEquals(10500000, mumbai.getPopulation());

        // Verify the second city.
        City shanghai = cities.get(1);

        assertEquals(2331, shanghai.getId());
        assertEquals("Shanghai", shanghai.getName());
        assertEquals("CHN", shanghai.getCountryCode());
        assertEquals("Shanghai", shanghai.getDistrict());
        assertEquals(9696300, shanghai.getPopulation());

        // Verify that the mocked records are in descending order.
        assertTrue(
                mumbai.getPopulation() >= shanghai.getPopulation()
        );

        // Verify the SQL query structure.
        verify(con).prepareStatement(
                argThat(sql ->
                        sql.contains(
                                "SELECT ID, Name, CountryCode, District, Population"
                        )
                                && sql.contains("FROM city")
                                && sql.contains("ORDER BY Population DESC")
                                && sql.contains("LIMIT ?")
                )
        );

        // Verify that the requested limit is bound correctly.
        verify(pstmt).setInt(1, 2);
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
    public void getTopNCitiesWorldSingleCityTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(1024);
        when(rset.getString("Name"))
                .thenReturn("Mumbai (Bombay)");
        when(rset.getString("CountryCode"))
                .thenReturn("IND");
        when(rset.getString("District"))
                .thenReturn("Maharashtra");
        when(rset.getInt("Population"))
                .thenReturn(10500000);

        Requirement12 req12 = new Requirement12(con);

        ArrayList<City> cities =
                req12.getTopNCitiesWorld(1);

        assertNotNull(cities);
        assertEquals(1, cities.size());

        City city = cities.get(0);

        assertEquals(1024, city.getId());
        assertEquals("Mumbai (Bombay)", city.getName());
        assertEquals("IND", city.getCountryCode());
        assertEquals("Maharashtra", city.getDistrict());
        assertEquals(10500000, city.getPopulation());

        verify(pstmt).setInt(1, 1);
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 6:
     * Verifies that a positive N larger than the available
     * records is accepted and returns the available cities.
     */
    @Test
    public void getTopNCitiesWorldLargeLimitTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(1024);
        when(rset.getString("Name"))
                .thenReturn("Mumbai (Bombay)");
        when(rset.getString("CountryCode"))
                .thenReturn("IND");
        when(rset.getString("District"))
                .thenReturn("Maharashtra");
        when(rset.getInt("Population"))
                .thenReturn(10500000);

        Requirement12 req12 = new Requirement12(con);

        ArrayList<City> cities =
                req12.getTopNCitiesWorld(1000);

        assertNotNull(cities);
        assertEquals(1, cities.size());
        assertEquals("Mumbai (Bombay)", cities.get(0).getName());

        verify(pstmt).setInt(1, 1000);
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 7:
     * Verifies that an empty ResultSet returns an empty list.
     */
    @Test
    public void getTopNCitiesWorldEmptyResultTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement12 req12 = new Requirement12(con);

        ArrayList<City> cities =
                req12.getTopNCitiesWorld(5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setInt(1, 5);
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 8:
     * Verifies SQL exception handling during statement preparation.
     */
    @Test
    public void getTopNCitiesWorldPrepareStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(
                        new SQLException("Statement preparation failed")
                );

        Requirement12 req12 = new Requirement12(con);

        ArrayList<City> cities =
                req12.getTopNCitiesWorld(5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(con).prepareStatement(anyString());
    }

    /**
     * Test 9:
     * Verifies SQL exception handling during LIMIT parameter binding.
     */
    @Test
    public void getTopNCitiesWorldSetParameterExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid LIMIT parameter"))
                .when(pstmt)
                .setInt(1, 5);

        Requirement12 req12 = new Requirement12(con);

        ArrayList<City> cities =
                req12.getTopNCitiesWorld(5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setInt(1, 5);
        verify(pstmt, never()).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 10:
     * Verifies SQL exception handling during query execution.
     */
    @Test
    public void getTopNCitiesWorldQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement12 req12 = new Requirement12(con);

        ArrayList<City> cities =
                req12.getTopNCitiesWorld(5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setInt(1, 5);
        verify(pstmt).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 11:
     * Verifies SQL exception handling while reading ResultSet records.
     */
    @Test
    public void getTopNCitiesWorldResultSetExceptionTest()
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

        Requirement12 req12 = new Requirement12(con);

        ArrayList<City> cities =
                req12.getTopNCitiesWorld(5);

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
    public void getTopNCitiesWorldFieldMappingExceptionTest()
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

        Requirement12 req12 = new Requirement12(con);

        ArrayList<City> cities =
                req12.getTopNCitiesWorld(5);

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
    public void getTopNCitiesWorldResultSetCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("ResultSet close failed"))
                .when(rset)
                .close();

        Requirement12 req12 = new Requirement12(con);

        ArrayList<City> cities =
                req12.getTopNCitiesWorld(5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 14:
     * Verifies SQL exception handling when closing the PreparedStatement.
     */
    @Test
    public void getTopNCitiesWorldStatementCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("PreparedStatement close failed"))
                .when(pstmt)
                .close();

        Requirement12 req12 = new Requirement12(con);

        ArrayList<City> cities =
                req12.getTopNCitiesWorld(5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 15:
     * Verifies that printCities handles a null list.
     */
    @Test
    public void printCitiesNullListTest() {

        Requirement12 req12 = new Requirement12(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req12.printCities(null, 5);

            assertTrue(
                    output.toString().contains(
                            "No cities found for Top 5 cities in the world."
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 16:
     * Verifies that printCities handles an empty list.
     */
    @Test
    public void printCitiesEmptyListTest() {

        Requirement12 req12 = new Requirement12(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req12.printCities(new ArrayList<>(), 5);

            assertTrue(
                    output.toString().contains(
                            "No cities found for Top 5 cities in the world."
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 17:
     * Verifies that printCities formats valid City records correctly.
     */
    @Test
    public void printCitiesValidListTest() {

        Requirement12 req12 = new Requirement12(null);

        ArrayList<City> cities = new ArrayList<>();

        City city = new City();

        city.setId(1024);
        city.setName("Mumbai (Bombay)");
        city.setCountryCode("IND");
        city.setDistrict("Maharashtra");
        city.setPopulation(10500000);

        cities.add(city);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req12.printCities(cities, 1);

            String printed = output.toString();

            assertTrue(printed.contains("REQ-12"));
            assertTrue(printed.contains("Top 1"));
            assertTrue(printed.contains("Mumbai (Bombay)"));
            assertTrue(printed.contains("IND"));
            assertTrue(printed.contains("Maharashtra"));
            assertTrue(printed.contains("10500000"));
            assertTrue(printed.contains("Total cities displayed: 1"));

        } finally {
            System.setOut(originalOut);
        }
    }
}