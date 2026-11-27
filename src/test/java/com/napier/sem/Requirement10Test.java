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
 * Unit tests for REQ-10:
 * All cities in a country organised by largest population to smallest.
 *
 * Covers input validation, SQL queries, city mapping,
 * database errors, resource closure and report formatting.
 */
public class Requirement10Test {

    /**
     * Test 1: A null database connection returns an empty list.
     */
    @Test
    public void getCitiesInCountryNullConnectionTest() {
        Requirement10 req10 = new Requirement10(null);

        ArrayList<City> result = req10.getCitiesInCountry("Spain");

        assertNotNull(result);
        assertTrue(result.isEmpty(),
                "Expected empty list when database connection is null");
    }

    /**
     * Test 2: Invalid country parameters are rejected.
     */
    @Test
    public void getCitiesInCountryInvalidParamTest() {
        Connection con = mock(Connection.class);
        Requirement10 req10 = new Requirement10(con);

        assertTrue(req10.getCitiesInCountry(null).isEmpty());
        assertTrue(req10.getCitiesInCountry("").isEmpty());
        assertTrue(req10.getCitiesInCountry("   ").isEmpty());

        verifyNoInteractions(con);
    }

    /**
     * Test 3: Multiple cities are retrieved and mapped correctly.
     */
    @Test
    public void getCitiesInCountrySuccessTest() throws SQLException {
        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);
        when(rset.next()).thenReturn(true, true, false);

        when(rset.getInt("ID")).thenReturn(653, 655);
        when(rset.getString("Name")).thenReturn("Madrid", "Barcelona");
        when(rset.getString("CountryCode")).thenReturn("ESP", "ESP");
        when(rset.getString("District")).thenReturn("Madrid", "Catalonia");
        when(rset.getInt("Population")).thenReturn(2879052, 1503451);

        Requirement10 req10 = new Requirement10(con);

        ArrayList<City> cities = req10.getCitiesInCountry("Spain");

        assertNotNull(cities);
        assertEquals(2, cities.size());

        City madrid = cities.get(0);
        assertEquals(653, madrid.getId());
        assertEquals("Madrid", madrid.getName());
        assertEquals("ESP", madrid.getCountryCode());
        assertEquals("Madrid", madrid.getDistrict());
        assertEquals(2879052, madrid.getPopulation());

        City barcelona = cities.get(1);
        assertEquals(655, barcelona.getId());
        assertEquals("Barcelona", barcelona.getName());
        assertEquals("ESP", barcelona.getCountryCode());
        assertEquals("Catalonia", barcelona.getDistrict());
        assertEquals(1503451, barcelona.getPopulation());

        assertTrue(madrid.getPopulation() >= barcelona.getPopulation());

        verify(con).prepareStatement(argThat(sql ->
                sql.contains("FROM city ct")
                        && sql.contains(
                        "JOIN country c ON ct.CountryCode = c.Code")
                        && sql.contains("WHERE c.Name = ?")
                        && sql.contains("ORDER BY ct.Population DESC")
        ));

        verify(pstmt).setString(1, "Spain");
        verify(pstmt).executeQuery();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 4: A single city is retrieved correctly.
     */
    @Test
    public void getCitiesInCountrySingleCityTest() throws SQLException {
        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);
        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(653);
        when(rset.getString("Name")).thenReturn("Madrid");
        when(rset.getString("CountryCode")).thenReturn("ESP");
        when(rset.getString("District")).thenReturn("Madrid");
        when(rset.getInt("Population")).thenReturn(2879052);

        Requirement10 req10 = new Requirement10(con);

        ArrayList<City> cities = req10.getCitiesInCountry("Spain");

        assertEquals(1, cities.size());
        assertEquals(653, cities.get(0).getId());
        assertEquals("Madrid", cities.get(0).getName());
        assertEquals("ESP", cities.get(0).getCountryCode());
        assertEquals("Madrid", cities.get(0).getDistrict());
        assertEquals(2879052, cities.get(0).getPopulation());

