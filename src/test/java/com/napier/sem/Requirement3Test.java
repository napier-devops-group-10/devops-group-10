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
 * Unit tests for REQ-03:
 * All countries in a region organised by largest population to smallest.
 *
 * Tests input validation, database connectivity, successful SQL queries,
 * missing capital information, empty results, SQL exceptions,
 * resource management and formatted report output.
 *
 * Mockito is used to simulate database interactions without requiring
 * a running MySQL database.
 */
public class Requirement3Test {

    /**
     * Test 1:
     * Verifies that a null database connection returns an empty list.
     */
    @Test
    public void getCountriesByRegionNullConnectionTest() {

        Requirement3 req3 = new Requirement3(null);

        ArrayList<Country> countries =
                req3.getCountriesByRegion("Western Europe");

        assertNotNull(countries);
        assertTrue(countries.isEmpty());
    }

    /**
     * Test 2:
     * Verifies that null, empty and whitespace-only regions
     * return an empty list.
     */
    @Test
    public void getCountriesByRegionInvalidParamTest() {

        Requirement3 req3 = new Requirement3(null);

        assertTrue(req3.getCountriesByRegion(null).isEmpty());
        assertTrue(req3.getCountriesByRegion("").isEmpty());
        assertTrue(req3.getCountriesByRegion("   ").isEmpty());
    }

    /**
     * Test 3:
     * Verifies successful retrieval of multiple countries
     * from a specified region.
     */
    @Test
    public void getCountriesByRegionSuccessTest() throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        // Simulate two database records.
        when(rset.next()).thenReturn(true, true, false);

        when(rset.getString("Code"))
                .thenReturn("DEU", "FRA");

        when(rset.getString("Name"))
                .thenReturn("Germany", "France");

        when(rset.getString("Continent"))
                .thenReturn("Europe");

        when(rset.getString("Region"))
                .thenReturn("Western Europe");

        when(rset.getDouble("SurfaceArea"))
                .thenReturn(357022.0, 551500.0);

        when(rset.getInt("Population"))
                .thenReturn(82164700, 59225700);

        when(rset.getString("Capital"))
                .thenReturn("Berlin", "Paris");

        Requirement3 req3 = new Requirement3(con);

        ArrayList<Country> countries =
                req3.getCountriesByRegion("Western Europe");

        assertNotNull(countries);
        assertEquals(2, countries.size());

        // Verify Germany.
        Country germany = countries.get(0);

        assertEquals("DEU", germany.getCode());
        assertEquals("Germany", germany.getName());
        assertEquals("Europe", germany.getContinent());
        assertEquals("Western Europe", germany.getRegion());
        assertEquals(357022.0, germany.getSurfaceArea(), 0.001);
        assertEquals(82164700, germany.getPopulation());
        assertEquals("Berlin", germany.getCapital());

        // Verify France.
        Country france = countries.get(1);

        assertEquals("FRA", france.getCode());
        assertEquals("France", france.getName());
        assertEquals("Europe", france.getContinent());
        assertEquals("Western Europe", france.getRegion());
        assertEquals(551500.0, france.getSurfaceArea(), 0.001);
        assertEquals(59225700, france.getPopulation());
        assertEquals("Paris", france.getCapital());

        // Verify descending population in returned records.
        assertTrue(
                germany.getPopulation() >= france.getPopulation()
        );

        // Verify region parameter binding.
        verify(pstmt).setString(1, "Western Europe");

        // Verify SQL ordering.
        verify(con).prepareStatement(
                argThat(sql ->
                        sql.contains("WHERE c.Region = ?")
                                && sql.contains("ORDER BY c.Population DESC")
                )
        );

