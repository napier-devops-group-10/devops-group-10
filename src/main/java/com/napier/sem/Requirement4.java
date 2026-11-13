package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * REQ-04: The top N populated countries in the world where N is provided by the user.
 */
public class Requirement4 {

    private final Connection con;

    public Requirement4(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves the top N populated countries in the world where N is provided by the user.
     *
     * @param n The number of countries to retrieve. Must be greater than 0.
     * @return List containing up to N Country objects ordered by descending population.
     */
    public ArrayList<Country> getTopNCountriesWorld(int n) {
        ArrayList<Country> countries = new ArrayList<>();

        if (n <= 0) {
            System.err.println("Invalid value for N: " + n + ". N must be greater than zero.");
            return countries;
        }

        if (con == null) {
            System.err.println("Database connection is not established.");
            return countries;
        }

        String sql = "SELECT c.Code, c.Name, c.Continent, c.Region, c.SurfaceArea, c.Population, ct.Name AS Capital " +
                "FROM country c " +
                "LEFT JOIN city ct ON c.Capital = ct.ID " +
                "ORDER BY c.Population DESC " +
                "LIMIT ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, n);

            try (ResultSet rset = pstmt.executeQuery()) {
                while (rset.next()) {
                    Country country = new Country();
                    country.setCode(rset.getString("Code"));
                    country.setName(rset.getString("Name"));
                    country.setContinent(rset.getString("Continent"));
                    country.setRegion(rset.getString("Region"));
                    country.setSurfaceArea(rset.getDouble("SurfaceArea"));
                    country.setPopulation(rset.getInt("Population"));

                    String capitalName = rset.getString("Capital");
                    country.setCapital(capitalName != null ? capitalName : "N/A");

                    countries.add(country);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing REQ-04 query: " + e.getMessage());
        }

        return countries;
    }

    /**
     * Prints a formatted table of the top N countries in the world to standard output.
     *
     * @param countries List of Country records to display.
     * @param n         The requested limit N for the report header.
     */
    public void printCountries(ArrayList<Country> countries, int n) {
        if (countries == null || countries.isEmpty()) {
            System.out.println("No countries found for Top " + n + " countries in the world.");
            return;
        }

        System.out.println("\n=================================================================================================================================");
        System.out.println(" REQ-04: Top " + n + " Populated Countries in the World");
        System.out.println("=================================================================================================================================");
        System.out.printf("%-6s %-40s %-20s %-30s %-15s %-25s%n", "Code", "Name", "Continent", "Region", "Population", "Capital");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

        for (Country country : countries) {
            System.out.printf("%-6s %-40s %-20s %-30s %-15d %-25s%n",
                    country.getCode(),
                    country.getName(),
                    country.getContinent(),
                    country.getRegion(),
                    country.getPopulation(),
                    country.getCapital());
        }
        System.out.println("Total records displayed: " + countries.size() + "\n");
    }
}