        verify(pstmt).setString(1, "Spain");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 5: Leading and trailing whitespace is removed.
     */
    @Test
    public void getCitiesInCountryTrimmedParameterTest() throws SQLException {
        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement10 req10 = new Requirement10(con);

        ArrayList<City> cities = req10.getCitiesInCountry("  Spain  ");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Spain");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 6: An empty ResultSet produces an empty list.
     */
    @Test
    public void getCitiesInCountryEmptyResultTest() throws SQLException {
        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement10 req10 = new Requirement10(con);

        ArrayList<City> cities = req10.getCitiesInCountry("France");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "France");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 7: SQLException during statement preparation.
     */
    @Test
    public void getCitiesInCountryPrepareStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(new SQLException("Preparation failed"));

        Requirement10 req10 = new Requirement10(con);

        ArrayList<City> cities = req10.getCitiesInCountry("Spain");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(con).prepareStatement(anyString());
    }

    /**
     * Test 8: SQLException during parameter binding.
     */
    @Test
    public void getCitiesInCountrySetParameterExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Parameter binding failed"))
                .when(pstmt).setString(1, "Spain");

        Requirement10 req10 = new Requirement10(con);

        ArrayList<City> cities = req10.getCitiesInCountry("Spain");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Spain");
        verify(pstmt, never()).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 9: SQLException during query execution.
     */
    @Test
    public void getCitiesInCountryQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(new SQLException("Query failed"));

        Requirement10 req10 = new Requirement10(con);

        ArrayList<City> cities = req10.getCitiesInCountry("Spain");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 10: SQLException while reading ResultSet rows.
     */
    @Test
    public void getCitiesInCountryResultSetExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);
        when(rset.next())
                .thenThrow(new SQLException("ResultSet read failed"));

        Requirement10 req10 = new Requirement10(con);

        ArrayList<City> cities = req10.getCitiesInCountry("Spain");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).next();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 11: SQLException while reading a city field.
     */
    @Test
    public void getCitiesInCountryFieldMappingExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);
        when(rset.next()).thenReturn(true);

        when(rset.getInt("ID"))
                .thenThrow(new SQLException("Unable to read city ID"));

        Requirement10 req10 = new Requirement10(con);

        ArrayList<City> cities = req10.getCitiesInCountry("Spain");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).getInt("ID");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 12: SQLException when closing the ResultSet.
     */
    @Test
    public void getCitiesInCountryResultSetCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("ResultSet close failed"))
                .when(rset).close();

        Requirement10 req10 = new Requirement10(con);

        ArrayList<City> cities = req10.getCitiesInCountry("Spain");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 13: printCities handles a null list.
     */
    @Test
    public void printCitiesNullListTest() {
        Requirement10 req10 = new Requirement10(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req10.printCities(null, "Spain");

            assertTrue(output.toString()
                    .contains("No cities found for country: Spain"));

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 14: printCities handles an empty list.
     */
    @Test
    public void printCitiesEmptyListTest() {
        Requirement10 req10 = new Requirement10(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req10.printCities(new ArrayList<>(), "Spain");

            assertTrue(output.toString()
                    .contains("No cities found for country: Spain"));

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 15: printCities formats valid records correctly.
     */
    @Test
    public void printCitiesValidListTest() {
        Requirement10 req10 = new Requirement10(null);

        ArrayList<City> cities = new ArrayList<>();

        City city = new City();
        city.setId(653);
        city.setName("Madrid");
        city.setCountryCode("ESP");
        city.setDistrict("Madrid");
        city.setPopulation(2879052);

        cities.add(city);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req10.printCities(cities, "Spain");

            String printed = output.toString();

            assertTrue(printed.contains("REQ-10"));
            assertTrue(printed.contains("Spain"));
            assertTrue(printed.contains("Madrid"));
            assertTrue(printed.contains("ESP"));
            assertTrue(printed.contains("2879052"));
            assertTrue(printed.contains("Total cities displayed: 1"));

        } finally {
            System.setOut(originalOut);
        }
    }
}