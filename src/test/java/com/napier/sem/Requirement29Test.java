package com.napier.sem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * REQ-29: Population of a Country.
 *
 * Unit tests for the Requirement29 class.
 */
public class Requirement29Test {

    /**
     * Verifies that the method handles a missing
     * database connection correctly.
     */
    @Test
    public void getCountryPopulationWithoutConnectionTest() {

        Requirement29 req29 = new Requirement29(null);

        long population = req29.getCountryPopulation("Grenada");

        assertEquals(-1L, population);
    }

    /**
     * Verifies that a null country name is rejected.
     */
    @Test
    public void getCountryPopulationNullNameTest() {

        Requirement29 req29 = new Requirement29(null);

        long population = req29.getCountryPopulation(null);

        assertEquals(-1L, population);
    }

    /**
     * Verifies that an empty country name is rejected.
     */
    @Test
    public void getCountryPopulationEmptyNameTest() {

        Requirement29 req29 = new Requirement29(null);

        long population = req29.getCountryPopulation("");

        assertEquals(-1L, population);
    }
    @Test
    public void printCountryPopulationWithoutConnectionTest() {

        Requirement29 req29 = new Requirement29(null);

        java.io.ByteArrayOutputStream output =
                new java.io.ByteArrayOutputStream();

        java.io.PrintStream originalOut = System.out;

        try {
            System.setOut(new java.io.PrintStream(output));

            req29.printCountryPopulation("Grenada");

            org.junit.jupiter.api.Assertions.assertTrue(
                    output.toString().contains(
                            "Population unavailable for: Grenada"
                    )
            );

        } finally {
            System.setOut(originalOut);
        }
    }
}