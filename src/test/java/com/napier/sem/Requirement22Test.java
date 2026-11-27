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
 * Unit tests for REQ-22:
 * The top N populated capital cities in a region.
 *
 * Tests input validation, database connectivity, SQL execution,
 * city data mapping, empty results, exception handling,
 * JDBC resource management and formatted report output.
 *
 * Mockito simulates JDBC operations without requiring
 * an active MySQL database connection.
 */
public class Requirement22Test {

    /**
     * Test 1:
     * Verifies that a null database connection returns an empty list.
     */
    @Test
    public void getTopNCapitalCitiesInRegionNullConnectionTest() {

        Requirement22 req22 = new Requirement22(null);

        ArrayList<City> result =
                req22.getTopNCapitalCitiesInRegion("Western Europe", 5);

        assertNotNull(result);

        assertTrue(
                result.isEmpty(),
                "Expected empty list when database connection is null"
        );
    }

    /**
     * Test 2:
     * Verifies that null, empty and whitespace-only
     * region parameters are rejected.
     */
    @Test
    public void getTopNCapitalCitiesInRegionInvalidRegionTest() {

        Connection con = mock(Connection.class);
        Requirement22 req22 = new Requirement22(con);

        assertTrue(
                req22.getTopNCapitalCitiesInRegion(null, 5).isEmpty()
        );

        assertTrue(
                req22.getTopNCapitalCitiesInRegion("", 5).isEmpty()
        );

        assertTrue(
                req22.getTopNCapitalCitiesInRegion("   ", 5).isEmpty()
        );

        // Invalid input must not access the database.
        verifyNoInteractions(con);
    }

    /**
     * Test 3:
     * Verifies that zero and negative N values are rejected.
     */
    @Test
    public void getTopNCapitalCitiesInRegionInvalidNTest() {

        Connection con = mock(Connection.class);
        Requirement22 req22 = new Requirement22(con);

        assertTrue(
                req22.getTopNCapitalCitiesInRegion("Western Europe", 0).isEmpty()
        );

        assertTrue(
                req22.getTopNCapitalCitiesInRegion("Caribbean", -2).isEmpty()
        );

        verifyNoInteractions(con);
    }

