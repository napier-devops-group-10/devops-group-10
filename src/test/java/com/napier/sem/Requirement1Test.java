package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-01: Countries in the World by Population.
 */
public class Requirement1Test {

    @Test
    public void getAllCountriesWorldNullConnectionTest() {
        Requirement1 req1 = new Requirement1(null);
        assertTrue(req1.getAllCountriesWorld().isEmpty());
    }

    @Test
    public void printCountriesEmptyOrNullTest() {
        Requirement1 req1 = new Requirement1(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req1.printCountries(null);
            req1.printCountries(new ArrayList<>());
            assertTrue(output.toString().contains("No countries found in the world."));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void printCountriesValidListTest() {
        Requirement1 req1 = new Requirement1(null);
        ArrayList<Country> countries = new ArrayList<>();
        Country c = new Country();
        c.setCode("CHN");
        c.setName("China");
        c.setContinent("Asia");
        c.setRegion("Eastern Asia");
        c.setPopulation(1277558000);
        c.setCapital("Peking");
        countries.add(c);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req1.printCountries(countries);
            String printed = output.toString();
            assertTrue(printed.contains("China"));
            assertTrue(printed.contains("1277558000"));
            assertTrue(printed.contains("Total records displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}