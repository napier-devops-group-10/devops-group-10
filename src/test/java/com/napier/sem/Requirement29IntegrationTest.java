package com.napier.sem;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for REQ-29:
 * Population of a Country.
 */
public class Requirement29IntegrationTest {

    @Test
    public void getGrenadaPopulationIntegrationTest() throws Exception {

        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        assertNotNull(user, "DB_USER environment variable is missing");
        assertNotNull(password, "DB_PASSWORD environment variable is missing");

        String url = "jdbc:mysql://localhost:3307/world"
                + "?allowPublicKeyRetrieval=true&useSSL=false";

        try (Connection con =
                     DriverManager.getConnection(url, user, password)) {

            Requirement29 req29 = new Requirement29(con);

            long population = req29.getCountryPopulation("Grenada");

            assertEquals(94000L, population,
                    "Grenada's population should match the world database");
        }
    }
    @Test
    public void printGrenadaPopulationIntegrationTest() throws Exception {

        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        assertNotNull(user, "DB_USER environment variable is missing");
        assertNotNull(password, "DB_PASSWORD environment variable is missing");

        String url = "jdbc:mysql://localhost:3307/world"
                + "?allowPublicKeyRetrieval=true&useSSL=false";

        try (Connection con =
                     DriverManager.getConnection(url, user, password)) {

            Requirement29 req29 = new Requirement29(con);

            java.io.ByteArrayOutputStream output =
                    new java.io.ByteArrayOutputStream();

            java.io.PrintStream originalOut = System.out;

            try {
                System.setOut(new java.io.PrintStream(output));

                req29.printCountryPopulation("Grenada");

            } finally {
                System.setOut(originalOut);
            }

            assertEquals(
                    "Population of Grenada: 94,000",
                    output.toString().trim()
            );
        }
    }
}