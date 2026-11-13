package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-30: Population of a District.
 */
public class Requirement30Test {

    /**
     * Verifies that a missing database connection returns -1 safely.
     */
    @Test
    public void getDistrictPopulationNullConnectionTest() {
        Requirement30 req30 = new Requirement30(null);
        long population = req30.getDistrictPopulation("Baleares");
        assertEquals(-1L, population, "Expected -1 when database connection is null");
    }

    /**
     * Verifies that a null district parameter is rejected safely.
     */
    @Test
    public void getDistrictPopulationNullParamTest() {
        Requirement30 req30 = new Requirement30(null);
        long population = req30.getDistrictPopulation(null);
        assertEquals(-1L, population, "Expected -1 when district is null");
    }

    /**
     * Verifies that an empty/blank district string is rejected safely.
     */
    @Test
    public void getDistrictPopulationEmptyParamTest() {
        Requirement30 req30 = new Requirement30(null);
        long population = req30.getDistrictPopulation("   ");
        assertEquals(-1L, population, "Expected -1 when district is empty");
    }

    /**
     * Verifies that printDistrictPopulation displays an unavailable message when population cannot be retrieved.
     */
    @Test
    public void printDistrictPopulationUnavailableTest() {
        Requirement30 req30 = new Requirement30(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req30.printDistrictPopulation("Baleares");
            assertTrue(output.toString().contains("Population unavailable for district: Baleares"));
        } finally {
            System.setOut(originalOut);
        }
    }
}