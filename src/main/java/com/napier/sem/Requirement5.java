package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
     */
    public ArrayList<Country> getTopNCountriesInContinent(String continent, int n) {
        ArrayList<Country> countries = new ArrayList<>();

        try {
            // SQL query with two placeholders: continent and limit (N)
            String query = "SELECT Code, Name, Continent, Region, Population, Capital "
                    + "FROM country "
                    + "WHERE Continent = ? "
                    + "ORDER BY Population DESC "
                    + "LIMIT ?";

            // Prepare statement and set parameters
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setString(1, continent);
            pstmt.setInt(2, n);

            // Execute query
            ResultSet rset = pstmt.executeQuery();

            // Unpack rows into Country objects using setters
            while (rset.next()) {
                Country cntry = new Country();
                cntry.setCode(rset.getString("Code"));
                cntry.setName(rset.getString("Name"));
                cntry.setContinent(rset.getString("Continent"));
                cntry.setRegion(rset.getString("Region"));
                cntry.setPopulation(rset.getInt("Population"));
                cntry.setCapital(rset.getString("Capital"));

                countries.add(cntry);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get top N countries in continent");
        }

        return countries;
    }

    /**
     * Displays the country results in the terminal.
     */
    public void printCountries(ArrayList<Country> countries) {
        if (countries == null || countries.isEmpty()) {
            System.out.println("No countries to display");
            return;
        }

        System.out.println("Code\tName\tContinent\tRegion\tPopulation");
        for (Country c : countries) {
            System.out.println(c.getCode() + "\t"
                    + c.getName() + "\t"
                    + c.getContinent() + "\t"
                    + c.getRegion() + "\t"
                    + c.getPopulation());
        }
    }
}