        // Verify database resources are closed.
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 4:
     * Verifies that leading and trailing spaces are removed
     * from the region parameter before executing the query.
     */
    @Test
    public void getCountriesByRegionTrimmedRegionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);
        when(rset.next()).thenReturn(false);

        Requirement3 req3 = new Requirement3(con);

        ArrayList<Country> countries =
                req3.getCountriesByRegion("  Caribbean  ");

        assertNotNull(countries);
        assertTrue(countries.isEmpty());

        // Verify the trimmed parameter is passed to SQL.
        verify(pstmt).setString(1, "Caribbean");

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 5:
     * Verifies that a missing capital city is represented
     * by the default value "N/A".
     */
    @Test
    public void getCountriesByRegionNullCapitalTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getString("Code")).thenReturn("AAA");
        when(rset.getString("Name")).thenReturn("Test Country");
        when(rset.getString("Continent")).thenReturn("Europe");
        when(rset.getString("Region")).thenReturn("Western Europe");
        when(rset.getDouble("SurfaceArea")).thenReturn(1000.0);
        when(rset.getInt("Population")).thenReturn(5000);

        // Simulate a missing capital.
        when(rset.getString("Capital")).thenReturn((String) null);

        Requirement3 req3 = new Requirement3(con);

        ArrayList<Country> countries =
                req3.getCountriesByRegion("Western Europe");

        assertEquals(1, countries.size());
        assertEquals("AAA", countries.get(0).getCode());
        assertEquals("Test Country", countries.get(0).getName());
        assertEquals("N/A", countries.get(0).getCapital());

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 6:
     * Verifies that an empty database result
     * produces an empty country list.
     */
    @Test
    public void getCountriesByRegionEmptyResultTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(false);

        Requirement3 req3 = new Requirement3(con);

        ArrayList<Country> countries =
                req3.getCountriesByRegion("Caribbean");

        assertNotNull(countries);
        assertTrue(countries.isEmpty());

        verify(pstmt).setString(1, "Caribbean");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 7:
     * Verifies SQL exception handling when preparing
     * the database statement.
     */
    @Test
    public void getCountriesByRegionPrepareStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(new SQLException("Statement preparation failed"));

        Requirement3 req3 = new Requirement3(con);

        ArrayList<Country> countries =
                req3.getCountriesByRegion("Western Europe");

        assertNotNull(countries);
        assertTrue(countries.isEmpty());

        verify(con).prepareStatement(anyString());
    }

    /**
     * Test 8:
     * Verifies SQL exception handling during query execution.
     */
    @Test
    public void getCountriesByRegionQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(new SQLException("Query execution failed"));

        Requirement3 req3 = new Requirement3(con);

        ArrayList<Country> countries =
                req3.getCountriesByRegion("Western Europe");

        assertNotNull(countries);
        assertTrue(countries.isEmpty());

        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt).close();
    }

    /**
     * Test 9:
     * Verifies SQL exception handling when setting
     * the region parameter.
     */
    @Test
    public void getCountriesByRegionSetParameterExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid region parameter"))
                .when(pstmt).setString(1, "Caribbean");

        Requirement3 req3 = new Requirement3(con);

        ArrayList<Country> countries =
                req3.getCountriesByRegion("Caribbean");

        assertNotNull(countries);
        assertTrue(countries.isEmpty());

        verify(pstmt).setString(1, "Caribbean");
        verify(pstmt, never()).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 10:
     * Verifies formatted output when the country list
     * is null or empty.
     */
    @Test
    public void printCountriesEmptyListTest() {

        Requirement3 req3 = new Requirement3(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req3.printCountries(null, "Western Europe");
            req3.printCountries(new ArrayList<>(), "Western Europe");

            String printed = output.toString();

            assertTrue(
                    printed.contains(
                            "No countries found for region: Western Europe"
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 11:
     * Verifies formatted output for a valid country list.
     */
    @Test
    public void printCountriesValidListTest() {

        Requirement3 req3 = new Requirement3(null);

        ArrayList<Country> countries = new ArrayList<>();

        Country country = new Country();

        country.setCode("FRA");
        country.setName("France");
        country.setContinent("Europe");
        country.setRegion("Western Europe");
        country.setPopulation(59225700);
        country.setCapital("Paris");

        countries.add(country);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req3.printCountries(countries, "Western Europe");

            String printed = output.toString();

            assertTrue(printed.contains("REQ-03"));
            assertTrue(printed.contains("Western Europe"));
            assertTrue(printed.contains("FRA"));
            assertTrue(printed.contains("France"));
            assertTrue(printed.contains("Europe"));
            assertTrue(printed.contains("59225700"));
            assertTrue(printed.contains("Paris"));

            assertTrue(
                    printed.contains("Total records displayed: 1")
            );

        } finally {
            System.setOut(originalOut);
        }
    }
}