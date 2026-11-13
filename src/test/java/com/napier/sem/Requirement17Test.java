package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-17: Capital Cities in the World by Population.
 */
public class Requirement17Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getAllCapitalCitiesWorldNullConnectionTest() {
        Requirement17 req17 = new Requirement17(null);
        ArrayList<City> result = req17.getAllCapitalCitiesWorld();
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that printCapitalCities handles null lists gracefully.
     */
    @Test
    public void printCapitalCitiesNullListTest() {
        Requirement17 req17 = new Requirement17(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req17.printCapitalCities(null);
            assertTrue(output.toString().contains("No capital cities found in the world."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printCapitalCities handles empty lists gracefully.
     */
    @Test
    public void printCapitalCitiesEmptyListTest() {
        Requirement17 req17 = new Requirement17(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req17.printCapitalCities(new ArrayList<>());
            assertTrue(output.toString().contains("No capital cities found in the world."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printCapitalCities formats valid capital City records properly.
     */
    @Test
    public void printCapitalCitiesValidListTest() {
        Requirement17 req17 = new Requirement17(null);
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
            req17.printCapitalCities(cities);
            String printed = output.toString();
            assertTrue(printed.contains("Seoul"));
            assertTrue(printed.contains("9981619"));
            assertTrue(printed.contains("Total capital cities displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}