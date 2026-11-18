package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-23: Population Living and Not Living in Cities by Continent.
 */
public class Requirement23Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getContinentPopulationBreakdownNullConnectionTest() {
        Requirement23 req23 = new Requirement23(null);
        ArrayList<String> result = req23.getContinentPopulationBreakdown();
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that printReport handles null lists gracefully.
     */
    @Test
    public void printReportNullListTest() {
        Requirement23 req23 = new Requirement23(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req23.printReport(null);
            assertTrue(output.toString().contains("No continent population breakdown data available."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printReport handles empty lists gracefully.
     */
    @Test
    public void printReportEmptyListTest() {
        Requirement23 req23 = new Requirement23(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req23.printReport(new ArrayList<>());
            assertTrue(output.toString().contains("No continent population breakdown data available."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printReport formats valid breakdown rows properly.
     */
    @Test
    public void printReportValidListTest() {
        Requirement23 req23 = new Requirement23(null);
        ArrayList<String> rows = new ArrayList<>();
        rows.add("Europe                      730,074,600        241,942,813 (33.14%)        488,131,787 (66.86%)");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req23.printReport(rows);
            String printed = output.toString();
            assertTrue(printed.contains("Europe"));
            assertTrue(printed.contains("730,074,600"));
            assertTrue(printed.contains("Total continents displayed: 1"));
        } finally {
            System.setOut(originalOut);
        }
    }
}