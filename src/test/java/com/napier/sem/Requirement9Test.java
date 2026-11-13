package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-09: Cities in a Region by Population.
 */
public class Requirement9Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getCitiesInRegionNullConnectionTest() {
        Requirement9 req9 = new Requirement9(null);
        ArrayList<City> result = req9.getCitiesInRegion("Western Europe");
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that null or blank region parameters are rejected safely.
     */
    @Test
    public void getCitiesInRegionInvalidParamTest() {
        Requirement9 req9 = new Requirement9(null);
        assertTrue(req9.getCitiesInRegion(null).isEmpty());
        assertTrue(req9.getCitiesInRegion("   ").isEmpty());
    }

    /**
     * Verifies that printCities handles empty lists gracefully.
     */
    @Test
    public void printCitiesEmptyListTest() {
        Requirement9 req9 = new Requirement9(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req9.printCities(new ArrayList<>(), "Western Europe");
            assertTrue(output.toString().contains("No cities found for region: Western Europe"));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printCities formats valid City records properly.
     */
    @Test
    public void printCitiesValidListTest() {
        Requirement9 req9 = new Requirement9(null);
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
            req9.printCities(cities, "Western Europe");
            String printed = output.toString();
            assertTrue(printed.contains("Paris"));
            assertTrue(printed.contains("2125246"));
            assertTrue(printed.contains("Total cities displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}