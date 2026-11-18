package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-21: Top N Populated Capital Cities in a Continent.
 */
public class Requirement21Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getTopNCapitalCitiesInContinentNullConnectionTest() {
        Requirement21 req21 = new Requirement21(null);
        ArrayList<City> result = req21.getTopNCapitalCitiesInContinent("Europe", 5);
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that null or blank continent parameters are rejected safely.
     */
    @Test
    public void getTopNCapitalCitiesInContinentInvalidContinentTest() {
        Requirement21 req21 = new Requirement21(null);
        assertTrue(req21.getTopNCapitalCitiesInContinent(null, 5).isEmpty());
        assertTrue(req21.getTopNCapitalCitiesInContinent("   ", 5).isEmpty());
    }

    /**
     * Verifies that non-positive N values (zero or negative) are rejected safely.
     */
    @Test
    public void getTopNCapitalCitiesInContinentInvalidNTest() {
        Requirement21 req21 = new Requirement21(null);
        assertTrue(req21.getTopNCapitalCitiesInContinent("Europe", 0).isEmpty());
        assertTrue(req21.getTopNCapitalCitiesInContinent("Asia", -3).isEmpty());
    }

    /**
     * Verifies that printCapitalCities handles empty lists gracefully.
     */
    @Test
    public void printCapitalCitiesEmptyListTest() {
        Requirement21 req21 = new Requirement21(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req21.printCapitalCities(new ArrayList<>(), "Europe", 5);
            assertTrue(output.toString().contains("No capital cities found for Top 5 capital cities in continent: Europe"));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printCapitalCities formats valid capital City records properly.
     */
    @Test
    public void printCapitalCitiesValidListTest() {
        Requirement21 req21 = new Requirement21(null);
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
            req21.printCapitalCities(cities, "Europe", 1);
            String printed = output.toString();
            assertTrue(printed.contains("London"));
            assertTrue(printed.contains("7285000"));
            assertTrue(printed.contains("Total capital cities displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}