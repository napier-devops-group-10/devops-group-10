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
 * Unit tests for REQ-19:
 * All capital cities in a region organised by population,
 * from largest to smallest.
 *
 * Tests input validation, database connectivity, SQL execution,
 * city data mapping, empty results, exception handling,
 * JDBC resource management and formatted report output.
 *
 * Mockito simulates JDBC operations without requiring
 * an active MySQL database connection.
 */
public class Requirement19Test {

    /**
     * Test 1:
     * Verifies that a null database connection returns an empty list.
     */
    @Test
    public void getCapitalCitiesInRegionNullConnectionTest() {

        Requirement19 req19 = new Requirement19(null);

        ArrayList<City> result =
                req19.getCapitalCitiesInRegion("Western Europe");

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
    public void getCapitalCitiesInRegionInvalidRegionTest() {

        Connection con = mock(Connection.class);
        Requirement19 req19 = new Requirement19(con);

        assertTrue(
                req19.getCapitalCitiesInRegion(null).isEmpty()
        );

        assertTrue(
                req19.getCapitalCitiesInRegion("").isEmpty()
        );

        assertTrue(
                req19.getCapitalCitiesInRegion("   ").isEmpty()
        );

        // Invalid input must not access the database.
        verifyNoInteractions(con);
    }

    /**
     * Test 3:
     * Verifies successful retrieval of multiple capital cities.
     *
     * Checks city field mapping, SQL region filtering,
     * descending population ordering and JDBC resource closure.
     */
    @Test
    public void getCapitalCitiesInRegionSuccessTest()
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

        Requirement19 req19 = new Requirement19(con);

        ArrayList<City> cities =
                req19.getCapitalCitiesInRegion("Western Europe");

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
                )
        );

        // Verify the SQL parameter.
        verify(pstmt).setString(1, "Western Europe");
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
    public void getCapitalCitiesInRegionSingleCityTest()
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

        Requirement19 req19 = new Requirement19(con);

        ArrayList<City> cities =
                req19.getCapitalCitiesInRegion("Western Europe");

        assertNotNull(cities);
        assertEquals(1, cities.size());

        City city = cities.get(0);

        assertEquals(3068, city.getId());
        assertEquals("Berlin", city.getName());
        assertEquals("DEU", city.getCountryCode());
        assertEquals("Berliini", city.getDistrict());
        assertEquals(3386667, city.getPopulation());

        verify(pstmt).setString(1, "Western Europe");

        verify(rset, times(2)).next();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 5:
     * Verifies that leading and trailing whitespace
     * is removed from the region parameter.
     */
    @Test
    public void getCapitalCitiesInRegionTrimmedParameterTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement19 req19 = new Requirement19(con);

        ArrayList<City> cities =
                req19.getCapitalCitiesInRegion("  Western Europe  ");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt).executeQuery();

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 6:
     * Verifies that an empty ResultSet returns an empty list.
     */
    @Test
    public void getCapitalCitiesInRegionEmptyResultTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement19 req19 = new Requirement19(con);

        ArrayList<City> cities =
                req19.getCapitalCitiesInRegion("Caribbean");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Caribbean");

        verify(rset).next();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 7:
     * Verifies SQL exception handling during statement preparation.
     */
    @Test
    public void getCapitalCitiesInRegionPrepareStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(
                        new SQLException("Statement preparation failed")
                );

        Requirement19 req19 = new Requirement19(con);

        ArrayList<City> cities =
                req19.getCapitalCitiesInRegion("Western Europe");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(con).prepareStatement(anyString());
    }

    /**
     * Test 8:
     * Verifies SQL exception handling when binding the region parameter.
     */
    @Test
    public void getCapitalCitiesInRegionSetRegionExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid region parameter"))
                .when(pstmt)
                .setString(1, "Western Europe");

        Requirement19 req19 = new Requirement19(con);

        ArrayList<City> cities =
                req19.getCapitalCitiesInRegion("Western Europe");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt, never()).executeQuery();

        verify(pstmt).close();
    }

    /**
     * Test 9:
     * Verifies SQL exception handling during query execution.
     */
    @Test
    public void getCapitalCitiesInRegionQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement19 req19 = new Requirement19(con);

        ArrayList<City> cities =
                req19.getCapitalCitiesInRegion("Western Europe");

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Western Europe");
        verify(pstmt).executeQuery();

        verify(pstmt).close();
    }

    /**
     * Test 10:
     * Verifies SQL exception handling while reading ResultSet records.
     */
    @Test
    public void getCapitalCitiesInRegionResultSetExceptionTest()
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

        Requirement19 req19 = new Requirement19(con);

        ArrayList<City> cities =
                req19.getCapitalCitiesInRegion("Western Europe");

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
    public void getCapitalCitiesInRegionIdMappingExceptionTest()
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

        Requirement19 req19 = new Requirement19(con);

        ArrayList<City> cities =
                req19.getCapitalCitiesInRegion("Western Europe");

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
    public void getCapitalCitiesInRegionNameMappingExceptionTest()
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

        Requirement19 req19 = new Requirement19(con);

        ArrayList<City> cities =
                req19.getCapitalCitiesInRegion("Western Europe");

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
    public void getCapitalCitiesInRegionPopulationMappingExceptionTest()
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

        Requirement19 req19 = new Requirement19(con);

        ArrayList<City> cities =
                req19.getCapitalCitiesInRegion("Western Europe");

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
    public void getCapitalCitiesInRegionResultSetCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("ResultSet close failed"))
                .when(rset)
                .close();

        Requirement19 req19 = new Requirement19(con);

        ArrayList<City> cities =
                req19.getCapitalCitiesInRegion("Western Europe");

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
    public void getCapitalCitiesInRegionStatementCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("PreparedStatement close failed"))
                .when(pstmt)
                .close();

        Requirement19 req19 = new Requirement19(con);

        ArrayList<City> cities =
                req19.getCapitalCitiesInRegion("Western Europe");

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
    public void getCapitalCitiesInRegionPartialResultsExceptionTest()
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

        Requirement19 req19 = new Requirement19(con);

        ArrayList<City> cities =
                req19.getCapitalCitiesInRegion("Western Europe");

        assertNotNull(cities);
        assertEquals(1, cities.size());

        assertEquals("Berlin", cities.get(0).getName());
        assertEquals(3386667, cities.get(0).getPopulation());

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

        Requirement19 req19 = new Requirement19(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req19.printCapitalCities(null, "Western Europe");
            req19.printCapitalCities(
                    new ArrayList<>(),
                    "Western Europe"
            );

            String printed = output.toString();

            String expected =
                    "No capital cities found for region: Western Europe";

            assertEquals(
                    2,
                    printed.split(expected, -1).length - 1
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

        Requirement19 req19 = new Requirement19(null);

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

            req19.printCapitalCities(
                    cities,
                    "Western Europe"
            );

            String printed = output.toString();

            assertTrue(
                    printed.contains(
                            "REQ-19: Capital Cities in Region 'Western Europe' by Population"
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