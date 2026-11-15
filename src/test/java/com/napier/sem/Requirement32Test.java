package com.napier.sem;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for REQ-32: Language Population Report.
 */
public class Requirement32Test {

    /**
     * Verifies that null database connection returns an empty list safely.
     */
    @Test
    public void getLanguagePopulationReportNullConnectionTest() {
        Requirement32 req32 = new Requirement32(null);
        ArrayList<String> result = req32.getLanguagePopulationReport();
        assertTrue(result.isEmpty(), "Expected empty list when database connection is null");
    }

    /**
     * Verifies that printReport handles null lists gracefully.
     */
    @Test
    public void printReportNullListTest() {
        Requirement32 req32 = new Requirement32(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req32.printReport(null);
            assertTrue(output.toString().contains("No language population data available."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printReport handles empty lists gracefully.
     */
    @Test
    public void printReportEmptyListTest() {
        Requirement32 req32 = new Requirement32(null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req32.printReport(new ArrayList<>());
            assertTrue(output.toString().contains("No language population data available."));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Verifies that printReport formats valid language rows properly.
     */
    @Test
    public void printReportValidListTest() {
        Requirement32 req32 = new Requirement32(null);
        ArrayList<String> rows = new ArrayList<>();
        rows.add("Chinese                1,191,843,539              19.61%");
        rows.add("Spanish                  355,029,462               5.84%");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            req32.printReport(rows);
            String printed = output.toString();
            assertTrue(printed.contains("Chinese"));
            assertTrue(printed.contains("1,191,843,539"));
            assertTrue(printed.contains("Spanish"));
            assertTrue(printed.contains("Total languages displayed: 2"));
        } finally {
            System.setOut(originalOut);
        }
    }
}