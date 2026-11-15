package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-06: Top N Populated Countries in a Region.
 */
public class Requirement6Test {

    @Test
    public void getTopCountriesInRegionNullConnectionTest() {
        Requirement6 req6 = new Requirement6(null);
        assertTrue(req6.getTopCountriesInRegion("Caribbean", 5).isEmpty());
    }

    @Test
    public void getTopCountriesInRegionInvalidParamsTest() {
        Requirement6 req6 = new Requirement6(null);
        assertTrue(req6.getTopCountriesInRegion(null, 5).isEmpty());
        assertTrue(req6.getTopCountriesInRegion("   ", 5).isEmpty());
        assertTrue(req6.getTopCountriesInRegion("Caribbean", 0).isEmpty());
        assertTrue(req6.getTopCountriesInRegion("Western Europe", -2).isEmpty());
    }

    @Test
    public void printReportEmptyOrNullTest() {
        Requirement6 req6 = new Requirement6(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req6.printReport(null, "Caribbean", 5);
            req6.printReport(new ArrayList<>(), "Caribbean", 5);
            assertTrue(output.toString().contains("No countries found for region: Caribbean"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void printReportValidListTest() {
        Requirement6 req6 = new Requirement6(null);
        ArrayList<Country> countries = new ArrayList<>();

        Country c = new Country();
        c.setCode("CUB");
        c.setName("Cuba");
        c.setContinent("North America");
        c.setRegion("Caribbean");
        c.setPopulation(11201000);
        c.setCapital("La Habana");
        countries.add(c);
        countries.add(null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req6.printReport(countries, "Caribbean", 1);
            String printed = output.toString();
            assertTrue(printed.contains("Top 1 Populated Countries in Region: Caribbean"));
            assertTrue(printed.contains("Cuba"));
            assertTrue(printed.contains("La Habana"));
        } finally {
            System.setOut(originalOut);
        }
    }
}