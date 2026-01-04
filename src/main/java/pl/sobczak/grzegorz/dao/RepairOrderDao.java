package pl.sobczak.grzegorz.dao;

import pl.sobczak.grzegorz.model.RepairOrder;
import pl.sobczak.grzegorz.model.RepairStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepairOrderDao {
    private final Connection connection;

    public RepairOrderDao(Connection connection) {
        this.connection = connection;
    }

    public void save(RepairOrder order) {
        String sql = "INSERT INTO repair_orders(id, vehicleId, description, cost, status, createdAt) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, order.getId());
            pstmt.setString(2, order.getVehicleId());
            pstmt.setString(3, order.getDescription());
            pstmt.setDouble(4, order.getCost());
            pstmt.setString(5, order.getStatus().name());
            pstmt.setString(6, order.getCreatedAt().toString()); // ISO-8601
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving repair order", e);
        }
    }

    public Optional<RepairOrder> findById(String id) {
        String sql = "SELECT * FROM repair_orders WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToOrder(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding repair order by ID", e);
        }
        return Optional.empty();
    }

    public List<RepairOrder> findByVehicleId(String vehicleId) {
        List<RepairOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM repair_orders WHERE vehicleId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, vehicleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRowToOrder(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching repair orders for vehicle", e);
        }
        return orders;
    }

    public void update(RepairOrder order) {
        String sql = "UPDATE repair_orders SET description = ?, cost = ?, status = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, order.getDescription());
            pstmt.setDouble(2, order.getCost());
            pstmt.setString(3, order.getStatus().name());
            pstmt.setString(4, order.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating repair order", e);
        }
    }

    public void delete(String id) {
        String sql = "DELETE FROM repair_orders WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting repair order", e);
        }
    }

    private RepairOrder mapRowToOrder(ResultSet rs) throws SQLException {
        RepairOrder order = new RepairOrder(
                rs.getString("id"),
                rs.getString("vehicleId"),
                rs.getString("description"),
                rs.getDouble("cost")
        );

        String statusFromDb = rs.getString("status");
        order.updateStatus(RepairStatus.valueOf(statusFromDb));

        return order;
    }
}
