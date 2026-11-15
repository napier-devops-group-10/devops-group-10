package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * REQ-10: All the cities in a country organised by largest population to smallest.
 */
public class Requirement10 {

    /**
     * Active database connection reused from App.
     */
    private final Connection con;

    /**
     * Constructor receiving the active database connection.
     *
     * @param con Active SQL Connection.
     */
    public Requirement10(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves all cities in a specific country ordered by descending population.
     *
     * @param country The name of the country (e.g., 'Spain', 'France').
     * @return ArrayList of City objects, or an empty list if input is invalid or an error occurs.
     */
    public ArrayList<City> getCitiesInCountry(String country) {
        ArrayList<City> cities = new ArrayList<>();

        // Validate country input parameter
        if (country == null || country.trim().isEmpty()) {
            System.err.println("Country parameter cannot be null or empty.");
            return cities;
        }

        // Validate active database connection
        if (con == null) {
            System.err.println("Database connection is not established.");
            return cities;
        }

        // SQL query joining city and country to filter by country name and order by population descending
        String sql = "SELECT ct.ID, ct.Name, ct.CountryCode, ct.District, ct.Population " +
                "FROM city ct " +
                "JOIN country c ON ct.CountryCode = c.Code " +
                "WHERE c.Name = ? " +
                "ORDER BY ct.Population DESC";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, country.trim());

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
            System.err.println("Error executing REQ-10 query: " + e.getMessage());
        }

        return cities;
    }

    /**
     * Prints a formatted table of cities in a country to standard output.
     *
     * @param cities  List of City records to display.
     * @param country Name of the queried country for the report header.
     */
    public void printCities(ArrayList<City> cities, String country) {
        if (cities == null || cities.isEmpty()) {
            System.out.println("No cities found for country: " + country);
            return;
        }

        System.out.println("\n=========================================================================================================");
        System.out.println(" REQ-10: Cities in Country '" + country + "' by Population");
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