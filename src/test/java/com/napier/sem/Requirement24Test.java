package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-24: Population Living and Not Living in Cities by Region.
 */
public class Requirement24Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getRegionPopulationBreakdownNullConnectionTest() {
        Requirement24 req24 = new Requirement24(null);
        ArrayList<String> result = req24.getRegionPopulationBreakdown();
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that printReport handles null lists gracefully.
     */
    @Test
    public void printReportNullListTest() {
        Requirement24 req24 = new Requirement24(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req24.printReport(null);
            assertTrue(output.toString().contains("No region population breakdown data available."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printReport handles empty lists gracefully.
     */
    @Test
    public void printReportEmptyListTest() {
        Requirement24 req24 = new Requirement24(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req24.printReport(new ArrayList<>());
            assertTrue(output.toString().contains("No region population breakdown data available."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printReport formats valid breakdown rows properly.
     */
    @Test
    public void printReportValidListTest() {
        Requirement24 req24 = new Requirement24(null);
        ArrayList<String> rows = new ArrayList<>();
        rows.add("Western Europe                      183,247,600         45,683,298 (24.93%)        137,564,302 (75.07%)");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req24.printReport(rows);
            String printed = output.toString();
            assertTrue(printed.contains("Western Europe"));
            assertTrue(printed.contains("183,247,600"));
            assertTrue(printed.contains("Total regions displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}