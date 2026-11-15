
package com.napier.sem;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for REQ-28: Population of a Region.
 *
 * Uses JUnit 5 and standard Java JDBC interfaces.
 * No external mocking library is required.
 */
public class Requirement28Test {

    /**
     * Checks behaviour when no database connection exists.
     */
    @Test
    public void testWithoutDatabaseConnection() {
        Requirement28 req28 = new Requirement28(null);

        assertEquals(-1L,
                req28.getRegionPopulation("Caribbean"));
    }

    /**
     * Checks null, empty and whitespace-only region names.
     */
    @Test
    public void testInvalidRegionNames() {
        Connection connection = fakeConnection(
                100L, false, null, null);

        Requirement28 req28 = new Requirement28(connection);

        assertAll(
                () -> assertEquals(-1L,
                        req28.getRegionPopulation(null)),
                () -> assertEquals(-1L,
                        req28.getRegionPopulation("")),
                () -> assertEquals(-1L,
                        req28.getRegionPopulation("   "))
        );
    }

    /**
     * Confirms the population returned by the database is used.
     */
    @Test
    public void testValidRegionPopulation() {
        AtomicReference<String> selectedRegion =
                new AtomicReference<>();

        Connection connection = fakeConnection(
                38000000L, false, null, selectedRegion);

        Requirement28 req28 = new Requirement28(connection);

        assertEquals(38000000L,
                req28.getRegionPopulation("Caribbean"));

        assertEquals("Caribbean", selectedRegion.get());
    }

    /**
     * Confirms region names are trimmed before querying.
     */
    @Test
    public void testRegionNameTrimmed() {
        AtomicReference<String> selectedRegion =
                new AtomicReference<>();

        Connection connection = fakeConnection(
                5000L, false, null, selectedRegion);

        Requirement28 req28 = new Requirement28(connection);

        assertEquals(5000L,
                req28.getRegionPopulation("  Caribbean  "));

        assertEquals("Caribbean", selectedRegion.get());
    }

    /**
     * A SQL NULL sum means no matching region was found.
     */
    @Test
    public void testRegionNotFound() {
        Connection connection = fakeConnection(
                0L, true, null, null);

        Requirement28 req28 = new Requirement28(connection);

        assertEquals(-1L,
                req28.getRegionPopulation("Unknown Region"));
    }

    /**
     * Checks that an SQL exception returns -1.
     */
    @Test
    public void testDatabaseError() {
        Connection connection = fakeConnection(
                0L, false,
                new SQLException("Database unavailable"),
                null);

        Requirement28 req28 = new Requirement28(connection);

        assertEquals(-1L,
                req28.getRegionPopulation("Caribbean"));
    }

    /**
     * Confirms successful output clearly displays the population.
     */
    @Test
    public void testPrintRegionPopulation() {
        Connection connection = fakeConnection(
                38000000L, false, null, null);

        Requirement28 req28 = new Requirement28(connection);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(
                    output, true, StandardCharsets.UTF_8));

            req28.printRegionPopulation("Caribbean");

        } finally {
            System.setOut(originalOut);
        }

        String text = output.toString(StandardCharsets.UTF_8);

        assertTrue(text.contains("Caribbean"));
        assertTrue(text.contains("38,000,000"));
    }

    /**
     * Creates a simulated JDBC connection for unit testing.
     *
     * The fake objects support the JDBC calls used by
     * Requirement28 without connecting to MySQL.
     */
    private Connection fakeConnection(
            long population,
            boolean sqlNull,
            SQLException failure,
            AtomicReference<String> selectedRegion) {

        ResultSet resultSet = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "next":
                            return true;
                        case "getLong":
                            return population;
                        case "wasNull":
                            return sqlNull;
                        case "close":
                            return null;
                        default:
                            return defaultValue(method.getReturnType());
                    }
                });

        PreparedStatement statement =
                (PreparedStatement) Proxy.newProxyInstance(
                        PreparedStatement.class.getClassLoader(),
                        new Class<?>[]{PreparedStatement.class},
                        (proxy, method, args) -> {
                            switch (method.getName()) {
                                case "setString":
                                    if (selectedRegion != null) {
                                        selectedRegion.set(
                                                (String) args[1]);
                                    }
                                    return null;
                                case "executeQuery":
                                    return resultSet;
                                case "close":
                                    return null;
                                default:
                                    return defaultValue(
                                            method.getReturnType());
                            }
                        });

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "prepareStatement":
                            if (failure != null) {
                                throw failure;
                            }
                            return statement;
                        case "close":
                            return null;
                        default:
                            return defaultValue(
                                    method.getReturnType());
                    }
                });
    }

    /**
     * Supplies default values for unused JDBC methods.
     */
    private Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == double.class) {
            return 0.0;
        }
        if (returnType == float.class) {
            return 0.0f;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }
}
