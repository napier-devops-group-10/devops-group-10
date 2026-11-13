package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * REQ-02: All the countries in a continent organised by largest population to smallest.
 */
public class Requirement2 {

    private final Connection con;

    public Requirement2(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves all countries in a specific continent organised by largest population to smallest.
     *
     * @param continent The name of the continent (e.g., 'Europe', 'Asia').
     * @return List of Country objects in the continent ordered by descending population.
     */
    public ArrayList<Country> getCountriesByContinent(String continent) {
        ArrayList<Country> countries = new ArrayList<>();

        if (continent == null || continent.trim().isEmpty()) {
            System.err.println("Continent parameter cannot be null or empty.");
            return countries;
        }

        if (con == null) {
            System.err.println("Database connection is not established.");
            return countries;
        }

        String sql = "SELECT c.Code, c.Name, c.Continent, c.Region, c.SurfaceArea, c.Population, ct.Name AS Capital " +
                "FROM country c " +
                "LEFT JOIN city ct ON c.Capital = ct.ID " +
                "WHERE c.Continent = ? " +
                "ORDER BY c.Population DESC";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, continent.trim());

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
            System.err.println("Error executing REQ-02 query: " + e.getMessage());
        }

        return countries;
    }

    /**
     * Prints a formatted table of countries in a continent to standard output.
     *
     * @param countries List of Country records to display.
     * @param continent Name of the queried continent for the report header.
     */
    public void printCountries(ArrayList<Country> countries, String continent) {
        if (countries == null || countries.isEmpty()) {
            System.out.println("No countries found for continent: " + continent);
            return;
        }

        System.out.println("\n=================================================================================================================================");
        System.out.println(" REQ-02: Countries in Continent '" + continent + "' by Population");
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