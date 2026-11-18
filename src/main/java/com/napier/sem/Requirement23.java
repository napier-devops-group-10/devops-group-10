package com.napier.sem;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * REQ-23: The population of people, people living in cities, and people not living in cities in each continent.
 */
public class Requirement23 {

    /**
     * Active database connection reused from App.
     */
    private final Connection con;

    /**
     * Constructor receiving the active database connection.
     *
     * @param con Active SQL Connection.
     */
    public Requirement23(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves and prints the total population, urban population, and rural population
     * (with percentages) for each continent.
     *
     * @return ArrayList of formatted report lines for testing and display purposes.
     */
    public ArrayList<String> getContinentPopulationBreakdown() {
        ArrayList<String> rows = new ArrayList<>();

        // Validate active database connection
        if (con == null) {
            System.err.println("Database connection is not established.");
            return rows;
        }

        // Subquery aggregates city populations per country first to prevent row multiplication when summing country populations
        String sql = "SELECT c.Continent AS Name, " +
                "SUM(c.Population) AS TotalPopulation, " +
                "COALESCE(SUM(city_totals.CityPopulation), 0) AS CityPopulation " +
                "FROM country c " +
                "LEFT JOIN (" +
                "    SELECT CountryCode, SUM(Population) AS CityPopulation " +
                "    FROM city " +
                "    GROUP BY CountryCode" +
                ") city_totals ON c.Code = city_totals.CountryCode " +
                "GROUP BY c.Continent " +
                "ORDER BY TotalPopulation DESC";

        try (Statement stmt = con.createStatement();
             ResultSet rset = stmt.executeQuery(sql)) {

            while (rset.next()) {
                String continent = rset.getString("Name");
                long totalPop = rset.getLong("TotalPopulation");
                long cityPop = rset.getLong("CityPopulation");
                long nonCityPop = Math.max(0, totalPop - cityPop);

                double cityPct = (totalPop > 0) ? (cityPop * 100.0 / totalPop) : 0.0;
                double nonCityPct = (totalPop > 0) ? (nonCityPop * 100.0 / totalPop) : 0.0;

                String formattedRow = String.format("%-20s %,18d %,18d (%.2f%%) %,18d (%.2f%%)",
                        continent, totalPop, cityPop, cityPct, nonCityPop, nonCityPct);
                rows.add(formattedRow);
            }
        } catch (SQLException e) {
            System.err.println("Error executing REQ-23 query: " + e.getMessage());
        }

        return rows;
    }

    /**
     * Prints the formatted continent urban/rural population report to standard output.
     *
     * @param rows List of formatted report rows.
     */
    public void printReport(ArrayList<String> rows) {
        if (rows == null || rows.isEmpty()) {
            System.out.println("No continent population breakdown data available.");
            return;
        }

        System.out.println("\n=========================================================================================================");
        System.out.println(" REQ-23: Population Living and Not Living in Cities by Continent");
        System.out.println("=========================================================================================================");
        System.out.printf("%-20s %18s %27s %27s%n", "Continent", "Total Population", "In Cities (%)", "Not in Cities (%)");
        System.out.println("---------------------------------------------------------------------------------------------------------");

        for (String row : rows) {
            System.out.println(row);
        }
        System.out.println("Total continents displayed: " + rows.size() + "\n");
    }
}