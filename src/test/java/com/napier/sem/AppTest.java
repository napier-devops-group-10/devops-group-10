
package com.napier.sem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import static org.junit.jupiter.api.Assertions.assertEquals;
public class AppTest {

    /**
     * REQ-26: Verify that the application handles
     * a missing database connection correctly.
     */
    @Test
    void getWorldPopulationWithoutConnectionTest() {
        App app = new App();

        long population = app.getWorldPopulation();

        assertEquals(-1L, population,
                "Expected -1 when no database connection exists.");
    }

    /**
     * REQ-26: Integration test for world population.
     * Requires MySQL and the project world database.
     */
    @Test
    void getWorldPopulationIntegrationTest() {

        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        Assumptions.assumeTrue(
                dbUser != null && dbPassword != null,
                "Database credentials not configured. Skipping integration test."
        );

        App app = new App();

        try {
            app.connect("localhost:3307", 0);

            long population = app.getWorldPopulation();

            assertEquals(
                    6078749450L,
                    population,
                    "World population should match the project database."
            );

        } finally {
            app.disconnect();
        }
    }

}


