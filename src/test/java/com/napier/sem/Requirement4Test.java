package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-04: Top N Populated Countries in the World.
 */
public class Requirement4Test {

    @Test
    public void getTopNCountriesWorldNullConnectionTest() {
        Requirement4 req4 = new Requirement4(null);
        assertTrue(req4.getTopNCountriesWorld(10).isEmpty());
    }

    @Test
    public void getTopNCountriesWorldInvalidNTest() {
        Requirement4 req4 = new Requirement4(null);
        assertTrue(req4.getTopNCountriesWorld(0).isEmpty());
        assertTrue(req4.getTopNCountriesWorld(-5).isEmpty());
    }

    @Test
    public void printCountriesEmptyListTest() {
        Requirement4 req4 = new Requirement4(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req4.printCountries(new ArrayList<>(), 10);
            assertTrue(output.toString().contains("No countries found for Top 10 countries in the world."));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void printCountriesValidListTest() {
        Requirement4 req4 = new Requirement4(null);
        ArrayList<Country> countries = new ArrayList<>();
        Country c = new Country();
        c.setCode("IND");
        c.setName("India");
        c.setContinent("Asia");
        c.setRegion("Southern and Central Asia");
        c.setPopulation(1013662000);
        c.setCapital("New Delhi");
        countries.add(c);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req4.printCountries(countries, 1);
            String printed = output.toString();
            assertTrue(printed.contains("India"));
            assertTrue(printed.contains("Total records displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}