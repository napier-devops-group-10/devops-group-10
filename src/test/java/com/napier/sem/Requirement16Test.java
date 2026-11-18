package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-16: Top N Populated Cities in a District.
 */
public class Requirement16Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getTopNCitiesInDistrictNullConnectionTest() {
        Requirement16 req16 = new Requirement16(null);
        ArrayList<City> result = req16.getTopNCitiesInDistrict("Gelderland", 5);
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that null or blank district parameters are rejected safely.
     */
    @Test
    public void getTopNCitiesInDistrictInvalidDistrictTest() {
        Requirement16 req16 = new Requirement16(null);
        assertTrue(req16.getTopNCitiesInDistrict(null, 5).isEmpty());
        assertTrue(req16.getTopNCitiesInDistrict("   ", 5).isEmpty());
    }

    /**
     * Verifies that non-positive N values (zero or negative) are rejected safely.
     */
    @Test
    public void getTopNCitiesInDistrictInvalidNTest() {
        Requirement16 req16 = new Requirement16(null);
        assertTrue(req16.getTopNCitiesInDistrict("Gelderland", 0).isEmpty());
        assertTrue(req16.getTopNCitiesInDistrict("Baleares", -3).isEmpty());
    }

    /**
     * Verifies that printCities handles empty lists gracefully.
     */
    @Test
    public void printCitiesEmptyListTest() {
        Requirement16 req16 = new Requirement16(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req16.printCities(new ArrayList<>(), "Gelderland", 5);
            assertTrue(output.toString().contains("No cities found for Top 5 cities in district: Gelderland"));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printCities formats valid City records properly.
     */
    @Test
    public void printCitiesValidListTest() {
        Requirement16 req16 = new Requirement16(null);
        ArrayList<City> cities = new ArrayList<>();

        City c = new City();
        c.setId(658);
        c.setName("Palma de Mallorca");
        c.setCountryCode("ESP");
        c.setDistrict("Baleares");
        c.setPopulation(326993);
        cities.add(c);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req16.printCities(cities, "Baleares", 1);
            String printed = output.toString();
            assertTrue(printed.contains("Palma de Mallorca"));
            assertTrue(printed.contains("326993"));
            assertTrue(printed.contains("Total cities displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}