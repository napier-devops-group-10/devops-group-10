
package com.napier.sem;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {

    /**
     * REQ-27: Verify that a missing database
     * connection is handled correctly.
     */
    @Test
    void getContinentPopulationWithoutConnectionTest() {
        App app = new App();

        long population = app.getContinentPopulation("Asia");

        assertEquals(-1L, population,
                "Expected -1 when no database connection exists.");
    }

    /**
     * REQ-27: Verify that a null continent
     * does not return a valid population.
     */
    @Test
    void getContinentPopulationNullInputTest() {
        App app = new App();

        long population = app.getContinentPopulation(null);

        assertEquals(-1L, population,
                "Expected -1 for an invalid continent.");
    }

    /**
     * REQ-27: Verify Asia's population against
     * the MySQL world database.
     */
    @Test
    void getContinentPopulationIntegrationTest() {

        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        Assumptions.assumeTrue(
                dbUser != null && !dbUser.isBlank()
                        && dbPassword != null && !dbPassword.isBlank(),
                "Database credentials not configured."
        );

        String dbHost = System.getenv().getOrDefault(
                "DB_HOST", "localhost");
        String dbPort = System.getenv().getOrDefault(
                "DB_PORT", "3307");

        App app = new App();

        try {
            app.connect(dbHost + ":" + dbPort, 0);

            long population = app.getContinentPopulation("Asia");

            assertEquals(
                    3705025700L,
                    population,
                    "Asia population must match the project database."
            );

            assertEquals(
                    -1L,
                    app.getContinentPopulation(null),
                    "Null continent must be rejected."
            );

        } finally {
            app.disconnect();
        }
    }
}
