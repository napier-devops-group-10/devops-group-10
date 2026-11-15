package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-08: Cities in a Continent by Population.
 */
public class Requirement8Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getCitiesInContinentNullConnectionTest() {
        Requirement8 req8 = new Requirement8(null);
        ArrayList<City> result = req8.getCitiesInContinent("Europe");
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that null or blank continent parameters are rejected safely.
     */
    @Test
    public void getCitiesInContinentInvalidParamTest() {
        Requirement8 req8 = new Requirement8(null);
        assertTrue(req8.getCitiesInContinent(null).isEmpty());
        assertTrue(req8.getCitiesInContinent("   ").isEmpty());
    }

    /**
     * Verifies that printCities handles empty lists gracefully.
     */
    @Test
    public void printCitiesEmptyListTest() {
        Requirement8 req8 = new Requirement8(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req8.printCities(new ArrayList<>(), "Europe");
            assertTrue(output.toString().contains("No cities found for continent: Europe"));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printCities formats valid City records properly.
     */
    @Test
    public void printCitiesValidListTest() {
        Requirement8 req8 = new Requirement8(null);
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
            req8.printCities(cities, "Europe");
            String printed = output.toString();
            assertTrue(printed.contains("London"));
            assertTrue(printed.contains("7285000"));
            assertTrue(printed.contains("Total cities displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}