package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-07: Cities in the World by Population.
 */
public class Requirement7Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getAllCitiesWorldNullConnectionTest() {
        Requirement7 req7 = new Requirement7(null);
        ArrayList<City> result = req7.getAllCitiesWorld();
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
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
            assertTrue(output.toString().contains("No cities found in the world."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
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
            assertTrue(output.toString().contains("No cities found in the world."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printCities formats valid City records properly.
     */
    @Test
    public void printCitiesValidListTest() {
        Requirement7 req7 = new Requirement7(null);
        ArrayList<City> cities = new ArrayList<>();

        City c = new City();
        c.setId(1024);
        c.setName("Mumbai (Bombay)");
        c.setCountryCode("IND");
        c.setDistrict("Maharashtra");
        c.setPopulation(10500000);
        cities.add(c);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req7.printCities(cities);
            String printed = output.toString();
            assertTrue(printed.contains("Mumbai (Bombay)"));
            assertTrue(printed.contains("10500000"));
            assertTrue(printed.contains("Total cities displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}