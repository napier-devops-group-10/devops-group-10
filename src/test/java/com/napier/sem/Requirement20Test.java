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
 * Unit tests for REQ-20:
 * The top N populated capital cities in the world.
 *
 * Tests input validation, database connectivity, SQL execution,
 * city data mapping, empty results, exception handling,
 * JDBC resource management and formatted report output.
 *
 * Mockito simulates JDBC operations without requiring
 * an active MySQL database connection.
 */
public class Requirement20Test {

    /**
     * Test 1:
     * Verifies that a null database connection returns an empty list.
     */
    @Test
    public void getTopNCapitalCitiesWorldNullConnectionTest() {

        Requirement20 req20 = new Requirement20(null);

        ArrayList<City> result =
                req20.getTopNCapitalCitiesWorld(10);

        assertNotNull(result);

        assertTrue(
                result.isEmpty(),
                "Expected empty list when database connection is null"
        );
    }

    /**
     * Test 2:
     * Verifies that zero and negative N values are rejected.
     */
    @Test
    public void getTopNCapitalCitiesWorldInvalidNTest() {

        Connection con = mock(Connection.class);
        Requirement20 req20 = new Requirement20(con);

        assertTrue(
                req20.getTopNCapitalCitiesWorld(0).isEmpty()
        );

        assertTrue(
                req20.getTopNCapitalCitiesWorld(-5).isEmpty()
        );

        // Invalid input must not access the database.
        verifyNoInteractions(con);
    }

