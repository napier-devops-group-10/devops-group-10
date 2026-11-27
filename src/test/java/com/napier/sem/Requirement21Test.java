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
 * Unit tests for REQ-21:
 * The top N populated capital cities in a continent.
 *
 * Tests input validation, database connectivity, SQL execution,
 * city data mapping, empty results, exception handling,
 * JDBC resource management and formatted report output.
 *
 * Mockito simulates JDBC operations without requiring
 * an active MySQL database connection.
 */
public class Requirement21Test {

    /**
     * Test 1:
     * Verifies that a null database connection returns an empty list.
     */
    @Test
    public void getTopNCapitalCitiesInContinentNullConnectionTest() {

        Requirement21 req21 = new Requirement21(null);

        ArrayList<City> result =
                req21.getTopNCapitalCitiesInContinent("Europe", 5);

        assertNotNull(result);

        assertTrue(
                result.isEmpty(),
                "Expected empty list when database connection is null"
        );
    }

    /**
     * Test 2:
     * Verifies that null, empty and whitespace-only
     * continent parameters are rejected.
     */
    @Test
    public void getTopNCapitalCitiesInContinentInvalidContinentTest() {

        Connection con = mock(Connection.class);
        Requirement21 req21 = new Requirement21(con);

        assertTrue(
                req21.getTopNCapitalCitiesInContinent(null, 5).isEmpty()
        );

        assertTrue(
                req21.getTopNCapitalCitiesInContinent("", 5).isEmpty()
        );

        assertTrue(
                req21.getTopNCapitalCitiesInContinent("   ", 5).isEmpty()
        );

        // Invalid input must not access the database.
        verifyNoInteractions(con);
    }

    /**
     * Test 3:
     * Verifies that zero and negative N values are rejected.
     */
    @Test
    public void getTopNCapitalCitiesInContinentInvalidNTest() {

        Connection con = mock(Connection.class);
        Requirement21 req21 = new Requirement21(con);

        assertTrue(
                req21.getTopNCapitalCitiesInContinent("Europe", 0).isEmpty()
        );

        assertTrue(
                req21.getTopNCapitalCitiesInContinent("Asia", -3).isEmpty()
        );

        verifyNoInteractions(con);
    }

