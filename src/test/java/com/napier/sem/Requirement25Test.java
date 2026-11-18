package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-25: Population Living and Not Living in Cities by Country.
 */
public class Requirement25Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getCountryPopulationBreakdownNullConnectionTest() {
        Requirement25 req25 = new Requirement25(null);
        ArrayList<String> result = req25.getCountryPopulationBreakdown();
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that printReport handles null lists gracefully.
     */
    @Test
    public void printReportNullListTest() {
        Requirement25 req25 = new Requirement25(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req25.printReport(null);
            assertTrue(output.toString().contains("No country population breakdown data available."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printReport handles empty lists gracefully.
     */
    @Test
    public void printReportEmptyListTest() {
        Requirement25 req25 = new Requirement25(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req25.printReport(new ArrayList<>());
            assertTrue(output.toString().contains("No country population breakdown data available."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printReport formats valid breakdown rows properly.
     */
    @Test
    public void printReportValidListTest() {
        Requirement25 req25 = new Requirement25(null);
        ArrayList<String> rows = new ArrayList<>();
        rows.add("Spain                                          39,441,700         16,669,189 (42.26%)         22,772,511 (57.74%)");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req25.printReport(rows);
            String printed = output.toString();
            assertTrue(printed.contains("Spain"));
            assertTrue(printed.contains("39,441,700"));
            assertTrue(printed.contains("Total countries displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}