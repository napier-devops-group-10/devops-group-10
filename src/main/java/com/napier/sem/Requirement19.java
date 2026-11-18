package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * REQ-19: All the capital cities in a region organised by largest population to smallest.
 */
public class Requirement19 {

    /**
     * Active database connection reused from App.
     */
    private final Connection con;

    /**
     * Constructor receiving the active database connection.
     *
     * @param con Active SQL Connection.
     */
    public Requirement19(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves all capital cities in a specific region ordered by descending population.
     *
     * @param region The name of the region (e.g., 'Western Europe', 'Caribbean').
     * @return ArrayList of City objects representing capital cities, or an empty list if an error occurs.
     */
    public ArrayList<City> getCapitalCitiesInRegion(String region) {
        ArrayList<City> capitalCities = new ArrayList<>();

        // Validate region input parameter
        if (region == null || region.trim().isEmpty()) {
            System.err.println("Region parameter cannot be null or empty.");
            return capitalCities;
        }

        // Validate active database connection
        if (con == null) {
            System.err.println("Database connection is not established.");
            return capitalCities;
        }

        // SQL query joining city and country on Capital ID, filtered by region and ordered by population descending
        String sql = "SELECT ct.ID, ct.Name, ct.CountryCode, ct.District, ct.Population " +
                "FROM city ct " +
                "JOIN country c ON ct.ID = c.Capital " +
                "WHERE c.Region = ? " +
                "ORDER BY ct.Population DESC";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, region.trim());

            try (ResultSet rset = pstmt.executeQuery()) {
                while (rset.next()) {
                    City city = new City();
                    city.setId(rset.getInt("ID"));
                    city.setName(rset.getString("Name"));
                    city.setCountryCode(rset.getString("CountryCode"));
                    city.setDistrict(rset.getString("District"));
                    city.setPopulation(rset.getInt("Population"));
                    capitalCities.add(city);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing REQ-19 query: " + e.getMessage());
        }

        return capitalCities;
    }

    /**
     * Prints a formatted table of capital cities in a region to standard output.
     *
     * @param capitalCities List of capital City records to display.
     * @param region        Name of the queried region for the report header.
     */
    public void printCapitalCities(ArrayList<City> capitalCities, String region) {
        if (capitalCities == null || capitalCities.isEmpty()) {
            System.out.println("No capital cities found for region: " + region);
            return;
        }

        System.out.println("\n=========================================================================================================");
        System.out.println(" REQ-19: Capital Cities in Region '" + region + "' by Population");
        System.out.println("=========================================================================================================");
        System.out.printf("%-35s %-15s %-25s %-15s%n", "Name", "Country Code", "District", "Population");
        System.out.println("---------------------------------------------------------------------------------------------------------");

        for (City city : capitalCities) {
            System.out.printf("%-35s %-15s %-25s %-15d%n",
                    city.getName(),
                    city.getCountryCode(),
                    city.getDistrict(),
                    city.getPopulation());
        }
        System.out.println("Total capital cities displayed: " + capitalCities.size() + "\n");
    }
}