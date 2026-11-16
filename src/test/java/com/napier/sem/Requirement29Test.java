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
 * REQ-29: Population of a Country.
 *
 * Unit tests for the Requirement29 class.
 *
 * Tests valid and invalid inputs, missing database connections,
 * countries not found, and SQL exception handling.
 */
public class Requirement29Test {

    /**
     * Verifies that the method handles a missing
     * database connection correctly.
     */
    @Test
    public void getCountryPopulationWithoutConnectionTest() {

        Requirement29 req29 = new Requirement29(null);

        long population = req29.getCountryPopulation("Grenada");

        assertEquals(-1L, population);
    }

    /**
     * Verifies that a null country name is rejected.
     */
    @Test
    public void getCountryPopulationNullNameTest() {

        Requirement29 req29 = new Requirement29(null);

        long population = req29.getCountryPopulation(null);

        assertEquals(-1L, population);
    }

    /**
     * Verifies that an empty country name is rejected.
     */
    @Test
    public void getCountryPopulationEmptyNameTest() {

        Requirement29 req29 = new Requirement29(null);

        long population = req29.getCountryPopulation("");

        assertEquals(-1L, population);
    }

    /**
     * Verifies that an appropriate message is displayed
     * when the database connection is unavailable.
     */
    @Test
    public void printCountryPopulationWithoutConnectionTest() {

        Requirement29 req29 = new Requirement29(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            req29.printCountryPopulation("Grenada");

            assertTrue(
                    output.toString().contains(
                            "Population unavailable for: Grenada"
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that an unknown country returns -1
     * when the database query produces no records.
     */
    @Test
    public void getCountryPopulationCountryNotFoundTest()
            throws SQLException {

        // Create mock database objects.
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        // Simulate preparing and executing a SQL query.
        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        when(statement.executeQuery())
                .thenReturn(resultSet);

        // Simulate a query that returns no matching country.
        when(resultSet.next()).thenReturn(false);

        Requirement29 req29 = new Requirement29(connection);

        long population =
                req29.getCountryPopulation("UnknownCountry");

        // An unknown country should return -1.
        assertEquals(-1L, population);

        // Verify that the query was executed.
        verify(statement).executeQuery();
    }

    /**
     * Verifies that SQL exceptions are handled correctly
     * and the method returns -1 instead of crashing.
     */
    @Test
    public void getCountryPopulationSQLExceptionTest()
            throws SQLException {

        // Create a mock database connection.
        Connection connection = mock(Connection.class);

        // Simulate an SQL exception when preparing the query.
        when(connection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Simulated database error"));

        Requirement29 req29 = new Requirement29(connection);

        long population =
                req29.getCountryPopulation("Grenada");

        // The method should handle the exception and return -1.
        assertEquals(-1L, population);

        // Verify that the database query was attempted.
        verify(connection).prepareStatement(anyString());
    }
}