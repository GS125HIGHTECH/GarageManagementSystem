package pl.sobczak.grzegorz.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:garage.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id TEXT PRIMARY KEY, " +
                "firstName TEXT NOT NULL, " +
                "lastName TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "isActive INTEGER NOT NULL" +
                ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
