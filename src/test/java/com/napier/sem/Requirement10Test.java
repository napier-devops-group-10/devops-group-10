package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-10: Cities in a Country by Population.
 */
public class Requirement10Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getCitiesInCountryNullConnectionTest() {
        Requirement10 req10 = new Requirement10(null);
        ArrayList<City> result = req10.getCitiesInCountry("Spain");
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that null or blank country parameters are rejected safely.
     */
    @Test
    public void getCitiesInCountryInvalidParamTest() {
        Requirement10 req10 = new Requirement10(null);
        assertTrue(req10.getCitiesInCountry(null).isEmpty());
        assertTrue(req10.getCitiesInCountry("   ").isEmpty());
    }

    /**
     * Verifies that printCities handles empty lists gracefully.
     */
    @Test
    public void printCitiesEmptyListTest() {
        Requirement10 req10 = new Requirement10(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req10.printCities(new ArrayList<>(), "Spain");
            assertTrue(output.toString().contains("No cities found for country: Spain"));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printCities formats valid City records properly.
     */
    @Test
    public void printCitiesValidListTest() {
        Requirement10 req10 = new Requirement10(null);
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
            req10.printCities(cities, "Spain");
            String printed = output.toString();
            assertTrue(printed.contains("Madrid"));
            assertTrue(printed.contains("2879052"));
            assertTrue(printed.contains("Total cities displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}