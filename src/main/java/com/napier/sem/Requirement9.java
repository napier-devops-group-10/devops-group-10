package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * REQ-09: All the cities in a region organised by largest population to smallest.
 */
public class Requirement9 {

    /**
     * Active database connection reused from App.
     */
    private final Connection con;

    /**
     * Constructor receiving the active database connection.
     *
     * @param con Active SQL Connection.
     */
    public Requirement9(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves all cities in a specific region ordered by descending population.
     *
     * @param region The name of the region (e.g., 'Western Europe', 'Caribbean').
     * @return ArrayList of City objects, or an empty list if input is invalid or an error occurs.
     */
    public ArrayList<City> getCitiesInRegion(String region) {
        ArrayList<City> cities = new ArrayList<>();

        // Validate region input parameter
        if (region == null || region.trim().isEmpty()) {
            System.err.println("Region parameter cannot be null or empty.");
            return cities;
        }

        // Validate active database connection
        if (con == null) {
            System.err.println("Database connection is not established.");
            return cities;
        }

        // SQL query joining city and country to filter by region and order by population descending
        String sql = "SELECT ct.ID, ct.Name, ct.CountryCode, ct.District, ct.Population " +
                "FROM city ct " +
                "JOIN country c ON ct.CountryCode = c.Code " +
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
                    cities.add(city);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing REQ-09 query: " + e.getMessage());
        }

        return cities;
    }

    /**
     * Prints a formatted table of cities in a region to standard output.
     *
     * @param cities List of City records to display.
     * @param region Name of the queried region for the report header.
     */
    public void printCities(ArrayList<City> cities, String region) {
        if (cities == null || cities.isEmpty()) {
            System.out.println("No cities found for region: " + region);
            return;
        }

        System.out.println("\n=========================================================================================================");
        System.out.println(" REQ-09: Cities in Region '" + region + "' by Population");
        System.out.println("=========================================================================================================");
        System.out.printf("%-35s %-15s %-25s %-15s%n", "Name", "Country Code", "District", "Population");
        System.out.println("---------------------------------------------------------------------------------------------------------");

        for (City city : cities) {
            System.out.printf("%-35s %-15s %-25s %-15d%n",
                    city.getName(),
                    city.getCountryCode(),
                    city.getDistrict(),
                    city.getPopulation());
        }
        System.out.println("Total cities displayed: " + cities.size() + "\n");
    }
}