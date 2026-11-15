package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * REQ-11: All the cities in a district organised by largest population to smallest.
 */
public class Requirement11 {

    /**
     * Active database connection reused from App.
     */
    private final Connection con;

    /**
     * Constructor receiving the active database connection.
     *
     * @param con Active SQL Connection.
     */
    public Requirement11(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves all cities in a specific district ordered by descending population.
     *
     * @param district The name of the district (e.g., 'Gelderland', 'Buenos Aires').
     * @return ArrayList of City objects, or an empty list if none found or an error occurs.
     */
    public ArrayList<City> getCitiesInDistrict(String district) {
        ArrayList<City> cities = new ArrayList<>();

        // Validate input parameter
        if (district == null || district.trim().isEmpty()) {
            System.err.println("District parameter cannot be null or empty.");
            return cities;
        }

        // Validate active database connection
        if (con == null) {
            System.err.println("Database connection is not established.");
            return cities;
        }

        // SQL query filtering by district and ordering by population descending
        String sql = "SELECT ID, Name, CountryCode, District, Population " +
                "FROM city " +
                "WHERE District = ? " +
                "ORDER BY Population DESC";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, district.trim());

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
            System.err.println("Error executing REQ-11 query: " + e.getMessage());
        }

        return cities;
    }

    /**
     * Prints a formatted table of cities in a district to standard output.
     *
     * @param cities   List of City records to display.
     * @param district Name of the queried district for the report header.
     */
    public void printCities(ArrayList<City> cities, String district) {
        if (cities == null || cities.isEmpty()) {
            System.out.println("No cities found for district: " + district);
            return;
        }

        System.out.println("\n=========================================================================================================");
        System.out.println(" REQ-11: Cities in District '" + district + "' by Population");
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