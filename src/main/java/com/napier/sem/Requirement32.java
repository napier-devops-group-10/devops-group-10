package com.napier.sem;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * REQ-32: Number of people who speak Chinese, English, Hindi, Spanish, and Arabic
 * from greatest number to smallest, including the percentage of the world population.
 */
public class Requirement32 {

    /**
     * Active database connection reused from App.
     */
    private final Connection con;

    /**
     * Constructor receiving the active database connection.
     *
     * @param con Active SQL Connection.
     */
    public Requirement32(Connection con) {
        this.con = con;
    }

    /**
     * Retrieves and formats the total speakers and world population percentage
     * for Chinese, English, Hindi, Spanish, and Arabic ordered descending.
     *
     * @return ArrayList of formatted report lines for testing and display purposes.
     */
    public ArrayList<String> getLanguagePopulationReport() {
        ArrayList<String> rows = new ArrayList<>();

        // Validate active database connection
        if (con == null) {
            System.err.println("Database connection is not established.");
            return rows;
        }

        // Calculate total speakers per target language and percentage relative to total world population
        String sql = "SELECT cl.Language AS Language, " +
                "ROUND(SUM(c.Population * (cl.Percentage / 100.0))) AS Speakers, " +
                "(SUM(c.Population * (cl.Percentage / 100.0)) / " +
                "    (SELECT SUM(Population) FROM country) * 100.0) AS WorldPercentage " +
                "FROM countrylanguage cl " +
                "JOIN country c ON cl.CountryCode = c.Code " +
                "WHERE cl.Language IN ('Chinese', 'English', 'Hindi', 'Spanish', 'Arabic') " +
                "GROUP BY cl.Language " +
                "ORDER BY Speakers DESC";

        try (Statement stmt = con.createStatement();
             ResultSet rset = stmt.executeQuery(sql)) {

            while (rset.next()) {
                String language = rset.getString("Language");
                long speakers = rset.getLong("Speakers");
                double worldPct = rset.getDouble("WorldPercentage");

                String formattedRow = String.format("%-15s %,20d %18.2f%%",
                        language, speakers, worldPct);
                rows.add(formattedRow);
            }
        } catch (SQLException e) {
            System.err.println("Error executing REQ-32 query: " + e.getMessage());
        }

        return rows;
    }

    /**
     * Prints the formatted language population report to standard output.
     *
     * @param rows List of formatted report rows.
     */
    public void printReport(ArrayList<String> rows) {
        if (rows == null || rows.isEmpty()) {
            System.out.println("No language population data available.");
            return;
        }

        System.out.println("\n=======================================================================");
        System.out.println(" REQ-32: Language Population Report (World Percentage)");
        System.out.println("=======================================================================");
        System.out.printf("%-15s %20s %19s%n", "Language", "Total Speakers", "% World Pop.");
        System.out.println("-----------------------------------------------------------------------");

        for (String row : rows) {
            System.out.println(row);
        }
        System.out.println("Total languages displayed: " + rows.size() + "\n");
    }
}