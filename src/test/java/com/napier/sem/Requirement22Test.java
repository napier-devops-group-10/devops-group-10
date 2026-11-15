package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-22: Top N Populated Capital Cities in a Region.
 */
public class Requirement22Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getTopNCapitalCitiesInRegionNullConnectionTest() {
        Requirement22 req22 = new Requirement22(null);
        ArrayList<City> result = req22.getTopNCapitalCitiesInRegion("Western Europe", 5);
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that null or blank region parameters are rejected safely.
     */
    @Test
    public void getTopNCapitalCitiesInRegionInvalidRegionTest() {
        Requirement22 req22 = new Requirement22(null);
        assertTrue(req22.getTopNCapitalCitiesInRegion(null, 5).isEmpty());
        assertTrue(req22.getTopNCapitalCitiesInRegion("   ", 5).isEmpty());
    }

    /**
     * Verifies that non-positive N values (zero or negative) are rejected safely.
     */
    @Test
    public void getTopNCapitalCitiesInRegionInvalidNTest() {
        Requirement22 req22 = new Requirement22(null);
        assertTrue(req22.getTopNCapitalCitiesInRegion("Western Europe", 0).isEmpty());
        assertTrue(req22.getTopNCapitalCitiesInRegion("Caribbean", -2).isEmpty());
    }

    /**
     * Verifies that printCapitalCities handles empty lists gracefully.
     */
    @Test
    public void printCapitalCitiesEmptyListTest() {
        Requirement22 req22 = new Requirement22(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req22.printCapitalCities(new ArrayList<>(), "Western Europe", 5);
            assertTrue(output.toString().contains("No capital cities found for Top 5 capital cities in region: Western Europe"));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printCapitalCities formats valid capital City records properly.
     */
    @Test
    public void printCapitalCitiesValidListTest() {
        Requirement22 req22 = new Requirement22(null);
        ArrayList<City> cities = new ArrayList<>();

        City c = new City();
        c.setId(3068);
        c.setName("Berlin");
        c.setCountryCode("DEU");
        c.setDistrict("Berliini");
        c.setPopulation(3386667);
        cities.add(c);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req22.printCapitalCities(cities, "Western Europe", 1);
            String printed = output.toString();
            assertTrue(printed.contains("Berlin"));
            assertTrue(printed.contains("3386667"));
            assertTrue(printed.contains("Total capital cities displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}