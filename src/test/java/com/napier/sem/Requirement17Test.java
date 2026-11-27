package com.napier.sem;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for REQ-17:
 * All capital cities in the world organised by population,
 * from largest to smallest.
 *
 * Tests database connectivity, SQL execution, city data mapping,
 * empty results, exception handling, JDBC resource management
 * and formatted report output.
 *
 * Mockito simulates JDBC operations without requiring
 * an active MySQL database connection.
 */
public class Requirement17Test {

    /**
     * Test 1:
     * Verifies that a null database connection returns an empty list.
     */
    @Test
    public void getAllCapitalCitiesWorldNullConnectionTest() {

        Requirement17 req17 = new Requirement17(null);

        ArrayList<City> result =
                req17.getAllCapitalCitiesWorld();

        assertNotNull(result);

        assertTrue(
                result.isEmpty(),
                "Expected empty list when database connection is null"
        );
    }

    /**
     * Test 2:
     * Verifies successful retrieval of multiple capital cities.
     *
     * Checks city field mapping, SQL query structure,
     * population ordering clause and JDBC resource closure.
     */
    @Test
    public void getAllCapitalCitiesWorldSuccessTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

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

        Requirement17 req17 = new Requirement17(con);

        ArrayList<City> cities =
                req17.getAllCapitalCitiesWorld();

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
        verify(stmt).executeQuery(
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
                )
        );

        verify(con).createStatement();

        // Verify all rows were processed.
        verify(rset, times(3)).next();

        // Verify JDBC resources were closed.
        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 3:
     * Verifies successful retrieval of a single capital city.
     */
    @Test
    public void getAllCapitalCitiesWorldSingleCityTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(2331);
        when(rset.getString("Name")).thenReturn("Seoul");
        when(rset.getString("CountryCode")).thenReturn("KOR");
        when(rset.getString("District")).thenReturn("Seoul");
        when(rset.getInt("Population")).thenReturn(9981619);

        Requirement17 req17 = new Requirement17(con);

        ArrayList<City> cities =
                req17.getAllCapitalCitiesWorld();

        assertNotNull(cities);
        assertEquals(1, cities.size());

        City city = cities.get(0);

        assertEquals(2331, city.getId());
        assertEquals("Seoul", city.getName());
        assertEquals("KOR", city.getCountryCode());
        assertEquals("Seoul", city.getDistrict());
        assertEquals(9981619, city.getPopulation());

        verify(rset, times(2)).next();
        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 4:
     * Verifies that an empty ResultSet returns an empty list.
     */
    @Test
    public void getAllCapitalCitiesWorldEmptyResultTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        Requirement17 req17 = new Requirement17(con);

        ArrayList<City> cities =
                req17.getAllCapitalCitiesWorld();

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).next();
        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 5:
     * Verifies SQL exception handling during Statement creation.
     */
    @Test
    public void getAllCapitalCitiesWorldCreateStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.createStatement())
                .thenThrow(
                        new SQLException("Statement creation failed")
                );

        Requirement17 req17 = new Requirement17(con);

        ArrayList<City> cities =
                req17.getAllCapitalCitiesWorld();

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(con).createStatement();
    }

    /**
     * Test 6:
     * Verifies SQL exception handling during query execution.
     */
    @Test
    public void getAllCapitalCitiesWorldQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);

        when(con.createStatement()).thenReturn(stmt);

        when(stmt.executeQuery(anyString()))
                .thenThrow(
                        new SQLException("Query execution failed")
                );

        Requirement17 req17 = new Requirement17(con);

        ArrayList<City> cities =
                req17.getAllCapitalCitiesWorld();

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(stmt).executeQuery(anyString());
        verify(stmt).close();
    }

    /**
     * Test 7:
     * Verifies SQL exception handling while reading ResultSet records.
     */
    @Test
    public void getAllCapitalCitiesWorldResultSetExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        when(rset.next())
                .thenThrow(
                        new SQLException("Failed to read capital cities")
                );

        Requirement17 req17 = new Requirement17(con);

        ArrayList<City> cities =
                req17.getAllCapitalCitiesWorld();

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).next();
        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 8:
     * Verifies SQL exception handling when reading the city ID.
     */
    @Test
    public void getAllCapitalCitiesWorldIdMappingExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        when(rset.next()).thenReturn(true);

        when(rset.getInt("ID"))
                .thenThrow(
                        new SQLException("Unable to read city ID")
                );

        Requirement17 req17 = new Requirement17(con);

        ArrayList<City> cities =
                req17.getAllCapitalCitiesWorld();

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).getInt("ID");
        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 9:
     * Verifies SQL exception handling when reading the city name.
     */
    @Test
    public void getAllCapitalCitiesWorldNameMappingExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        when(rset.next()).thenReturn(true);

        when(rset.getInt("ID")).thenReturn(2331);

        when(rset.getString("Name"))
                .thenThrow(
                        new SQLException("Unable to read city name")
                );

        Requirement17 req17 = new Requirement17(con);

        ArrayList<City> cities =
                req17.getAllCapitalCitiesWorld();

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).getInt("ID");
        verify(rset).getString("Name");

        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 10:
     * Verifies SQL exception handling when reading the population.
     */
    @Test
    public void getAllCapitalCitiesWorldPopulationMappingExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        when(rset.next()).thenReturn(true);

        when(rset.getInt("Population"))
                .thenThrow(
                        new SQLException("Unable to read population")
                );

        Requirement17 req17 = new Requirement17(con);

        ArrayList<City> cities =
                req17.getAllCapitalCitiesWorld();

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).getInt("Population");

        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 11:
     * Verifies SQL exception handling when closing the ResultSet.
     */
    @Test
    public void getAllCapitalCitiesWorldResultSetCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        doThrow(new SQLException("ResultSet close failed"))
                .when(rset)
                .close();

        Requirement17 req17 = new Requirement17(con);

        ArrayList<City> cities =
                req17.getAllCapitalCitiesWorld();

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 12:
     * Verifies SQL exception handling when closing the Statement.
     */
    @Test
    public void getAllCapitalCitiesWorldStatementCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        doThrow(new SQLException("Statement close failed"))
                .when(stmt)
                .close();

        Requirement17 req17 = new Requirement17(con);

        ArrayList<City> cities =
                req17.getAllCapitalCitiesWorld();

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 13:
     * Verifies that previously retrieved records are retained
     * if a later ResultSet read fails.
     */
    @Test
    public void getAllCapitalCitiesWorldPartialResultsExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

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

        Requirement17 req17 = new Requirement17(con);

        ArrayList<City> cities =
                req17.getAllCapitalCitiesWorld();

        assertNotNull(cities);
        assertEquals(1, cities.size());

        assertEquals("Seoul", cities.get(0).getName());
        assertEquals(9981619, cities.get(0).getPopulation());

        verify(rset, times(2)).next();
        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 14:
     * Verifies that valid records are retained
     * if closing the ResultSet fails.
     */
    @Test
    public void getAllCapitalCitiesWorldDataRetainedOnCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(2331);
        when(rset.getString("Name")).thenReturn("Seoul");
        when(rset.getString("CountryCode")).thenReturn("KOR");
        when(rset.getString("District")).thenReturn("Seoul");
        when(rset.getInt("Population")).thenReturn(9981619);

        doThrow(new SQLException("ResultSet close failed"))
                .when(rset)
                .close();

        Requirement17 req17 = new Requirement17(con);

        ArrayList<City> cities =
                req17.getAllCapitalCitiesWorld();

        assertNotNull(cities);
        assertEquals(1, cities.size());
        assertEquals("Seoul", cities.get(0).getName());

        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 15:
     * Verifies that printCapitalCities handles a null list.
     *
     * Preserves the original test scenario.
     */
    @Test
    public void printCapitalCitiesNullListTest() {

        Requirement17 req17 = new Requirement17(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req17.printCapitalCities(null);

            assertTrue(
                    output.toString().contains(
                            "No capital cities found in the world."
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 16:
     * Verifies that printCapitalCities handles an empty list.
     *
     * Preserves the original test scenario.
     */
    @Test
    public void printCapitalCitiesEmptyListTest() {

        Requirement17 req17 = new Requirement17(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req17.printCapitalCities(new ArrayList<>());

            assertTrue(
                    output.toString().contains(
                            "No capital cities found in the world."
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 17:
     * Verifies that printCapitalCities formats valid
     * capital city records correctly.
     *
     * Preserves and expands the original test scenario.
     */
    @Test
    public void printCapitalCitiesValidListTest() {

        Requirement17 req17 = new Requirement17(null);

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

            req17.printCapitalCities(cities);

            String printed = output.toString();

            assertTrue(
                    printed.contains(
                            "REQ-17: All Capital Cities in the World by Population"
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