    /**
     * Test 3:
     * Verifies successful retrieval of multiple capital cities.
     *
     * Checks city field mapping, SQL query structure,
     * population ordering, LIMIT binding and JDBC closure.
     */
    @Test
    public void getTopNCapitalCitiesWorldSuccessTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, true, false);

        when(rset.getInt("ID"))
                .thenReturn(2331, 2974);

        when(rset.getString("Name"))
                .thenReturn("Seoul", "Paris");

        when(rset.getString("CountryCode"))
                .thenReturn("KOR", "FRA");

        when(rset.getString("District"))
                .thenReturn("Seoul", "Île-de-France");

        when(rset.getInt("Population"))
                .thenReturn(9981619, 2125246);

        Requirement20 req20 = new Requirement20(con);

        ArrayList<City> cities =
                req20.getTopNCapitalCitiesWorld(2);

        assertNotNull(cities);
        assertEquals(2, cities.size());

        // Verify the first capital city.
        City seoul = cities.get(0);

        assertEquals(2331, seoul.getId());
        assertEquals("Seoul", seoul.getName());
        assertEquals("KOR", seoul.getCountryCode());
        assertEquals("Seoul", seoul.getDistrict());
        assertEquals(9981619, seoul.getPopulation());

        // Verify the second capital city.
        City paris = cities.get(1);

        assertEquals(2974, paris.getId());
        assertEquals("Paris", paris.getName());
        assertEquals("FRA", paris.getCountryCode());
        assertEquals("Île-de-France", paris.getDistrict());
        assertEquals(2125246, paris.getPopulation());

        // Verify the mocked records are in descending order.
        assertTrue(
                seoul.getPopulation() >= paris.getPopulation()
        );

        // Verify the SQL query structure.
        verify(con).prepareStatement(
                argThat(sql ->
                        sql.contains(
                                "SELECT ct.ID, ct.Name, ct.CountryCode, ct.District, ct.Population"
                        )
                                && sql.contains("FROM city ct")
                                && sql.contains(
                                "JOIN country c ON ct.ID = c.Capital"
                        )
                                && sql.contains(
                                "ORDER BY ct.Population DESC"
                        )
                                && sql.contains("LIMIT ?")
                )
        );

        // Verify LIMIT parameter.
        verify(pstmt).setInt(1, 2);
        verify(pstmt).executeQuery();

        // Verify all rows were processed.
        verify(rset, times(3)).next();

        // Verify JDBC resources were closed.
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 4:
     * Verifies successful retrieval of a single capital city.
     */
    @Test
    public void getTopNCapitalCitiesWorldSingleCityTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(2331);
        when(rset.getString("Name")).thenReturn("Seoul");
        when(rset.getString("CountryCode")).thenReturn("KOR");
        when(rset.getString("District")).thenReturn("Seoul");
        when(rset.getInt("Population")).thenReturn(9981619);

        Requirement20 req20 = new Requirement20(con);

        ArrayList<City> cities =
                req20.getTopNCapitalCitiesWorld(1);

        assertNotNull(cities);
        assertEquals(1, cities.size());

        City city = cities.get(0);

        assertEquals(2331, city.getId());
        assertEquals("Seoul", city.getName());
        assertEquals("KOR", city.getCountryCode());
        assertEquals("Seoul", city.getDistrict());
        assertEquals(9981619, city.getPopulation());

        verify(pstmt).setInt(1, 1);

        verify(rset, times(2)).next();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 5:
     * Verifies that a large positive N is accepted.
     */
    @Test
    public void getTopNCapitalCitiesWorldLargeLimitTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(2331);
        when(rset.getString("Name")).thenReturn("Seoul");
        when(rset.getString("CountryCode")).thenReturn("KOR");
        when(rset.getString("District")).thenReturn("Seoul");
        when(rset.getInt("Population")).thenReturn(9981619);

        Requirement20 req20 = new Requirement20(con);

        ArrayList<City> cities =
                req20.getTopNCapitalCitiesWorld(1000);

        assertNotNull(cities);
        assertEquals(1, cities.size());
        assertEquals("Seoul", cities.get(0).getName());

        verify(pstmt).setInt(1, 1000);
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 6:
     * Verifies that an empty ResultSet returns an empty list.
     */
    @Test
    public void getTopNCapitalCitiesWorldEmptyResultTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement20 req20 = new Requirement20(con);

        ArrayList<City> cities =
                req20.getTopNCapitalCitiesWorld(5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setInt(1, 5);
        verify(rset).next();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 7:
     * Verifies SQL exception handling during statement preparation.
     */
    @Test
    public void getTopNCapitalCitiesWorldPrepareStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(
                        new SQLException("Statement preparation failed")
                );

        Requirement20 req20 = new Requirement20(con);

        ArrayList<City> cities =
                req20.getTopNCapitalCitiesWorld(5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(con).prepareStatement(anyString());
    }

    /**
     * Test 8:
     * Verifies SQL exception handling when binding the LIMIT parameter.
     */
    @Test
    public void getTopNCapitalCitiesWorldSetLimitExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid LIMIT parameter"))
                .when(pstmt)
                .setInt(1, 5);

        Requirement20 req20 = new Requirement20(con);

        ArrayList<City> cities =
                req20.getTopNCapitalCitiesWorld(5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setInt(1, 5);
        verify(pstmt, never()).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 9:
     * Verifies SQL exception handling during query execution.
     */
    @Test
    public void getTopNCapitalCitiesWorldQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement20 req20 = new Requirement20(con);

        ArrayList<City> cities =
                req20.getTopNCapitalCitiesWorld(5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setInt(1, 5);
        verify(pstmt).executeQuery();
        verify(pstmt).close();
    }

    /**
     * Test 10:
     * Verifies SQL exception handling while reading ResultSet records.
     */
    @Test
    public void getTopNCapitalCitiesWorldResultSetExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next())
                .thenThrow(
                        new SQLException("Failed to read capital cities")
                );

        Requirement20 req20 = new Requirement20(con);

        ArrayList<City> cities =
                req20.getTopNCapitalCitiesWorld(5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).next();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 11:
     * Verifies SQL exception handling when reading the city ID.
     */
    @Test
    public void getTopNCapitalCitiesWorldIdMappingExceptionTest()
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

        Requirement20 req20 = new Requirement20(con);

        ArrayList<City> cities =
                req20.getTopNCapitalCitiesWorld(5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).getInt("ID");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 12:
     * Verifies SQL exception handling when reading the city name.
     */
    @Test
    public void getTopNCapitalCitiesWorldNameMappingExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true);

        when(rset.getInt("ID")).thenReturn(2331);

        when(rset.getString("Name"))
                .thenThrow(
                        new SQLException("Unable to read city name")
                );

        Requirement20 req20 = new Requirement20(con);

        ArrayList<City> cities =
                req20.getTopNCapitalCitiesWorld(5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).getInt("ID");
        verify(rset).getString("Name");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 13:
     * Verifies SQL exception handling when reading the population.
     */
    @Test
    public void getTopNCapitalCitiesWorldPopulationMappingExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true);

        when(rset.getInt("Population"))
                .thenThrow(
                        new SQLException("Unable to read population")
                );

        Requirement20 req20 = new Requirement20(con);

        ArrayList<City> cities =
                req20.getTopNCapitalCitiesWorld(5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).getInt("Population");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 14:
     * Verifies SQL exception handling when closing the ResultSet.
     */
    @Test
    public void getTopNCapitalCitiesWorldResultSetCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("ResultSet close failed"))
                .when(rset)
                .close();

        Requirement20 req20 = new Requirement20(con);

        ArrayList<City> cities =
                req20.getTopNCapitalCitiesWorld(5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 15:
     * Verifies SQL exception handling when closing the PreparedStatement.
     */
    @Test
    public void getTopNCapitalCitiesWorldStatementCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("PreparedStatement close failed"))
                .when(pstmt)
                .close();

        Requirement20 req20 = new Requirement20(con);

        ArrayList<City> cities =
                req20.getTopNCapitalCitiesWorld(5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 16:
     * Verifies that previously retrieved records are retained
     * if a later ResultSet read fails.
     */
    @Test
    public void getTopNCapitalCitiesWorldPartialResultsExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next())
                .thenReturn(true)
                .thenThrow(
                        new SQLException("Error reading next record")
                );

        when(rset.getInt("ID")).thenReturn(2331);
        when(rset.getString("Name")).thenReturn("Seoul");
        when(rset.getString("CountryCode")).thenReturn("KOR");
        when(rset.getString("District")).thenReturn("Seoul");
        when(rset.getInt("Population")).thenReturn(9981619);

        Requirement20 req20 = new Requirement20(con);

        ArrayList<City> cities =
                req20.getTopNCapitalCitiesWorld(5);

        assertNotNull(cities);
        assertEquals(1, cities.size());

        assertEquals("Seoul", cities.get(0).getName());
        assertEquals(9981619, cities.get(0).getPopulation());

        verify(rset, times(2)).next();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 17:
     * Verifies that printCapitalCities handles null and empty lists.
     *
     * Preserves the original empty-list test scenario.
     */
    @Test
    public void printCapitalCitiesEmptyListTest() {

        Requirement20 req20 = new Requirement20(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req20.printCapitalCities(null, 5);
            req20.printCapitalCities(new ArrayList<>(), 5);

            String printed = output.toString();

            String expected =
                    "No capital cities found for Top 5 capital cities in the world.";

            assertEquals(
                    2,
                    printed.split(
                            java.util.regex.Pattern.quote(expected),
                            -1
                    ).length - 1
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 18:
     * Verifies that printCapitalCities formats valid records correctly.
     *
     * Preserves and expands the original valid-list test scenario.
     */
    @Test
    public void printCapitalCitiesValidListTest() {

        Requirement20 req20 = new Requirement20(null);

        ArrayList<City> cities = new ArrayList<>();

        City city = new City();

        city.setId(2331);
        city.setName("Seoul");
        city.setCountryCode("KOR");
        city.setDistrict("Seoul");
        city.setPopulation(9981619);

        cities.add(city);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req20.printCapitalCities(cities, 1);

            String printed = output.toString();

            assertTrue(
                    printed.contains(
                            "REQ-20: Top 1 Populated Capital Cities in the World"
                    )
            );

            assertTrue(printed.contains("Name"));
            assertTrue(printed.contains("Country Code"));
            assertTrue(printed.contains("District"));
            assertTrue(printed.contains("Population"));

            assertTrue(printed.contains("Seoul"));
            assertTrue(printed.contains("KOR"));
            assertTrue(printed.contains("9981619"));

            assertTrue(
                    printed.contains(
                            "Total capital cities displayed: 1"
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }
}