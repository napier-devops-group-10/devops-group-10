package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * REQ-29: Population of a Country.
 *
 * Retrieves the population of a specified country
 * from the MySQL world database.
 */
public class Requirement29 {

    private final Connection con;

    /**
     * Constructor receives the existing database connection.
     *
     * @param con Active database connection.
     */
    public Requirement29(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves the population of a country by its name.
     *
     * @param countryName Name of the country.
     * @return Country population, or -1 if unavailable.
     */
    public long getCountryPopulation(String countryName) {

        if (con == null || countryName == null
                || countryName.trim().isEmpty()) {
            return -1;
        }

        String query = "SELECT Population FROM country WHERE Name = ?";

        try (PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setString(1, countryName.trim());

            try (ResultSet rset = pstmt.executeQuery()) {

                if (rset.next()) {
                    return rset.getLong("Population");
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "Failed to retrieve country population: "
                            + e.getMessage()
            );
        }

        return -1;
    }

    /**
     * Displays the population of the specified country.
     *
     * @param countryName Name of the country.
     */
    public void printCountryPopulation(String countryName) {

        long population = getCountryPopulation(countryName);

        if (population == -1) {
            System.out.println(
                    "Population unavailable for: " + countryName
            );
            return;
        }

        System.out.printf(
                "Population of %s: %,d%n",
                countryName,
                population
        );
    }
}