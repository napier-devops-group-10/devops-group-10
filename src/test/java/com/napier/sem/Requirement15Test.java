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
 * Unit tests for REQ-15:
 * The top N populated cities in a country.
 *
 * Tests input validation, database connectivity, SQL execution,
 * city data mapping, empty results, exception handling,
 * JDBC resource management and formatted report output.
 *
 * Mockito is used to simulate database operations without
 * requiring an active MySQL connection.
 */
public class Requirement15Test {

    /**
     * Test 1:
     * Verifies that a null database connection returns an empty list.
     */
    @Test
    public void getTopNCitiesInCountryNullConnectionTest() {

        Requirement15 req15 = new Requirement15(null);

        ArrayList<City> result =
                req15.getTopNCitiesInCountry("Spain", 5);

        assertNotNull(result);

        assertTrue(
                result.isEmpty(),
                "Expected empty list when database connection is null"
        );
    }

    /**
     * Test 2:
     * Verifies that null, empty and whitespace-only
     * country parameters are rejected.
     */
    @Test
    public void getTopNCitiesInCountryInvalidCountryTest() {

        Connection con = mock(Connection.class);
        Requirement15 req15 = new Requirement15(con);

        assertTrue(
                req15.getTopNCitiesInCountry(null, 5).isEmpty()
        );

        assertTrue(
                req15.getTopNCitiesInCountry("", 5).isEmpty()
        );

        assertTrue(
                req15.getTopNCitiesInCountry("   ", 5).isEmpty()
        );

        // Invalid input must not access the database.
        verifyNoInteractions(con);
    }

    /**
     * Test 3:
     * Verifies that zero and negative N values are rejected.
     */
    @Test
    public void getTopNCitiesInCountryInvalidNTest() {

        Connection con = mock(Connection.class);
        Requirement15 req15 = new Requirement15(con);

        assertTrue(
                req15.getTopNCitiesInCountry("Spain", 0).isEmpty()
        );

        assertTrue(
                req15.getTopNCitiesInCountry("France", -4).isEmpty()
        );

        verifyNoInteractions(con);
    }

