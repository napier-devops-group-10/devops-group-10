package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Requirement 5:
 * The top N populated countries in a continent where N is provided by the user.
 */
public class Requirement5 {

    // Active SQL connection
    private Connection con;

    // Constructor to receive the database connection from App
    public Requirement5(Connection con) {
        this.con = con;
    }

    /**
     * Executes the query and returns the top N countries in the chosen continent.
     *
     * @param continent The continent name to filter by (e.g., "Europe", "Asia").
     * @param n         The limit number of countries.
     * @return List of matching Country objects.
     */
    public ArrayList<Country> getTopNCountriesInContinent(String continent, int n) {
        ArrayList<Country> countries = new ArrayList<>();

        if (con == null || continent == null || continent.trim().isEmpty() || n <= 0) {
            System.out.println("Invalid input parameters or database connection is null.");
            return countries;
        }

        // Join country with city to resolve Capital name from ID
        String query = "SELECT c.Code, c.Name, c.Continent, c.Region, c.SurfaceArea, c.Population, "
                + "       COALESCE(ci.Name, 'N/A') AS Capital "
                + "FROM country c "
                + "LEFT JOIN city ci ON c.Capital = ci.ID "
                + "WHERE c.Continent = ? "
                + "ORDER BY c.Population DESC "
                + "LIMIT ?";

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, continent.trim());
            pstmt.setInt(2, n);

            ResultSet rset = pstmt.executeQuery();

            while (rset.next()) {
                Country cntry = new Country();
                cntry.setCode(rset.getString("Code"));
                cntry.setName(rset.getString("Name"));
                cntry.setContinent(rset.getString("Continent"));
                cntry.setRegion(rset.getString("Region"));
                cntry.setSurfaceArea(rset.getDouble("SurfaceArea"));
                cntry.setPopulation(rset.getInt("Population"));
                cntry.setCapital(rset.getString("Capital"));

                countries.add(cntry);
            }
        } catch (SQLException e) {
            System.out.println("Failed to get top N countries in continent: " + e.getMessage());
        }

        return countries;
    }

    /**
     * Displays the country results in a structured table format.
     *
     * @param countries List of Country records to print.
     * @param continent Target continent name for header.
     * @param n         Requested count limit.
     */
    public void printCountries(ArrayList<Country> countries, String continent, int n) {
        if (countries == null || countries.isEmpty()) {
            System.out.println("No countries to display for continent: " + continent);
            return;
        }

        System.out.println();
        System.out.println("=================================================================================================================================");
        System.out.println(" REPORT: REQ-05 - Top " + n + " Populated Countries in Continent: " + continent);
        System.out.println("=================================================================================================================================");
        System.out.printf("%-6s %-40s %-20s %-30s %-15s %-25s%n",
                "Code", "Name", "Continent", "Region", "Population", "Capital");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

        for (Country c : countries) {
            if (c != null) {
                System.out.printf("%-6s %-40s %-20s %-30s %-15d %-25s%n",
                        c.getCode(),
                        c.getName(),
                        c.getContinent(),
                        c.getRegion(),
                        c.getPopulation(),
                        c.getCapital());
            }
        }
        System.out.println("Total records displayed: " + countries.size() + "\n");
    }

    /**
     * Backward-compatible print method.
     */
    public void printCountries(ArrayList<Country> countries) {
        printCountries(countries, "Specified Continent", countries != null ? countries.size() : 0);
    }
}