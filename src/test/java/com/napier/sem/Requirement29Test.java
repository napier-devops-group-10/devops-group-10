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
    /**
     * Tests successful retrieval of a country's population
     * using a mocked database connection.
     *
     * REQ-29: Population of a Country.
     */
    @Test
    void getCountryPopulationSuccessMockitoTest() throws SQLException {

        // Arrange: Mock the database objects.
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        // Simulate a successful database query.
        when(connection.prepareStatement(anyString()))
                .thenReturn(statement);

        when(statement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true);

        when(resultSet.getLong("Population"))
                .thenReturn(125000L);

        // Create REQ-29 using the mocked connection.
        Requirement29 requirement29 = new Requirement29(connection);

        // Act: Retrieve the population.
        long population = requirement29.getCountryPopulation("Grenada");

        // Assert: Verify the expected population.
        assertEquals(125000L, population);

        // Verify that the correct country name was used.
        verify(statement).setString(1, "Grenada");

        // Verify that the query was executed.
        verify(statement).executeQuery();

        // Verify that the population was retrieved.
        verify(resultSet).getLong("Population");
    }
    /**
     * Tests that printCountryPopulation handles a null country name
     * without throwing an exception.
     *
     * REQ-29: Population of a Country.
     */
    @Test
    void printCountryPopulationNullNameTest() {

        // Arrange: Create Requirement29 without a database connection.
        Requirement29 requirement29 = new Requirement29(null);

        // Act and Assert: Verify that a null country name is handled.
        assertDoesNotThrow(() ->
                requirement29.printCountryPopulation(null)
        );
    }
    /**
     * Tests a null country name when a database connection exists.
     *
     * REQ-29: Population of a Country.
     */
    @Test
    void getCountryPopulationNullNameWithConnectionTest() {

        // Arrange: Create a mocked database connection.
        Connection connection = mock(Connection.class);

        Requirement29 requirement29 = new Requirement29(connection);

        // Act: Request population using a null country name.
        long population = requirement29.getCountryPopulation(null);

        // Assert: Invalid input should return -1.
        assertEquals(-1, population);

        // Verify that no database operation was attempted.
        verifyNoInteractions(connection);
    }
    /**
     * Tests that a whitespace-only country name is rejected
     * when a database connection is available.
     *
     * REQ-29: Population of a Country.
     */
    @Test
    void getCountryPopulationEmptyNameWithConnectionTest() {

        // Arrange: Create a mocked database connection.
        Connection connection = mock(Connection.class);

        Requirement29 requirement29 = new Requirement29(connection);

        // Act: Request population using a whitespace-only country name.
        long population = requirement29.getCountryPopulation("   ");

        // Assert: Invalid input should return -1.
        assertEquals(-1, population);

        // Verify that no database query was attempted.
        verifyNoInteractions(connection);
    }
}