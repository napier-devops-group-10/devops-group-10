package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-31: Population of a City.
 */
public class Requirement31Test {

    /**
     * Verifies that a missing database connection returns -1 safely.
     */
    @Test
    public void getCityPopulationNullConnectionTest() {
        Requirement31 req31 = new Requirement31(null);
        long population = req31.getCityPopulation("Palma de Mallorca");
        assertEquals(-1L, population, "Expected -1 when database connection is null");
    }

    /**
     * Verifies that a null city parameter is rejected safely.
     */
    @Test
    public void getCityPopulationNullParamTest() {
        Requirement31 req31 = new Requirement31(null);
        long population = req31.getCityPopulation(null);
        assertEquals(-1L, population, "Expected -1 when city name is null");
    }

    /**
     * Verifies that an empty/blank city string is rejected safely.
     */
    @Test
    public void getCityPopulationEmptyParamTest() {
        Requirement31 req31 = new Requirement31(null);
        long population = req31.getCityPopulation("   ");
        assertEquals(-1L, population, "Expected -1 when city name is empty");
    }

    /**
     * Verifies that printCityPopulation displays an unavailable message when population cannot be retrieved.
     */
    @Test
    public void printCityPopulationUnavailableTest() {
        Requirement31 req31 = new Requirement31(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req31.printCityPopulation("Palma de Mallorca");
            assertTrue(output.toString().contains("Population unavailable for city: Palma de Mallorca"));
        } finally {
            System.setOut(originalOut);
        }
    }
}