package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * REQ-20: The top N populated capital cities in the world where N is provided by the user.
 */
public class Requirement20 {

    /**
     * Active database connection reused from App.
     */
    private final Connection con;

    /**
     * Constructor receiving the active database connection.
     *
     * @param con Active SQL Connection.
     */
    public Requirement20(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves the top N most populated capital cities in the world ordered by descending population.
     *
     * @param n The number of top capital cities to retrieve. Must be greater than 0.
     * @return ArrayList of City objects representing capital cities, or an empty list if input is invalid or an error occurs.
     */
    public ArrayList<City> getTopNCapitalCitiesWorld(int n) {
        ArrayList<City> capitalCities = new ArrayList<>();

        // Validate that N is a positive integer
        if (n <= 0) {
            System.err.println("Invalid value for N: " + n + ". Value must be greater than 0.");
            return capitalCities;
        }

        // Validate active database connection
        if (con == null) {
            System.err.println("Database connection is not established.");
            return capitalCities;
        }

        // SQL query joining city and country on Capital ID, limiting to top N ordered by population descending
        String sql = "SELECT ct.ID, ct.Name, ct.CountryCode, ct.District, ct.Population " +
                "FROM city ct " +
                "JOIN country c ON ct.ID = c.Capital " +
                "ORDER BY ct.Population DESC " +
                "LIMIT ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, n);

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
            System.err.println("Error executing REQ-20 query: " + e.getMessage());
        }

        return capitalCities;
    }

    /**
     * Prints a formatted table of the top N capital cities in the world to standard output.
     *
     * @param capitalCities List of capital City records to display.
     * @param n             The requested limit N for the report header.
     */
    public void printCapitalCities(ArrayList<City> capitalCities, int n) {
        if (capitalCities == null || capitalCities.isEmpty()) {
            System.out.println("No capital cities found for Top " + n + " capital cities in the world.");
            return;
        }

        System.out.println("\n=========================================================================================================");
        System.out.println(" REQ-20: Top " + n + " Populated Capital Cities in the World");
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