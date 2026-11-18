package com.napier.sem;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * REQ-01: All the countries in the world organised by largest population to smallest.
 */
public class Requirement1 {

    private final Connection con;

    public Requirement1(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves all countries in the world organised by largest population to smallest.
     *
     * @return List of all Country objects ordered by descending population.
     */
    public ArrayList<Country> getAllCountriesWorld() {
        ArrayList<Country> countries = new ArrayList<>();

        if (con == null) {
            System.err.println("Database connection is not established.");
            return countries;
        }

        String sql = "SELECT c.Code, c.Name, c.Continent, c.Region, c.SurfaceArea, c.Population, ct.Name AS Capital " +
                "FROM country c " +
                "LEFT JOIN city ct ON c.Capital = ct.ID " +
                "ORDER BY c.Population DESC";

        try (Statement stmt = con.createStatement();
             ResultSet rset = stmt.executeQuery(sql)) {

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
        } catch (SQLException e) {
            System.err.println("Error executing REQ-01 query: " + e.getMessage());
        }

        return countries;
    }

    /**
     * Prints a formatted table of all countries in the world to standard output.
     *
     * @param countries List of Country records to display.
     */
    public void printCountries(ArrayList<Country> countries) {
        if (countries == null || countries.isEmpty()) {
            System.out.println("No countries found in the world.");
            return;
        }

        System.out.println("\n=================================================================================================================================");
        System.out.println(" REQ-01: All Countries in the World by Population");
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