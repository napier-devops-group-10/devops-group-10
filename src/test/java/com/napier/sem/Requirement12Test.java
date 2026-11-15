package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-12: Top N Populated Cities in the World.
 */
public class Requirement12Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getTopNCitiesWorldNullConnectionTest() {
        Requirement12 req12 = new Requirement12(null);
        ArrayList<City> result = req12.getTopNCitiesWorld(10);
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that zero value for N is rejected safely.
     */
    @Test
    public void getTopNCitiesWorldZeroParamTest() {
        Requirement12 req12 = new Requirement12(null);
        ArrayList<City> result = req12.getTopNCitiesWorld(0);
        assertTrue(result.isEmpty(), "Expected empty list when N is 0");
    }

    /**
     * Verifies that negative value for N is rejected safely.
     */
    @Test
    public void getTopNCitiesWorldNegativeParamTest() {
        Requirement12 req12 = new Requirement12(null);
        ArrayList<City> result = req12.getTopNCitiesWorld(-5);
        assertTrue(result.isEmpty(), "Expected empty list when N is negative");
    }

    /**
     * Verifies that printCities handles empty lists gracefully.
     */
    @Test
    public void printCitiesEmptyListTest() {
        Requirement12 req12 = new Requirement12(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req12.printCities(new ArrayList<>(), 5);
            assertTrue(output.toString().contains("No cities found for Top 5 cities in the world."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printCities formats valid City records properly.
     */
    @Test
    public void printCitiesValidListTest() {
        Requirement12 req12 = new Requirement12(null);
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
            req12.printCities(cities, 1);
            String printed = output.toString();
            assertTrue(printed.contains("Mumbai (Bombay)"));
            assertTrue(printed.contains("10500000"));
            assertTrue(printed.contains("Total cities displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}