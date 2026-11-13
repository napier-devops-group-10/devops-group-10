package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-19: Capital Cities in a Region by Population.
 */
public class Requirement19Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getCapitalCitiesInRegionNullConnectionTest() {
        Requirement19 req19 = new Requirement19(null);
        ArrayList<City> result = req19.getCapitalCitiesInRegion("Western Europe");
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that null or blank region parameters are rejected safely.
     */
    @Test
    public void getCapitalCitiesInRegionInvalidRegionTest() {
        Requirement19 req19 = new Requirement19(null);
        assertTrue(req19.getCapitalCitiesInRegion(null).isEmpty());
        assertTrue(req19.getCapitalCitiesInRegion("   ").isEmpty());
    }

    /**
     * Verifies that printCapitalCities handles empty lists gracefully.
     */
    @Test
    public void printCapitalCitiesEmptyListTest() {
        Requirement19 req19 = new Requirement19(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req19.printCapitalCities(new ArrayList<>(), "Western Europe");
            assertTrue(output.toString().contains("No capital cities found for region: Western Europe"));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printCapitalCities formats valid capital City records properly.
     */
    @Test
    public void printCapitalCitiesValidListTest() {
        Requirement19 req19 = new Requirement19(null);
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
            req19.printCapitalCities(cities, "Western Europe");
            String printed = output.toString();
            assertTrue(printed.contains("Berlin"));
            assertTrue(printed.contains("3386667"));
            assertTrue(printed.contains("Total capital cities displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}