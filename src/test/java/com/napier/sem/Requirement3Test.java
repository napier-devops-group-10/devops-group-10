package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-03: Countries in a Region by Population.
 */
public class Requirement3Test {

    @Test
    public void getCountriesByRegionNullConnectionTest() {
        Requirement3 req3 = new Requirement3(null);
        assertTrue(req3.getCountriesByRegion("Western Europe").isEmpty());
    }

    @Test
    public void getCountriesByRegionInvalidParamTest() {
        Requirement3 req3 = new Requirement3(null);
        assertTrue(req3.getCountriesByRegion(null).isEmpty());
        assertTrue(req3.getCountriesByRegion("   ").isEmpty());
    }

    @Test
    public void printCountriesEmptyListTest() {
        Requirement3 req3 = new Requirement3(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req3.printCountries(new ArrayList<>(), "Western Europe");
            assertTrue(output.toString().contains("No countries found for region: Western Europe"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void printCountriesValidListTest() {
        Requirement3 req3 = new Requirement3(null);
        ArrayList<Country> countries = new ArrayList<>();
        Country c = new Country();
        c.setCode("FRA");
        c.setName("France");
        c.setContinent("Europe");
        c.setRegion("Western Europe");
        c.setPopulation(59225700);
        c.setCapital("Paris");
        countries.add(c);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req3.printCountries(countries, "Western Europe");
            String printed = output.toString();
            assertTrue(printed.contains("France"));
            assertTrue(printed.contains("Total records displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}