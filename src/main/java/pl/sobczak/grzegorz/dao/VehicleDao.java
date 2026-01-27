package pl.sobczak.grzegorz.dao;

import pl.sobczak.grzegorz.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record VehicleDao(Connection connection) {

    public void save(Vehicle vehicle) {
        String sql = "INSERT INTO vehicles(id, ownerId, brand, model, vin, color) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, vehicle.getId());
            pstmt.setString(2, vehicle.getOwnerId());
            pstmt.setString(3, vehicle.getBrand());
            pstmt.setString(4, vehicle.getModel());
            pstmt.setString(5, vehicle.getVin());
            pstmt.setString(6, vehicle.getColor());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving vehicle", e);
        }
    }

    public Optional<Vehicle> findById(String id) {
        String sql = "SELECT * FROM vehicles WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToVehicle(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding vehicle by ID", e);
        }
        return Optional.empty();
    }

    public Optional<Vehicle> findByVin(String vin) {
        String sql = "SELECT * FROM vehicles WHERE vin = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, vin);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToVehicle(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding vehicle by VIN", e);
        }
        return Optional.empty();
    }

    public List<Vehicle> findByOwnerId(String ownerId) {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM vehicles WHERE ownerId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ownerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    vehicles.add(mapRowToVehicle(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching vehicles for owner: " + ownerId, e);
        }
        return vehicles;
    }

    public void update(Vehicle vehicle) {
        String sql = "UPDATE vehicles SET color = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, vehicle.getColor());
            pstmt.setString(2, vehicle.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating vehicle color", e);
        }
    }

    public void delete(String id) {
        String sql = "DELETE FROM vehicles WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting vehicle", e);
        }
    }

    private Vehicle mapRowToVehicle(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getString("id"),
                rs.getString("ownerId"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getString("vin"),
                rs.getString("color")
        );
    }
}
