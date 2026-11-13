package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * REQ-15: The top N populated cities in a country where N is provided by the user.
 */
public class Requirement15 {

    /**
     * Active database connection reused from App.
     */
    private final Connection con;

    /**
     * Constructor receiving the active database connection.
     *
     * @param con Active SQL Connection.
     */
    public Requirement15(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves the top N most populated cities in a specific country ordered by descending population.
     *
     * @param country The name of the country (e.g., 'Spain', 'France').
     * @param n       The number of top cities to retrieve. Must be greater than 0.
     * @return ArrayList of City objects, or an empty list if input is invalid or an error occurs.
     */
    public ArrayList<City> getTopNCitiesInCountry(String country, int n) {
        ArrayList<City> cities = new ArrayList<>();

        // Validate country input parameter
        if (country == null || country.trim().isEmpty()) {
            System.err.println("Country parameter cannot be null or empty.");
            return cities;
        }

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

        // SQL query joining city and country to filter by country name and limit results to top N
        String sql = "SELECT ct.ID, ct.Name, ct.CountryCode, ct.District, ct.Population " +
                "FROM city ct " +
                "JOIN country c ON ct.CountryCode = c.Code " +
                "WHERE c.Name = ? " +
                "ORDER BY ct.Population DESC " +
                "LIMIT ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, country.trim());
            pstmt.setInt(2, n);

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
            System.err.println("Error executing REQ-15 query: " + e.getMessage());
        }

        return cities;
    }

    /**
     * Prints a formatted table of the top N cities in a country to standard output.
     *
     * @param cities  List of City records to display.
     * @param country Name of the queried country for the report header.
     * @param n       The requested limit N for the report header.
     */
    public void printCities(ArrayList<City> cities, String country, int n) {
        if (cities == null || cities.isEmpty()) {
            System.out.println("No cities found for Top " + n + " cities in country: " + country);
            return;
        }

        System.out.println("\n=========================================================================================================");
        System.out.println(" REQ-15: Top " + n + " Populated Cities in " + country);
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