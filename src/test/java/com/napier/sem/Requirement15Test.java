package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-15: Top N Populated Cities in a Country.
 */
public class Requirement15Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getTopNCitiesInCountryNullConnectionTest() {
        Requirement15 req15 = new Requirement15(null);
        ArrayList<City> result = req15.getTopNCitiesInCountry("Spain", 5);
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that null or blank country parameters are rejected safely.
     */
    @Test
    public void getTopNCitiesInCountryInvalidCountryTest() {
        Requirement15 req15 = new Requirement15(null);
        assertTrue(req15.getTopNCitiesInCountry(null, 5).isEmpty());
        assertTrue(req15.getTopNCitiesInCountry("   ", 5).isEmpty());
    }

    /**
     * Verifies that non-positive N values (zero or negative) are rejected safely.
     */
    @Test
    public void getTopNCitiesInCountryInvalidNTest() {
        Requirement15 req15 = new Requirement15(null);
        assertTrue(req15.getTopNCitiesInCountry("Spain", 0).isEmpty());
        assertTrue(req15.getTopNCitiesInCountry("France", -4).isEmpty());
    }

    /**
     * Verifies that printCities handles empty lists gracefully.
     */
    @Test
    public void printCitiesEmptyListTest() {
        Requirement15 req15 = new Requirement15(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req15.printCities(new ArrayList<>(), "Spain", 5);
            assertTrue(output.toString().contains("No cities found for Top 5 cities in country: Spain"));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printCities formats valid City records properly.
     */
    @Test
    public void printCitiesValidListTest() {
        Requirement15 req15 = new Requirement15(null);
        ArrayList<City> cities = new ArrayList<>();

        City c = new City();
        c.setId(653);
        c.setName("Madrid");
        c.setCountryCode("ESP");
        c.setDistrict("Madrid");
        c.setPopulation(2879052);
        cities.add(c);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req15.printCities(cities, "Spain", 1);
            String printed = output.toString();
            assertTrue(printed.contains("Madrid"));
            assertTrue(printed.contains("2879052"));
            assertTrue(printed.contains("Total cities displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}