package com.napier.sem;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for REQ-31: Population of a City.
 *
 * Tests successful population retrieval, invalid parameters,
 * missing database connections, SQL exceptions,
 * missing city records, and formatted population output.
 */
public class Requirement31Test {

    /**
     * Verifies that a missing database connection returns -1 safely.
     */
    @Test
    public void getCityPopulationNullConnectionTest() {

        Requirement31 req31 = new Requirement31(null);

        long population =
                req31.getCityPopulation("Palma de Mallorca");

        assertEquals(-1L, population,
                "Expected -1 when database connection is null");
    }

    /**
     * Verifies that a null city parameter is rejected safely.
     */
    @Test
    public void getCityPopulationNullParamTest() {

        Requirement31 req31 = new Requirement31(null);

        long population = req31.getCityPopulation(null);

        assertEquals(-1L, population,
                "Expected -1 when city name is null");
    }

    /**
     * Verifies that a whitespace-only city name is rejected.
     */
    @Test
    public void getCityPopulationEmptyParamTest() {

        Requirement31 req31 = new Requirement31(null);

        long population = req31.getCityPopulation("   ");

        assertEquals(-1L, population,
                "Expected -1 when city name is blank");
    }

    /**
     * Verifies that an empty city name returns -1.
     */
    @Test
    public void getCityPopulationEmptyStringTest() {

        Requirement31 req31 = new Requirement31(null);

        long population = req31.getCityPopulation("");

        assertEquals(-1L, population);
    }

    /**
     * Verifies successful retrieval of a city's population.
     */
    @Test
    public void getCityPopulationSuccessTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString()))
                .thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenReturn(rset);

        when(rset.next())
                .thenReturn(true);

        when(rset.getLong("Population"))
                .thenReturn(400000L);

        Requirement31 req31 = new Requirement31(con);

        long population =
                req31.getCityPopulation("Palma de Mallorca");

        assertEquals(400000L, population);

        verify(pstmt).setString(1, "Palma de Mallorca");
        verify(rset).getLong("Population");
    }

    /**
     * Verifies that leading and trailing spaces are removed
     * before executing the database query.
     */
    @Test
    public void getCityPopulationTrimmedInputTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString()))
                .thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenReturn(rset);

        when(rset.next())
                .thenReturn(true);

        when(rset.getLong("Population"))
                .thenReturn(3000000L);

        Requirement31 req31 = new Requirement31(con);

        long population =
                req31.getCityPopulation("  Madrid  ");

        assertEquals(3000000L, population);

        verify(pstmt).setString(1, "Madrid");
    }

    /**
     * Verifies that a city not found in the database
     * returns -1.
     */
    @Test
    public void getCityPopulationNotFoundTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString()))
                .thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenReturn(rset);

        when(rset.next())
                .thenReturn(false);

        Requirement31 req31 = new Requirement31(con);

        long population =
                req31.getCityPopulation("UnknownCity");

        assertEquals(-1L, population);
    }

    /**
     * Verifies that an SQL exception during statement preparation
     * is handled safely.
     */
    @Test
    public void getCityPopulationSQLExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(new SQLException("Database error"));

        Requirement31 req31 = new Requirement31(con);

        long population =
                req31.getCityPopulation("Madrid");

        assertEquals(-1L, population);
    }

    /**
     * Verifies that an SQL exception during query execution
     * returns -1 without crashing the application.
     */
    @Test
    public void getCityPopulationExecuteQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString()))
                .thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(new SQLException("Query execution failed"));

        Requirement31 req31 = new Requirement31(con);

        long population =
                req31.getCityPopulation("Madrid");

        assertEquals(-1L, population);
    }

    /**
     * Verifies that a null city name is rejected
     * even when a database connection exists.
     */
    @Test
    public void getCityPopulationNullParamWithConnectionTest() {

        Connection con = mock(Connection.class);

        Requirement31 req31 = new Requirement31(con);

        long population = req31.getCityPopulation(null);

        assertEquals(-1L, population);

        verifyNoInteractions(con);
    }

    /**
     * Verifies that a blank city name is rejected
     * even when a database connection exists.
     */
    @Test
    public void getCityPopulationBlankParamWithConnectionTest() {

        Connection con = mock(Connection.class);

        Requirement31 req31 = new Requirement31(con);

        long population = req31.getCityPopulation("   ");

        assertEquals(-1L, population);

        verifyNoInteractions(con);
    }

    /**
     * Verifies that an unavailable city population
     * displays the appropriate message.
     */
    @Test
    public void printCityPopulationUnavailableTest() {

        Requirement31 req31 = new Requirement31(null);

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req31.printCityPopulation("Palma de Mallorca");

            assertTrue(output.toString().contains(
                    "Population unavailable for city: Palma de Mallorca"));

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that a successful population lookup
     * displays the expected formatted report.
     */
    @Test
    public void printCityPopulationSuccessTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString()))
                .thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenReturn(rset);

        when(rset.next())
                .thenReturn(true);

        when(rset.getLong("Population"))
                .thenReturn(1500000L);

        Requirement31 req31 = new Requirement31(con);

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req31.printCityPopulation("Madrid");

            String result = output.toString();

            assertTrue(result.contains(
                    "REQ-31: Population of City 'Madrid'"));

            assertTrue(result.contains(
                    "Total Population of Madrid: 1,500,000"));

        } finally {
            System.setOut(originalOut);
        }
    }
}