    /**
     * Test 4:
     * Verifies successful retrieval of multiple cities.
     *
     * Checks city field mapping, SQL country filtering,
     * population ordering, LIMIT binding and JDBC resource closure.
     */
    @Test
    public void getTopNCitiesInCountrySuccessTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, true, false);

        when(rset.getInt("ID"))
                .thenReturn(653, 654);

        when(rset.getString("Name"))
                .thenReturn("Madrid", "Barcelona");

        when(rset.getString("CountryCode"))
                .thenReturn("ESP", "ESP");

        when(rset.getString("District"))
                .thenReturn("Madrid", "Catalonia");

        when(rset.getInt("Population"))
                .thenReturn(2879052, 1503451);

        Requirement15 req15 = new Requirement15(con);

        ArrayList<City> cities =
                req15.getTopNCitiesInCountry("Spain", 2);

        assertNotNull(cities);
        assertEquals(2, cities.size());

        // Verify the first city.
        City madrid = cities.get(0);

        assertEquals(653, madrid.getId());
        assertEquals("Madrid", madrid.getName());
        assertEquals("ESP", madrid.getCountryCode());
        assertEquals("Madrid", madrid.getDistrict());
        assertEquals(2879052, madrid.getPopulation());

        // Verify the second city.
        City barcelona = cities.get(1);

        assertEquals(654, barcelona.getId());
        assertEquals("Barcelona", barcelona.getName());
        assertEquals("ESP", barcelona.getCountryCode());
        assertEquals("Catalonia", barcelona.getDistrict());
        assertEquals(1503451, barcelona.getPopulation());

        // Verify the mocked result sequence.
        assertTrue(
                madrid.getPopulation() >= barcelona.getPopulation()
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
                                && sql.contains("WHERE c.Name = ?")
                                && sql.contains(
                                "ORDER BY ct.Population DESC"
                        )
                                && sql.contains("LIMIT ?")
                )
        );

        // Verify both SQL parameters.
        verify(pstmt).setString(1, "Spain");
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
    public void getTopNCitiesInCountrySingleCityTest()
            throws SQLException {

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

        Requirement15 req15 = new Requirement15(con);

        ArrayList<City> cities =
                req15.getTopNCitiesInCountry("Spain", 1);

        assertNotNull(cities);
        assertEquals(1, cities.size());

        City city = cities.get(0);

        assertEquals(653, city.getId());
        assertEquals("Madrid", city.getName());
        assertEquals("ESP", city.getCountryCode());
        assertEquals("Madrid", city.getDistrict());
        assertEquals(2879052, city.getPopulation());

        verify(pstmt).setString(1, "Spain");
        verify(pstmt).setInt(2, 1);

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 6:
     * Verifies that leading and trailing whitespace
     * is removed from the country parameter.
     */
    @Test
    public void getTopNCitiesInCountryTrimmedParameterTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement15 req15 = new Requirement15(con);

        ArrayList<City> cities =
                req15.getTopNCitiesInCountry("  Spain  ", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Spain");
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
    public void getTopNCitiesInCountryLargeLimitTest()
            throws SQLException {

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

        Requirement15 req15 = new Requirement15(con);

        ArrayList<City> cities =
                req15.getTopNCitiesInCountry("Spain", 1000);

        assertNotNull(cities);
        assertEquals(1, cities.size());
        assertEquals("Madrid", cities.get(0).getName());

        verify(pstmt).setString(1, "Spain");
        verify(pstmt).setInt(2, 1000);

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 8:
     * Verifies that an empty ResultSet returns an empty list.
     */
    @Test
    public void getTopNCitiesInCountryEmptyResultTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement15 req15 = new Requirement15(con);

        ArrayList<City> cities =
                req15.getTopNCitiesInCountry("France", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "France");
        verify(pstmt).setInt(2, 5);

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 9:
     * Verifies SQL exception handling during statement preparation.
     */
    @Test
    public void getTopNCitiesInCountryPrepareStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(
                        new SQLException("Statement preparation failed")
                );

        Requirement15 req15 = new Requirement15(con);

        ArrayList<City> cities =
                req15.getTopNCitiesInCountry("Spain", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(con).prepareStatement(anyString());
    }

    /**
     * Test 10:
     * Verifies SQL exception handling when binding the country parameter.
     */
    @Test
    public void getTopNCitiesInCountrySetCountryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid country parameter"))
                .when(pstmt)
                .setString(1, "Spain");

        Requirement15 req15 = new Requirement15(con);

        ArrayList<City> cities =
                req15.getTopNCitiesInCountry("Spain", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Spain");
        verify(pstmt, never()).setInt(2, 5);
        verify(pstmt, never()).executeQuery();

        verify(pstmt).close();
    }

    /**
     * Test 11:
     * Verifies SQL exception handling when binding the LIMIT parameter.
     */
    @Test
    public void getTopNCitiesInCountrySetLimitExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid LIMIT parameter"))
                .when(pstmt)
                .setInt(2, 5);

        Requirement15 req15 = new Requirement15(con);

        ArrayList<City> cities =
                req15.getTopNCitiesInCountry("Spain", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Spain");
        verify(pstmt).setInt(2, 5);
        verify(pstmt, never()).executeQuery();

        verify(pstmt).close();
    }

    /**
     * Test 12:
     * Verifies SQL exception handling during query execution.
     */
    @Test
    public void getTopNCitiesInCountryQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement15 req15 = new Requirement15(con);

        ArrayList<City> cities =
                req15.getTopNCitiesInCountry("Spain", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Spain");
        verify(pstmt).setInt(2, 5);
        verify(pstmt).executeQuery();

        verify(pstmt).close();
    }

    /**
     * Test 13:
     * Verifies SQL exception handling while reading ResultSet records.
     */
    @Test
    public void getTopNCitiesInCountryResultSetExceptionTest()
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

        Requirement15 req15 = new Requirement15(con);

        ArrayList<City> cities =
                req15.getTopNCitiesInCountry("Spain", 5);

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
    public void getTopNCitiesInCountryFieldMappingExceptionTest()
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

        Requirement15 req15 = new Requirement15(con);

        ArrayList<City> cities =
                req15.getTopNCitiesInCountry("Spain", 5);

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
    public void getTopNCitiesInCountryResultSetCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("ResultSet close failed"))
                .when(rset)
                .close();

        Requirement15 req15 = new Requirement15(con);

        ArrayList<City> cities =
                req15.getTopNCitiesInCountry("Spain", 5);

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
    public void getTopNCitiesInCountryStatementCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("PreparedStatement close failed"))
                .when(pstmt)
                .close();

        Requirement15 req15 = new Requirement15(con);

        ArrayList<City> cities =
                req15.getTopNCitiesInCountry("Spain", 5);

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
    public void getTopNCitiesInCountryPartialResultsExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next())
                .thenReturn(true)
                .thenThrow(new SQLException("Error reading next record"));

        when(rset.getInt("ID")).thenReturn(653);
        when(rset.getString("Name")).thenReturn("Madrid");
        when(rset.getString("CountryCode")).thenReturn("ESP");
        when(rset.getString("District")).thenReturn("Madrid");
        when(rset.getInt("Population")).thenReturn(2879052);

        Requirement15 req15 = new Requirement15(con);

        ArrayList<City> cities =
                req15.getTopNCitiesInCountry("Spain", 5);

        assertNotNull(cities);
        assertEquals(1, cities.size());
        assertEquals("Madrid", cities.get(0).getName());
        assertEquals(2879052, cities.get(0).getPopulation());

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

        Requirement15 req15 = new Requirement15(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req15.printCities(null, "Spain", 5);

            assertTrue(
                    output.toString().contains(
                            "No cities found for Top 5 cities in country: Spain"
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

        Requirement15 req15 = new Requirement15(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req15.printCities(new ArrayList<>(), "Spain", 5);

            assertTrue(
                    output.toString().contains(
                            "No cities found for Top 5 cities in country: Spain"
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

        Requirement15 req15 = new Requirement15(null);

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

            req15.printCities(cities, "Spain", 1);

            String printed = output.toString();

            assertTrue(printed.contains("REQ-15"));
            assertTrue(printed.contains("Top 1"));
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