package com.napier.sem;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * REQ-07: All the cities in the world organised by largest population to smallest.
 */
public class Requirement7 {

    /**
     * Active database connection reused from App.
     */
    private final Connection con;

    /**
     * Constructor receiving the active database connection.
     *
     * @param con Active SQL Connection.
     */
    public Requirement7(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves all cities in the world ordered by descending population.
     *
     * @return ArrayList of City objects ordered by descending population, or an empty list if an error occurs.
     */
    public ArrayList<City> getAllCitiesWorld() {
        ArrayList<City> cities = new ArrayList<>();

        // Validate active database connection
        if (con == null) {
            System.err.println("Database connection is not established.");
            return cities;
        }

        // SQL query retrieving all cities ordered by population descending
        String sql = "SELECT ID, Name, CountryCode, District, Population " +
                "FROM city " +
                "ORDER BY Population DESC";

        try (Statement stmt = con.createStatement();
             ResultSet rset = stmt.executeQuery(sql)) {

            while (rset.next()) {
                City city = new City();
                city.setId(rset.getInt("ID"));
                city.setName(rset.getString("Name"));
                city.setCountryCode(rset.getString("CountryCode"));
                city.setDistrict(rset.getString("District"));
                city.setPopulation(rset.getInt("Population"));
                cities.add(city);
            }
        } catch (SQLException e) {
            System.err.println("Error executing REQ-07 query: " + e.getMessage());
        }

        return cities;
    }

    /**
     * Prints a formatted table of all cities in the world to standard output.
     *
     * @param cities List of City records to display.
     */
    public void printCities(ArrayList<City> cities) {
        if (cities == null || cities.isEmpty()) {
            System.out.println("No cities found in the world.");
            return;
        }

        System.out.println("\n=========================================================================================================");
        System.out.println(" REQ-07: All Cities in the World by Population");
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