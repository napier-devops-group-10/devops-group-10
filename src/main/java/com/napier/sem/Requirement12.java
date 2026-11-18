package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * REQ-12: The top N populated cities in the world where N is provided by the user.
 */
public class Requirement12 {

    /**
     * Active database connection reused from App.
     */
    private final Connection con;

    /**
     * Constructor receiving the active database connection.
     *
     * @param con Active SQL Connection.
     */
    public Requirement12(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves the top N most populated cities in the world ordered by descending population.
     *
     * @param n The number of top cities to retrieve. Must be greater than 0.
     * @return ArrayList of City objects, or an empty list if input is invalid or an error occurs.
     */
    public ArrayList<City> getTopNCitiesWorld(int n) {
        ArrayList<City> cities = new ArrayList<>();

        // Validate that N is a positive integer
        if (n <= 0) {
            System.err.println("Invalid value for N: " + n + ". Value must be greater than 0.");
            return cities;
        }

        // Validate active database connection
        if (con == null) {
            System.err.println("Database connection is not established.");
            return cities;
        }

        // SQL query selecting top N cities in the world ordered by population descending
        String sql = "SELECT ID, Name, CountryCode, District, Population " +
                "FROM city " +
                "ORDER BY Population DESC " +
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
                    cities.add(city);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing REQ-12 query: " + e.getMessage());
        }

        return cities;
    }

    /**
     * Prints a formatted table of the top N cities in the world to standard output.
     *
     * @param cities List of City records to display.
     * @param n      The requested limit N for the report header.
     */
    public void printCities(ArrayList<City> cities, int n) {
        if (cities == null || cities.isEmpty()) {
            System.out.println("No cities found for Top " + n + " cities in the world.");
            return;
        }

        System.out.println("\n=========================================================================================================");
        System.out.println(" REQ-12: Top " + n + " Populated Cities in the World");
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