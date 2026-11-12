package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * REQ-06: The top N populated countries in a region where N is provided by the user.
 */
public class Requirement6 {

    // Database connection reference
    private Connection con;

    // Constructor to pass the database connection
    public Requirement6(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves the top N most populated countries in a specific region.
     *
     * @param region The name of the region to filter by (e.g., "Caribbean").
     * @param n      The maximum number of records to return.
     * @return An ArrayList of Country objects.
     */
    public ArrayList<Country> getTopCountriesInRegion(String region, int n) {
        ArrayList<Country> countries = new ArrayList<>();

        // Validate boundary conditions
        if (con == null || region == null || region.trim().isEmpty() || n <= 0) {
            System.out.println("Invalid input parameters or database connection is null.");
            return countries;
        }

        // Left join to city table to resolve the capital city name from city.ID
        String strSelect = "SELECT c.Code, c.Name, c.Continent, c.Region, c.SurfaceArea, c.Population, "
                + "       COALESCE(ci.Name, 'N/A') AS Capital "
                + "FROM country c "
                + "LEFT JOIN city ci ON c.Capital = ci.ID "
                + "WHERE c.Region = ? "
                + "ORDER BY c.Population DESC "
                + "LIMIT ?";

        try (PreparedStatement pstmt = con.prepareStatement(strSelect)) {
            pstmt.setString(1, region.trim());
            pstmt.setInt(2, n);

            ResultSet rset = pstmt.executeQuery();

            while (rset.next()) {
                Country c = new Country();
                c.setCode(rset.getString("Code"));
                c.setName(rset.getString("Name"));
                c.setContinent(rset.getString("Continent"));
                c.setRegion(rset.getString("Region"));
                c.setSurfaceArea(rset.getDouble("SurfaceArea"));
                c.setPopulation(rset.getInt("Population"));
                c.setCapital(rset.getString("Capital"));

                countries.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Failed to get country details: " + e.getMessage());
        }

        return countries;
    }

    /**
     * Prints the list of countries retrieved for the report.
     *
     * @param countries The list of Country objects to output.
     * @param region    The region name used for the header.
     * @param n         The number of countries requested.
     */
    public void printReport(ArrayList<Country> countries, String region, int n) {
        if (countries == null || countries.isEmpty()) {
            System.out.println("No countries found for region: " + region);
            return;
        }

        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println("Top " + n + " Populated Countries in Region: " + region);
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-6s %-35s %-20s %-25s %-12s %-20s%n",
                "Code", "Name", "Continent", "Region", "Population", "Capital");
        System.out.println("-----------------------------------------------------------------------------------------------------------------");

        for (Country c : countries) {
            if (c != null) {
                System.out.printf("%-6s %-35s %-20s %-25s %,12d %-20s%n",
                        c.getCode(),
                        c.getName(),
                        c.getContinent(),
                        c.getRegion(),
                        c.getPopulation(),
                        c.getCapital());
            }
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------\n");
    }
}