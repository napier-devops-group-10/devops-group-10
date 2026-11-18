package com.napier.sem;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * REQ-17: All the capital cities in the world organised by largest population to smallest.
 */
public class Requirement17 {

    /**
     * Active database connection reused from App.
     */
    private final Connection con;

    /**
     * Constructor receiving the active database connection.
     *
     * @param con Active SQL Connection.
     */
    public Requirement17(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves all capital cities in the world ordered by descending population.
     *
     * @return ArrayList of City objects representing capital cities, or an empty list if an error occurs.
     */
    public ArrayList<City> getAllCapitalCitiesWorld() {
        ArrayList<City> capitalCities = new ArrayList<>();

        // Validate active database connection
        if (con == null) {
            System.err.println("Database connection is not established.");
            return capitalCities;
        }

        // SQL query joining city and country on Capital ID, ordered by population descending
        String sql = "SELECT ct.ID, ct.Name, ct.CountryCode, ct.District, ct.Population " +
                "FROM city ct " +
                "JOIN country c ON ct.ID = c.Capital " +
                "ORDER BY ct.Population DESC";

        try (Statement stmt = con.createStatement();
             ResultSet rset = stmt.executeQuery(sql)) {

            while (rset.next()) {
                City city = new City();
                city.setId(rset.getInt("ID"));
                city.setName(rset.getString("Name"));
                city.setCountryCode(rset.getString("CountryCode"));
                city.setDistrict(rset.getString("District"));
                city.setPopulation(rset.getInt("Population"));
                capitalCities.add(city);
            }
        } catch (SQLException e) {
            System.err.println("Error executing REQ-17 query: " + e.getMessage());
        }

        return capitalCities;
    }

    /**
     * Prints a formatted table of all capital cities in the world to standard output.
     *
     * @param capitalCities List of capital City records to display.
     */
    public void printCapitalCities(ArrayList<City> capitalCities) {
        if (capitalCities == null || capitalCities.isEmpty()) {
            System.out.println("No capital cities found in the world.");
            return;
        }

        System.out.println("\n=========================================================================================================");
        System.out.println(" REQ-17: All Capital Cities in the World by Population");
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