package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-02: Countries in a Continent by Population.
 */
public class Requirement2Test {

    @Test
    public void getCountriesByContinentNullConnectionTest() {
        Requirement2 req2 = new Requirement2(null);
        assertTrue(req2.getCountriesByContinent("Europe").isEmpty());
    }

    @Test
    public void getCountriesByContinentInvalidParamTest() {
        Requirement2 req2 = new Requirement2(null);
        assertTrue(req2.getCountriesByContinent(null).isEmpty());
        assertTrue(req2.getCountriesByContinent("   ").isEmpty());
    }

    @Test
    public void printCountriesEmptyListTest() {
        Requirement2 req2 = new Requirement2(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req2.printCountries(new ArrayList<>(), "Europe");
            assertTrue(output.toString().contains("No countries found for continent: Europe"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void printCountriesValidListTest() {
        Requirement2 req2 = new Requirement2(null);
        ArrayList<Country> countries = new ArrayList<>();
        Country c = new Country();
        c.setCode("DEU");
        c.setName("Germany");
        c.setContinent("Europe");
        c.setRegion("Western Europe");
        c.setPopulation(82164700);
        c.setCapital("Berlin");
        countries.add(c);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req2.printCountries(countries, "Europe");
            String printed = output.toString();
            assertTrue(printed.contains("Germany"));
            assertTrue(printed.contains("Total records displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}