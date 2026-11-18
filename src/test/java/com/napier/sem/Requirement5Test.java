package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-05: Top N Populated Countries in a Continent.
 */
public class Requirement5Test {

    @Test
    public void getTopNCountriesInContinentNullConnectionTest() {
        Requirement5 req5 = new Requirement5(null);
        assertTrue(req5.getTopNCountriesInContinent("Europe", 5).isEmpty());
    }

    @Test
    public void getTopNCountriesInContinentInvalidParamsTest() {
        Requirement5 req5 = new Requirement5(null);
        assertTrue(req5.getTopNCountriesInContinent(null, 5).isEmpty());
        assertTrue(req5.getTopNCountriesInContinent("   ", 5).isEmpty());
        assertTrue(req5.getTopNCountriesInContinent("Europe", 0).isEmpty());
        assertTrue(req5.getTopNCountriesInContinent("Asia", -3).isEmpty());
    }

    @Test
    public void printCountriesEmptyOrNullTest() {
        Requirement5 req5 = new Requirement5(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req5.printCountries(null, "Europe", 5);
            req5.printCountries(new ArrayList<>(), "Europe", 5);
            assertTrue(output.toString().contains("No countries to display for continent: Europe"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void printCountriesValidListTest() {
        Requirement5 req5 = new Requirement5(null);
        ArrayList<Country> countries = new ArrayList<>();

        Country c = new Country();
        c.setCode("RUS");
        c.setName("Russian Federation");
        c.setContinent("Europe");
        c.setRegion("Eastern Europe");
        c.setPopulation(146934000);
        c.setCapital("Moscow");
        countries.add(c);
        countries.add(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req5.printCountries(countries, "Europe", 1);
            String printed = output.toString();
            assertTrue(printed.contains("Russian Federation"));
            assertTrue(printed.contains("146934000"));
            assertTrue(printed.contains("Total records displayed: 2"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void printCountriesOverloadedMethodTest() {
        Requirement5 req5 = new Requirement5(null);
        ArrayList<Country> countries = new ArrayList<>();

        Country c = new Country();
        c.setCode("JPN");
        c.setName("Japan");
        c.setContinent("Asia");
        c.setRegion("Eastern Asia");
        c.setPopulation(126714000);
        c.setCapital("Tokyo");
        countries.add(c);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req5.printCountries(countries);
            String printed = output.toString();
            assertTrue(printed.contains("Specified Continent"));
            assertTrue(printed.contains("Japan"));
        } finally {
            System.setOut(originalOut);
        }
    }
}