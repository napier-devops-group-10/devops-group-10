package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-20: Top N Populated Capital Cities in the World.
 */
public class Requirement20Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getTopNCapitalCitiesWorldNullConnectionTest() {
        Requirement20 req20 = new Requirement20(null);
        ArrayList<City> result = req20.getTopNCapitalCitiesWorld(10);
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that non-positive N values (zero or negative) are rejected safely.
     */
    @Test
    public void getTopNCapitalCitiesWorldInvalidNTest() {
        Requirement20 req20 = new Requirement20(null);
        assertTrue(req20.getTopNCapitalCitiesWorld(0).isEmpty());
        assertTrue(req20.getTopNCapitalCitiesWorld(-5).isEmpty());
    }

    /**
     * Verifies that printCapitalCities handles null or empty lists gracefully.
     */
    @Test
    public void printCapitalCitiesEmptyListTest() {
        Requirement20 req20 = new Requirement20(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req20.printCapitalCities(new ArrayList<>(), 5);
            assertTrue(output.toString().contains("No capital cities found for Top 5 capital cities in the world."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printCapitalCities formats valid capital City records properly.
     */
    @Test
    public void printCapitalCitiesValidListTest() {
        Requirement20 req20 = new Requirement20(null);
        ArrayList<City> cities = new ArrayList<>();

        City c = new City();
        c.setId(2331);
        c.setName("Seoul");
        c.setCountryCode("KOR");
        c.setDistrict("Seoul");
        c.setPopulation(9981619);
        cities.add(c);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req20.printCapitalCities(cities, 1);
            String printed = output.toString();
            assertTrue(printed.contains("Seoul"));
            assertTrue(printed.contains("9981619"));
            assertTrue(printed.contains("Total capital cities displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}