package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-11: Cities in a District by Population.
 */
public class Requirement11Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getCitiesInDistrictNullConnectionTest() {
        Requirement11 req11 = new Requirement11(null);
        ArrayList<City> result = req11.getCitiesInDistrict("Gelderland");
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that a null district parameter is handled safely.
     */
    @Test
    public void getCitiesInDistrictNullParamTest() {
        Requirement11 req11 = new Requirement11(null);
        ArrayList<City> result = req11.getCitiesInDistrict(null);
        assertTrue(result.isEmpty(), "Expected empty list when district is null");
    }

    /**
     * Verifies that an empty/blank district string is handled safely.
     */
    @Test
    public void getCitiesInDistrictEmptyParamTest() {
        Requirement11 req11 = new Requirement11(null);
        ArrayList<City> result = req11.getCitiesInDistrict("   ");
        assertTrue(result.isEmpty(), "Expected empty list when district is empty");
    }

    /**
     * Verifies that printCities handles null or empty lists without throwing exceptions.
     */
    @Test
    public void printCitiesEmptyListTest() {
        Requirement11 req11 = new Requirement11(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req11.printCities(new ArrayList<>(), "UnknownDistrict");
            assertTrue(output.toString().contains("No cities found for district: UnknownDistrict"));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printCities formats valid City records properly.
     */
    @Test
    public void printCitiesValidListTest() {
        Requirement11 req11 = new Requirement11(null);
        ArrayList<City> cities = new ArrayList<>();

        City c = new City();
        c.setId(1);
        c.setName("Palma de Mallorca");
        c.setCountryCode("ESP");
        c.setDistrict("Baleares");
        c.setPopulation(326993);
        cities.add(c);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req11.printCities(cities, "Baleares");
            String printed = output.toString();
            assertTrue(printed.contains("Palma de Mallorca"));
            assertTrue(printed.contains("326993"));
            assertTrue(printed.contains("Total cities displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}