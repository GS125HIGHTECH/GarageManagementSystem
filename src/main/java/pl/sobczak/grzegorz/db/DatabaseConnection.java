package pl.sobczak.grzegorz.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:garage.db";

    public static Connection getConnection() throws SQLException {
        Connection conn =  DriverManager.getConnection(URL);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
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

        String sqlParts = "CREATE TABLE IF NOT EXISTS parts (" +
                "id TEXT PRIMARY KEY, " +
                "repairOrderId TEXT NOT NULL, " +
                "partCode TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "price REAL NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "FOREIGN KEY (repairOrderId) REFERENCES repair_orders(id) ON DELETE CASCADE" +
                ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsers);
            stmt.execute(sqlVehicles);
            stmt.execute(sqlRepairOrders);
            stmt.execute(sqlParts);
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize database tables", e);
        }
    }
}
