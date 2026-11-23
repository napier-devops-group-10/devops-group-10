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
 * Unit tests for REQ-30: Population of a District.
 *
 * Tests valid population retrieval, invalid parameters,
 * missing database connections, SQL exceptions,
 * and formatted population output.
 */
public class Requirement30Test {

    /**
     * Verifies that a missing database connection returns -1 safely.
     */
    @Test
    public void getDistrictPopulationNullConnectionTest() {
        Requirement30 req30 = new Requirement30(null);

        long population = req30.getDistrictPopulation("Baleares");

        assertEquals(-1L, population,
                "Expected -1 when database connection is null");
    }

    /**
     * Verifies that a null district parameter is rejected safely.
     */
    @Test
    public void getDistrictPopulationNullParamTest() {
        Requirement30 req30 = new Requirement30(null);

        long population = req30.getDistrictPopulation(null);

        assertEquals(-1L, population,
                "Expected -1 when district is null");
    }

    /**
     * Verifies that a blank district parameter is rejected safely.
     */
    @Test
    public void getDistrictPopulationEmptyParamTest() {
        Requirement30 req30 = new Requirement30(null);

        long population = req30.getDistrictPopulation("   ");

        assertEquals(-1L, population,
                "Expected -1 when district is blank");
    }

    /**
     * Verifies that an empty district string is rejected.
     */
    @Test
    public void getDistrictPopulationEmptyStringTest() {
        Requirement30 req30 = new Requirement30(null);

        long population = req30.getDistrictPopulation("");

        assertEquals(-1L, population);
    }

    /**
     * Verifies successful retrieval of a district population.
     */
    @Test
    public void getDistrictPopulationSuccessTest() throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true);
        when(rset.getLong("TotalPopulation")).thenReturn(500000L);
        when(rset.wasNull()).thenReturn(false);

        Requirement30 req30 = new Requirement30(con);

        long population = req30.getDistrictPopulation("Baleares");

        assertEquals(500000L, population);

        verify(pstmt).setString(1, "Baleares");
        verify(rset).getLong("TotalPopulation");
    }

    /**
     * Verifies that leading and trailing spaces are removed
     * from the district parameter before executing the query.
     */
    @Test
    public void getDistrictPopulationTrimmedInputTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true);
        when(rset.getLong("TotalPopulation")).thenReturn(250000L);
        when(rset.wasNull()).thenReturn(false);

        Requirement30 req30 = new Requirement30(con);

        long population =
                req30.getDistrictPopulation("  Gelderland  ");

        assertEquals(250000L, population);

        verify(pstmt).setString(1, "Gelderland");
    }

    /**
     * Verifies that a SQL NULL population returns -1.
     *
     * SUM(Population) returns NULL when no matching
     * district records are found.
     */
    @Test
    public void getDistrictPopulationSqlNullTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true);
        when(rset.getLong("TotalPopulation")).thenReturn(0L);
        when(rset.wasNull()).thenReturn(true);

        Requirement30 req30 = new Requirement30(con);

        long population =
                req30.getDistrictPopulation("UnknownDistrict");

        assertEquals(-1L, population);
    }

    /**
     * Verifies that a result set containing no rows returns -1.
     */
    @Test
    public void getDistrictPopulationNoRowsTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(false);

        Requirement30 req30 = new Requirement30(con);

        long population =
                req30.getDistrictPopulation("UnknownDistrict");

        assertEquals(-1L, population);
    }

    /**
     * Verifies that an SQL exception during statement preparation
     * is handled without crashing the application.
     */
    @Test
    public void getDistrictPopulationSQLExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenThrow(new SQLException("Database error"));

        Requirement30 req30 = new Requirement30(con);

        long population =
                req30.getDistrictPopulation("Baleares");

        assertEquals(-1L, population);
    }

    /**
     * Verifies that an SQL exception during query execution
     * returns -1 safely.
     */
    @Test
    public void getDistrictPopulationExecuteQueryExceptionTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);

        when(pstmt.executeQuery())
                .thenThrow(new SQLException("Query execution failed"));

        Requirement30 req30 = new Requirement30(con);

        long population =
                req30.getDistrictPopulation("Baleares");

        assertEquals(-1L, population);
    }

    /**
     * Verifies that an unavailable population displays
     * the appropriate message.
     */
    @Test
    public void printDistrictPopulationUnavailableTest() {

        Requirement30 req30 = new Requirement30(null);

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req30.printDistrictPopulation("Baleares");

            assertTrue(output.toString().contains(
                    "Population unavailable for district: Baleares"));

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that a successfully retrieved population
     * is displayed using the expected report format.
     */
    @Test
    public void printDistrictPopulationSuccessTest()
            throws SQLException {

        Connection con = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rset = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rset);

        when(rset.next()).thenReturn(true);
        when(rset.getLong("TotalPopulation")).thenReturn(1500000L);
        when(rset.wasNull()).thenReturn(false);

        Requirement30 req30 = new Requirement30(con);

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req30.printDistrictPopulation("Baleares");

            String result = output.toString();

            assertTrue(result.contains(
                    "REQ-30: Population of District 'Baleares'"));

            assertTrue(result.contains(
                    "Total Population of Baleares: 1,500,000"));

        } finally {
            System.setOut(originalOut);
        }
    }
}