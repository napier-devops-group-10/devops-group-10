package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-13: Top N Populated Cities in a Continent.
 */
public class Requirement13Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getTopNCitiesInContinentNullConnectionTest() {
        Requirement13 req13 = new Requirement13(null);
        ArrayList<City> result = req13.getTopNCitiesInContinent("Europe", 5);
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that null or blank continent parameters are rejected safely.
     */
    @Test
    public void getTopNCitiesInContinentInvalidContinentTest() {
        Requirement13 req13 = new Requirement13(null);
        assertTrue(req13.getTopNCitiesInContinent(null, 5).isEmpty());
        assertTrue(req13.getTopNCitiesInContinent("   ", 5).isEmpty());
    }

    /**
     * Verifies that non-positive N values (zero or negative) are rejected safely.
     */
    @Test
    public void getTopNCitiesInContinentInvalidNTest() {
        Requirement13 req13 = new Requirement13(null);
        assertTrue(req13.getTopNCitiesInContinent("Europe", 0).isEmpty());
        assertTrue(req13.getTopNCitiesInContinent("Asia", -3).isEmpty());
    }

    /**
     * Verifies that printCities handles empty lists gracefully.
     */
    @Test
    public void printCitiesEmptyListTest() {
        Requirement13 req13 = new Requirement13(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req13.printCities(new ArrayList<>(), "Europe", 5);
            assertTrue(output.toString().contains("No cities found for Top 5 cities in continent: Europe"));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printCities formats valid City records properly.
     */
    @Test
    public void printCitiesValidListTest() {
        Requirement13 req13 = new Requirement13(null);
        ArrayList<City> cities = new ArrayList<>();

        City c = new City();
        c.setId(456);
        c.setName("London");
        c.setCountryCode("GBR");
        c.setDistrict("England");
        c.setPopulation(7285000);
        cities.add(c);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req13.printCities(cities, "Europe", 1);
            String printed = output.toString();
            assertTrue(printed.contains("London"));
            assertTrue(printed.contains("7285000"));
            assertTrue(printed.contains("Total cities displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}