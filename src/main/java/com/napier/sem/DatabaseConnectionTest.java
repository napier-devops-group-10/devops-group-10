
package com.napier.sem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseConnectionTest {

    public static void main(String[] args) {

        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "3307");
        String database = System.getenv().getOrDefault("DB_NAME", "world");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

        try (Connection connection =
                     DriverManager.getConnection(url, user, password)) {

            System.out.println("Successfully connected to MySQL!");

            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("SELECT DATABASE()")) {

                if (result.next()) {
                    System.out.println("Connected database: " + result.getString(1));
                }
            }

        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            System.exit(1);
        }
    }
}