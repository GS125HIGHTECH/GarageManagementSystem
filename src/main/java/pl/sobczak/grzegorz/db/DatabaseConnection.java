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
        String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "id TEXT PRIMARY KEY, " +
                "firstName TEXT NOT NULL, " +
                "lastName TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "isActive INTEGER NOT NULL" +
                ")";

        String sqlVehicles = "CREATE TABLE IF NOT EXISTS vehicles (" +
                "id TEXT PRIMARY KEY, " +
                "ownerId TEXT NOT NULL, " +
                "brand TEXT NOT NULL, " +
                "model TEXT NOT NULL, " +
                "vin TEXT UNIQUE NOT NULL, " +
                "color TEXT, " +
                "FOREIGN KEY (ownerId) REFERENCES users(id)" +
                ")";

        String sqlRepairOrders = "CREATE TABLE IF NOT EXISTS repair_orders (" +
                "id TEXT PRIMARY KEY, " +
                "vehicleId TEXT NOT NULL, " +
                "description TEXT, " +
                "cost REAL, " +
                "status TEXT NOT NULL, " +
                "createdAt TEXT NOT NULL, " +
                "FOREIGN KEY (vehicleId) REFERENCES vehicles(id)" +
                ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsers);
            stmt.execute(sqlVehicles);
            stmt.execute(sqlRepairOrders);
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize database tables", e);
        }
    }
}
