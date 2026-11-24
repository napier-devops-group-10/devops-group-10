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
import static org.mockito.Mockito.*;

/**
 * Unit tests for REQ-07:
 * All cities in the world organised by largest population to smallest.
 *
 * Tests database connectivity, successful SQL queries, city data mapping,
 * population ordering, empty results, SQL exception handling,
 * database resource management and formatted report output.
 *
 * Mockito is used to simulate database interactions without requiring
 * a running MySQL database.
 */
public class Requirement7Test {

    /**
     * Test 1:
     * Verifies that a null database connection returns an empty list safely.
     */
    @Test
    public void getAllCitiesWorldNullConnectionTest() {

        Requirement7 req7 = new Requirement7(null);

        ArrayList<City> result = req7.getAllCitiesWorld();

        assertNotNull(result);
        assertTrue(
                result.isEmpty(),
                "Expected empty list when database connection is null"
        );
    }

    /**
     * Test 2:
     * Verifies successful retrieval of multiple cities.
     *
     * Checks all City fields and verifies that the SQL query
     * requests descending population order.
     */
    @Test
    public void getAllCitiesWorldSuccessTest() throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        // Simulate two database records followed by the end of results.
        when(rset.next()).thenReturn(true, true, false);

        when(rset.getInt("ID"))
                .thenReturn(1024, 2331);

        when(rset.getString("Name"))
                .thenReturn("Mumbai (Bombay)", "London");

        when(rset.getString("CountryCode"))
                .thenReturn("IND", "GBR");

        when(rset.getString("District"))
                .thenReturn("Maharashtra", "England");

        when(rset.getInt("Population"))
                .thenReturn(10500000, 7285000);

        Requirement7 req7 = new Requirement7(con);

        ArrayList<City> cities = req7.getAllCitiesWorld();

        // Verify the number of records returned.
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
        City london = cities.get(1);

        assertEquals(2331, london.getId());
        assertEquals("London", london.getName());
        assertEquals("GBR", london.getCountryCode());
        assertEquals("England", london.getDistrict());
        assertEquals(7285000, london.getPopulation());

        // Verify the mocked records are in descending population order.
        assertTrue(
                mumbai.getPopulation() >= london.getPopulation()
        );

        // Verify that the SQL query requests descending population.
        verify(stmt).executeQuery(
                argThat(sql ->
                        sql.contains("FROM city")
                                && sql.contains("ORDER BY Population DESC")
                )
        );

        // Verify database resources are closed.
        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 3:
     * Verifies that a single city is correctly retrieved and mapped.
     */
    @Test
    public void getAllCitiesWorldSingleCityTest() throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        when(rset.next()).thenReturn(true, false);

        when(rset.getInt("ID")).thenReturn(3793);
        when(rset.getString("Name")).thenReturn("New York");
        when(rset.getString("CountryCode")).thenReturn("USA");
        when(rset.getString("District")).thenReturn("New York");
        when(rset.getInt("Population")).thenReturn(8008278);

        Requirement7 req7 = new Requirement7(con);

        ArrayList<City> cities = req7.getAllCitiesWorld();

        assertNotNull(cities);
        assertEquals(1, cities.size());

        City city = cities.get(0);

        assertEquals(3793, city.getId());
        assertEquals("New York", city.getName());
        assertEquals("USA", city.getCountryCode());
        assertEquals("New York", city.getDistrict());
        assertEquals(8008278, city.getPopulation());

        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 4:
     * Verifies that an empty database result produces an empty list.
     */
    @Test
    public void getAllCitiesWorldEmptyResultTest() throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        // Simulate no records returned by the database.
        when(rset.next()).thenReturn(false);

        Requirement7 req7 = new Requirement7(con);

        ArrayList<City> cities = req7.getAllCitiesWorld();

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(con).createStatement();
        verify(stmt).executeQuery(anyString());

        // Verify resources are closed even when no records exist.
        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 5:
     * Verifies SQL exception handling when creating a statement.
     */
    @Test
    public void getAllCitiesWorldCreateStatementExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.createStatement())
                .thenThrow(new SQLException("Failed to create statement"));

        Requirement7 req7 = new Requirement7(con);

        ArrayList<City> cities = req7.getAllCitiesWorld();

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(con).createStatement();
    }

    /**
     * Test 6:
     * Verifies SQL exception handling during query execution.
     */
    @Test
    public void getAllCitiesWorldQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);

        when(con.createStatement()).thenReturn(stmt);

        when(stmt.executeQuery(anyString()))
                .thenThrow(new SQLException("Query execution failed"));

        Requirement7 req7 = new Requirement7(con);

        ArrayList<City> cities = req7.getAllCitiesWorld();

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(stmt).executeQuery(anyString());

        // Statement must still be closed after the exception.
        verify(stmt).close();
    }

    /**
     * Test 7:
     * Verifies SQL exception handling when reading ResultSet records.
     */
    @Test
    public void getAllCitiesWorldResultSetExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);

        // Simulate an error while reading the ResultSet.
        when(rset.next())
                .thenThrow(new SQLException("Failed to read city records"));

        Requirement7 req7 = new Requirement7(con);

        ArrayList<City> cities = req7.getAllCitiesWorld();

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        verify(rset).next();

        // Both resources must be closed after the exception.
        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 8:
     * Verifies SQL exception handling when closing the ResultSet.
     */
    @Test
    public void getAllCitiesWorldResultSetCloseExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rset);
        when(rset.next()).thenReturn(false);

        // Simulate an error during ResultSet closure.
        doThrow(new SQLException("Failed to close ResultSet"))
                .when(rset).close();

        Requirement7 req7 = new Requirement7(con);

        ArrayList<City> cities = req7.getAllCitiesWorld();

        assertNotNull(cities);
        assertTrue(cities.isEmpty());

        // Statement should still close despite the ResultSet error.
        verify(rset).close();
        verify(stmt).close();
    }

    /**
     * Test 9:
     * Verifies that printCities handles null lists gracefully.
     */
    @Test
    public void printCitiesNullListTest() {

        Requirement7 req7 = new Requirement7(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req7.printCities(null);

            assertTrue(
                    output.toString().contains("No cities found in the world.")
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 10:
     * Verifies that printCities handles empty lists gracefully.
     */
    @Test
    public void printCitiesEmptyListTest() {

        Requirement7 req7 = new Requirement7(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req7.printCities(new ArrayList<>());

            assertTrue(
                    output.toString().contains("No cities found in the world.")
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test 11:
     * Verifies that printCities formats valid City records properly.
     */
    @Test
    public void printCitiesValidListTest() {

        Requirement7 req7 = new Requirement7(null);

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

            req7.printCities(cities);

            String printed = output.toString();

            assertTrue(printed.contains("REQ-07"));
            assertTrue(printed.contains("All Cities in the World by Population"));
            assertTrue(printed.contains("Mumbai (Bombay)"));
            assertTrue(printed.contains("IND"));
            assertTrue(printed.contains("Maharashtra"));
            assertTrue(printed.contains("10500000"));

            assertTrue(
                    printed.contains("Total cities displayed: 1")
            );

        } finally {
            System.setOut(originalOut);
        }
    }
}