    /**
     * Test 4:
     * Verifies successful retrieval of multiple capital cities.
     *
     * Checks city field mapping, SQL structure,
     * region filtering, LIMIT binding and JDBC closure.
     */
    @Test
    public void getTopNCapitalCitiesInRegionSuccessTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, true, false);

        when(rset.getInt("ID"))
                .thenReturn(3068, 2974);

        when(rset.getString("Name"))
                .thenReturn("Berlin", "Paris");

        when(rset.getString("CountryCode"))
                .thenReturn("DEU", "FRA");

        when(rset.getString("District"))
                .thenReturn("Berliini", "Île-de-France");

        when(rset.getInt("Population"))
                .thenReturn(3386667, 2125246);

        Requirement22 req22 = new Requirement22(con);

        ArrayList<City> cities =
                req22.getTopNCapitalCitiesInRegion("Western Europe", 2);

        assertNotNull(cities);
        assertEquals(2, cities.size());

        // Verify the first capital city.
        City berlin = cities.get(0);

        assertEquals(3068, berlin.getId());
        assertEquals("Berlin", berlin.getName());
        assertEquals("DEU", berlin.getCountryCode());
        assertEquals("Berliini", berlin.getDistrict());
        assertEquals(3386667, berlin.getPopulation());

        // Verify the second capital city.
        City paris = cities.get(1);

        assertEquals(2974, paris.getId());
        assertEquals("Paris", paris.getName());
        assertEquals("FRA", paris.getCountryCode());
        assertEquals("Île-de-France", paris.getDistrict());
        assertEquals(2125246, paris.getPopulation());

        // Verify the mocked records are in descending order.
        assertTrue(
                berlin.getPopulation() >= paris.getPopulation()
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
                                && sql.contains("WHERE c.Region = ?")
                                && sql.contains(
                                "ORDER BY ct.Population DESC"
                        )
                                && sql.contains("LIMIT ?")
                )
        );

        // Verify SQL parameters.
        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt).setInt(2, 2);
        verify(pstmt).executeQuery();

        // Verify all rows were processed.
        verify(rset, times(3)).next();

        // Verify JDBC resources were closed.
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 5:
     * Verifies successful retrieval of a single capital city.
     */
    @Test
    public void getTopNCapitalCitiesInRegionSingleCityTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(3068);
        when(rset.getString("Name")).thenReturn("Berlin");
        when(rset.getString("CountryCode")).thenReturn("DEU");
        when(rset.getString("District")).thenReturn("Berliini");
        when(rset.getInt("Population")).thenReturn(3386667);

        Requirement22 req22 = new Requirement22(con);

        ArrayList<City> cities =
                req22.getTopNCapitalCitiesInRegion("Western Europe", 1);

        assertNotNull(cities);
        assertEquals(1, cities.size());

        City city = cities.get(0);

        assertEquals(3068, city.getId());
        assertEquals("Berlin", city.getName());
        assertEquals("DEU", city.getCountryCode());
        assertEquals("Berliini", city.getDistrict());
        assertEquals(3386667, city.getPopulation());

        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt).setInt(2, 1);

        verify(rset, times(2)).next();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 6:
     * Verifies that whitespace is removed from the region parameter.
     */
    @Test
    public void getTopNCapitalCitiesInRegionTrimmedParameterTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement22 req22 = new Requirement22(con);

        ArrayList<City> cities =
                req22.getTopNCapitalCitiesInRegion("  Western Europe  ", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt).setInt(2, 5);
        verify(pstmt).executeQuery();

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 7:
     * Verifies that a large positive N is accepted.
     */
    @Test
    public void getTopNCapitalCitiesInRegionLargeLimitTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement22 req22 = new Requirement22(con);

        ArrayList<City> cities =
                req22.getTopNCapitalCitiesInRegion("Caribbean", 1000);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Caribbean");
        verify(pstmt).setInt(2, 1000);

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 8:
     * Verifies that an empty ResultSet returns an empty list.
     */
    @Test
    public void getTopNCapitalCitiesInRegionEmptyResultTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement22 req22 = new Requirement22(con);

        ArrayList<City> cities =
                req22.getTopNCapitalCitiesInRegion("Western Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt).setInt(2, 5);
        verify(rset).next();

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 9:
     * Verifies SQL exception handling during statement preparation.
     */
    @Test
    public void getTopNCapitalCitiesInRegionPrepareStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(
                        new SQLException("Statement preparation failed")
                );

        Requirement22 req22 = new Requirement22(con);

        ArrayList<City> cities =
                req22.getTopNCapitalCitiesInRegion("Western Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(con).prepareStatement(anyString());
    }

    /**
     * Test 10:
     * Verifies SQL exception handling when binding the region parameter.
     */
    @Test
    public void getTopNCapitalCitiesInRegionSetRegionExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid region parameter"))
                .when(pstmt)
                .setString(1, "Western Europe");

        Requirement22 req22 = new Requirement22(con);

        ArrayList<City> cities =
                req22.getTopNCapitalCitiesInRegion("Western Europe", 5);

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
    public void getTopNCapitalCitiesInRegionSetLimitExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid LIMIT parameter"))
                .when(pstmt)
                .setInt(2, 5);

        Requirement22 req22 = new Requirement22(con);

        ArrayList<City> cities =
                req22.getTopNCapitalCitiesInRegion("Western Europe", 5);

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
    public void getTopNCapitalCitiesInRegionQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement22 req22 = new Requirement22(con);

        ArrayList<City> cities =
                req22.getTopNCapitalCitiesInRegion("Western Europe", 5);

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
    public void getTopNCapitalCitiesInRegionResultSetExceptionTest()
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

        Requirement22 req22 = new Requirement22(con);

        ArrayList<City> cities =
                req22.getTopNCapitalCitiesInRegion("Western Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).next();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 14:
     * Verifies SQL exception handling when reading the city ID.
     */
    @Test
    public void getTopNCapitalCitiesInRegionIdMappingExceptionTest()
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

        Requirement22 req22 = new Requirement22(con);

        ArrayList<City> cities =
                req22.getTopNCapitalCitiesInRegion("Western Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).getInt("ID");
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 15:
     * Verifies SQL exception handling when reading the city name.
     */
    @Test
    public void getTopNCapitalCitiesInRegionNameMappingExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true);

        when(rset.getInt("ID")).thenReturn(3068);

        when(rset.getString("Name"))
                .thenThrow(
                        new SQLException("Unable to read city name")
                );

        Requirement22 req22 = new Requirement22(con);

        ArrayList<City> cities =
                req22.getTopNCapitalCitiesInRegion("Western Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).getInt("ID");
        verify(rset).getString("Name");

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 16:
     * Verifies SQL exception handling when reading the population.
     */
    @Test
    public void getTopNCapitalCitiesInRegionPopulationMappingExceptionTest()
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

        Requirement22 req22 = new Requirement22(con);

        ArrayList<City> cities =
                req22.getTopNCapitalCitiesInRegion("Western Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).getInt("Population");

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 17:
     * Verifies SQL exception handling when closing the ResultSet.
     */
    @Test
    public void getTopNCapitalCitiesInRegionResultSetCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("ResultSet close failed"))
                .when(rset)
                .close();

        Requirement22 req22 = new Requirement22(con);

        ArrayList<City> cities =
                req22.getTopNCapitalCitiesInRegion("Western Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 18:
     * Verifies SQL exception handling when closing the PreparedStatement.
     */
    @Test
    public void getTopNCapitalCitiesInRegionStatementCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("PreparedStatement close failed"))
                .when(pstmt)
                .close();

        Requirement22 req22 = new Requirement22(con);

        ArrayList<City> cities =
                req22.getTopNCapitalCitiesInRegion("Western Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 19:
     * Verifies that previously retrieved records are retained
     * if a later ResultSet read fails.
     */
    @Test
    public void getTopNCapitalCitiesInRegionPartialResultsExceptionTest()
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

        when(rset.getInt("ID")).thenReturn(3068);
        when(rset.getString("Name")).thenReturn("Berlin");
        when(rset.getString("CountryCode")).thenReturn("DEU");
        when(rset.getString("District")).thenReturn("Berliini");
        when(rset.getInt("Population")).thenReturn(3386667);

        Requirement22 req22 = new Requirement22(con);

        ArrayList<City> cities =
                req22.getTopNCapitalCitiesInRegion("Western Europe", 5);

        assertNotNull(cities);
        assertEquals(1, cities.size());

        assertEquals("Berlin", cities.get(0).getName());
        assertEquals(3386667, cities.get(0).getPopulation());

        verify(rset, times(2)).next();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 20:
     * Verifies that printCapitalCities handles null and empty lists.
     *
     * Preserves the original empty-list test scenario.
     */
    @Test
    public void printCapitalCitiesEmptyListTest() {

        Requirement22 req22 = new Requirement22(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req22.printCapitalCities(null, "Western Europe", 5);
            req22.printCapitalCities(new ArrayList<>(), "Western Europe", 5);

            String printed = output.toString();

            String expected =
                    "No capital cities found for Top 5 capital cities in region: Western Europe";

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
     * Test 21:
     * Verifies that printCapitalCities formats valid records correctly.
     *
     * Preserves and expands the original valid-list test scenario.
     */
    @Test
    public void printCapitalCitiesValidListTest() {

        Requirement22 req22 = new Requirement22(null);

        ArrayList<City> cities = new ArrayList<>();

        City city = new City();

        city.setId(3068);
        city.setName("Berlin");
        city.setCountryCode("DEU");
        city.setDistrict("Berliini");
        city.setPopulation(3386667);

        cities.add(city);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req22.printCapitalCities(cities, "Western Europe", 1);

            String printed = output.toString();

            assertTrue(
                    printed.contains(
                            "REQ-22: Top 1 Populated Capital Cities in Region 'Western Europe'"
                    )
            );

            assertTrue(printed.contains("Name"));
            assertTrue(printed.contains("Country Code"));
            assertTrue(printed.contains("District"));
            assertTrue(printed.contains("Population"));

            assertTrue(printed.contains("Berlin"));
            assertTrue(printed.contains("DEU"));
            assertTrue(printed.contains("Berliini"));
            assertTrue(printed.contains("3386667"));

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