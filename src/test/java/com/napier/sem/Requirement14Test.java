package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-14: Top N Populated Cities in a Region.
 */
public class Requirement14Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getTopNCitiesInRegionNullConnectionTest() {
        Requirement14 req14 = new Requirement14(null);
        ArrayList<City> result = req14.getTopNCitiesInRegion("Western Europe", 5);
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that null or blank region parameters are rejected safely.
     */
    @Test
    public void getTopNCitiesInRegionInvalidRegionTest() {
        Requirement14 req14 = new Requirement14(null);
        assertTrue(req14.getTopNCitiesInRegion(null, 5).isEmpty());
        assertTrue(req14.getTopNCitiesInRegion("   ", 5).isEmpty());
    }

    /**
     * Verifies that non-positive N values (zero or negative) are rejected safely.
     */
    @Test
    public void getTopNCitiesInRegionInvalidNTest() {
        Requirement14 req14 = new Requirement14(null);
        assertTrue(req14.getTopNCitiesInRegion("Western Europe", 0).isEmpty());
        assertTrue(req14.getTopNCitiesInRegion("Caribbean", -2).isEmpty());
    }

    /**
     * Verifies that printCities handles empty lists gracefully.
     */
    @Test
    public void printCitiesEmptyListTest() {
        Requirement14 req14 = new Requirement14(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req14.printCities(new ArrayList<>(), "Western Europe", 5);
            assertTrue(output.toString().contains("No cities found for Top 5 cities in region: Western Europe"));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printCities formats valid City records properly.
     */
    @Test
    public void printCitiesValidListTest() {
        Requirement14 req14 = new Requirement14(null);
        ArrayList<City> cities = new ArrayList<>();

        City c = new City();
        c.setId(2974);
        c.setName("Paris");
        c.setCountryCode("FRA");
        c.setDistrict("Île-de-France");
        c.setPopulation(2125246);
        cities.add(c);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req14.printCities(cities, "Western Europe", 1);
            String printed = output.toString();
            assertTrue(printed.contains("Paris"));
            assertTrue(printed.contains("2125246"));
            assertTrue(printed.contains("Total cities displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}