    /**
     * Test 4:
     * Verifies successful retrieval of multiple capital cities.
     *
     * Checks city field mapping, SQL structure,
     * continent filtering, LIMIT binding and JDBC closure.
     */
    @Test
    public void getTopNCapitalCitiesInContinentSuccessTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, true, false);

        when(rset.getInt("ID"))
                .thenReturn(456, 3068);

        when(rset.getString("Name"))
                .thenReturn("London", "Berlin");

        when(rset.getString("CountryCode"))
                .thenReturn("GBR", "DEU");

        when(rset.getString("District"))
                .thenReturn("England", "Berliini");

        when(rset.getInt("Population"))
                .thenReturn(7285000, 3386667);

        Requirement21 req21 = new Requirement21(con);

        ArrayList<City> cities =
                req21.getTopNCapitalCitiesInContinent("Europe", 2);

        assertNotNull(cities);
        assertEquals(2, cities.size());

        // Verify the first capital city.
        City london = cities.get(0);

        assertEquals(456, london.getId());
        assertEquals("London", london.getName());
        assertEquals("GBR", london.getCountryCode());
        assertEquals("England", london.getDistrict());
        assertEquals(7285000, london.getPopulation());

        // Verify the second capital city.
        City berlin = cities.get(1);

        assertEquals(3068, berlin.getId());
        assertEquals("Berlin", berlin.getName());
        assertEquals("DEU", berlin.getCountryCode());
        assertEquals("Berliini", berlin.getDistrict());
        assertEquals(3386667, berlin.getPopulation());

        // Verify the mocked records are in descending order.
        assertTrue(
                london.getPopulation() >= berlin.getPopulation()
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
                                && sql.contains("WHERE c.Continent = ?")
                                && sql.contains(
                                "ORDER BY ct.Population DESC"
                        )
                                && sql.contains("LIMIT ?")
                )
        );

        // Verify SQL parameters.
        verify(pstmt).setString(1, "Europe");
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
    public void getTopNCapitalCitiesInContinentSingleCityTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(456);
        when(rset.getString("Name")).thenReturn("London");
        when(rset.getString("CountryCode")).thenReturn("GBR");
        when(rset.getString("District")).thenReturn("England");
        when(rset.getInt("Population")).thenReturn(7285000);

        Requirement21 req21 = new Requirement21(con);

        ArrayList<City> cities =
                req21.getTopNCapitalCitiesInContinent("Europe", 1);

        assertNotNull(cities);
        assertEquals(1, cities.size());

        City city = cities.get(0);

        assertEquals(456, city.getId());
        assertEquals("London", city.getName());
        assertEquals("GBR", city.getCountryCode());
        assertEquals("England", city.getDistrict());
        assertEquals(7285000, city.getPopulation());

        verify(pstmt).setString(1, "Europe");
        verify(pstmt).setInt(2, 1);

        verify(rset, times(2)).next();
        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 6:
     * Verifies that whitespace is removed from the continent parameter.
     */
    @Test
    public void getTopNCapitalCitiesInContinentTrimmedParameterTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement21 req21 = new Requirement21(con);

        ArrayList<City> cities =
                req21.getTopNCapitalCitiesInContinent("  Europe  ", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Europe");
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
    public void getTopNCapitalCitiesInContinentLargeLimitTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement21 req21 = new Requirement21(con);

        ArrayList<City> cities =
                req21.getTopNCapitalCitiesInContinent("Asia", 1000);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Asia");
        verify(pstmt).setInt(2, 1000);

        verify(rset).close();
        verify(pstmt).close();
    }

    /**
     * Test 8:
     * Verifies that an empty ResultSet returns an empty list.
     */
    @Test
    public void getTopNCapitalCitiesInContinentEmptyResultTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        Requirement21 req21 = new Requirement21(con);

        ArrayList<City> cities =
                req21.getTopNCapitalCitiesInContinent("Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Europe");
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
    public void getTopNCapitalCitiesInContinentPrepareStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(
                        new SQLException("Statement preparation failed")
                );

        Requirement21 req21 = new Requirement21(con);

        ArrayList<City> cities =
                req21.getTopNCapitalCitiesInContinent("Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(con).prepareStatement(anyString());
    }

    /**
     * Test 10:
     * Verifies SQL exception handling when binding the continent parameter.
     */
    @Test
    public void getTopNCapitalCitiesInContinentSetContinentExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid continent parameter"))
                .when(pstmt)
                .setString(1, "Europe");

        Requirement21 req21 = new Requirement21(con);

        ArrayList<City> cities =
                req21.getTopNCapitalCitiesInContinent("Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Europe");
        verify(pstmt, never()).setInt(2, 5);
        verify(pstmt, never()).executeQuery();

        verify(pstmt).close();
    }

    /**
     * Test 11:
     * Verifies SQL exception handling when binding the LIMIT parameter.
     */
    @Test
    public void getTopNCapitalCitiesInContinentSetLimitExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        doThrow(new SQLException("Invalid LIMIT parameter"))
                .when(pstmt)
                .setInt(2, 5);

        Requirement21 req21 = new Requirement21(con);

        ArrayList<City> cities =
                req21.getTopNCapitalCitiesInContinent("Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Europe");
        verify(pstmt).setInt(2, 5);
        verify(pstmt, never()).executeQuery();

        verify(pstmt).close();
    }

    /**
     * Test 12:
     * Verifies SQL exception handling during query execution.
     */
    @Test
    public void getTopNCapitalCitiesInContinentQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement21 req21 = new Requirement21(con);

        ArrayList<City> cities =
                req21.getTopNCapitalCitiesInContinent("Europe", 5);

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(pstmt).setString(1, "Europe");
        verify(pstmt).setInt(2, 5);
        verify(pstmt).executeQuery();

        verify(pstmt).close();
    }

    /**
     * Test 13:
     * Verifies SQL exception handling while reading ResultSet records.
     */
    @Test
    public void getTopNCapitalCitiesInContinentResultSetExceptionTest()
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

        Requirement21 req21 = new Requirement21(con);

        ArrayList<City> cities =
                req21.getTopNCapitalCitiesInContinent("Europe", 5);

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
    public void getTopNCapitalCitiesInContinentIdMappingExceptionTest()
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

        Requirement21 req21 = new Requirement21(con);

        ArrayList<City> cities =
                req21.getTopNCapitalCitiesInContinent("Europe", 5);

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
    public void getTopNCapitalCitiesInContinentNameMappingExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true);

        when(rset.getInt("ID")).thenReturn(456);

        when(rset.getString("Name"))
                .thenThrow(
                        new SQLException("Unable to read city name")
                );

        Requirement21 req21 = new Requirement21(con);

        ArrayList<City> cities =
                req21.getTopNCapitalCitiesInContinent("Europe", 5);

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
    public void getTopNCapitalCitiesInContinentPopulationMappingExceptionTest()
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

        Requirement21 req21 = new Requirement21(con);

        ArrayList<City> cities =
                req21.getTopNCapitalCitiesInContinent("Europe", 5);

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
    public void getTopNCapitalCitiesInContinentResultSetCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("ResultSet close failed"))
                .when(rset)
                .close();

        Requirement21 req21 = new Requirement21(con);

        ArrayList<City> cities =
                req21.getTopNCapitalCitiesInContinent("Europe", 5);

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
    public void getTopNCapitalCitiesInContinentStatementCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        doThrow(new SQLException("PreparedStatement close failed"))
                .when(pstmt)
                .close();

        Requirement21 req21 = new Requirement21(con);

        ArrayList<City> cities =
                req21.getTopNCapitalCitiesInContinent("Europe", 5);

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
    public void getTopNCapitalCitiesInContinentPartialResultsExceptionTest()
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

        when(rset.getInt("ID")).thenReturn(456);
        when(rset.getString("Name")).thenReturn("London");
        when(rset.getString("CountryCode")).thenReturn("GBR");
        when(rset.getString("District")).thenReturn("England");
        when(rset.getInt("Population")).thenReturn(7285000);

        Requirement21 req21 = new Requirement21(con);

        ArrayList<City> cities =
                req21.getTopNCapitalCitiesInContinent("Europe", 5);

        assertNotNull(cities);
        assertEquals(1, cities.size());

        assertEquals("London", cities.get(0).getName());
        assertEquals(7285000, cities.get(0).getPopulation());

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

        Requirement21 req21 = new Requirement21(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req21.printCapitalCities(null, "Europe", 5);
            req21.printCapitalCities(new ArrayList<>(), "Europe", 5);

            String printed = output.toString();

            String expected =
                    "No capital cities found for Top 5 capital cities in continent: Europe";

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

        Requirement21 req21 = new Requirement21(null);

        ArrayList<City> cities = new ArrayList<>();

        City city = new City();

        city.setId(456);
        city.setName("London");
        city.setCountryCode("GBR");
        city.setDistrict("England");
        city.setPopulation(7285000);

        cities.add(city);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req21.printCapitalCities(cities, "Europe", 1);

            String printed = output.toString();

            assertTrue(
                    printed.contains(
                            "REQ-21: Top 1 Populated Capital Cities in Europe"
                    )
            );

            assertTrue(printed.contains("Name"));
            assertTrue(printed.contains("Country Code"));
            assertTrue(printed.contains("District"));
            assertTrue(printed.contains("Population"));

            assertTrue(printed.contains("London"));
            assertTrue(printed.contains("GBR"));
            assertTrue(printed.contains("England"));
            assertTrue(printed.contains("7285000"));

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