package pl.sobczak.grzegorz.dao;

import pl.sobczak.grzegorz.model.Part;
import pl.sobczak.grzegorz.model.RepairOrder;
import pl.sobczak.grzegorz.model.RepairStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record RepairOrderDao(Connection connection) {
    private static final String INSERT_PART_SQL = "INSERT INTO parts(id, repairOrderId, partCode, name, description, price, quantity) VALUES (?, ?, ?, ?, ?, ?, ?)";

    public void save(RepairOrder order) {
        String orderSql = "INSERT INTO repair_orders(id, vehicleId, description, cost, status, createdAt) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);
            try (PreparedStatement pstmt = connection.prepareStatement(orderSql)) {
                mapOrderToStatement(pstmt, order);
                pstmt.executeUpdate();
            }

            try (PreparedStatement partPstmt = connection.prepareStatement(INSERT_PART_SQL)) {
                saveParts(partPstmt, order.getParts());
            }

            connection.commit();
        } catch (SQLException e) {
            rollback();
            throw new RuntimeException("Error saving repair order with parts", e);
        } finally {
            resetAutoCommit();
        }
    }

    public void update(RepairOrder order) {
        String updateOrderSql = "UPDATE repair_orders SET description = ?, cost = ?, status = ? WHERE id = ?";
        String deletePartsSql = "DELETE FROM parts WHERE repairOrderId = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(updateOrderSql)) {
                pstmt.setString(1, order.getDescription());
                pstmt.setDouble(2, order.getServiceCost());
                pstmt.setString(3, order.getStatus().name());
                pstmt.setString(4, order.getId());
                pstmt.executeUpdate();
            }

            try (PreparedStatement deletePstmt = connection.prepareStatement(deletePartsSql)) {
                deletePstmt.setString(1, order.getId());
                deletePstmt.executeUpdate();
            }

            try (PreparedStatement partPstmt = connection.prepareStatement(INSERT_PART_SQL)) {
                saveParts(partPstmt, order.getParts());
            }

            connection.commit();
        } catch (SQLException e) {
            rollback();
            throw new RuntimeException("Error updating repair order and its parts", e);
        } finally {
            resetAutoCommit();
        }
    }

    public Optional<RepairOrder> findById(String id) {
        String sql = "SELECT * FROM repair_orders WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    RepairOrder order = mapRowToOrder(rs);
                    loadPartsForOrder(order);
                    return Optional.of(order);
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
                    RepairOrder order = mapRowToOrder(rs);
                    loadPartsForOrder(order);
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching repair orders for vehicle", e);
        }
        return orders;
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

        order.updateStatus(RepairStatus.valueOf(rs.getString("status")));

        return order;
    }

    private void loadPartsForOrder(RepairOrder order) throws SQLException {
        String sql = "SELECT * FROM parts WHERE repairOrderId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, order.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Part part = new Part(
                            rs.getString("id"),
                            rs.getString("repairOrderId"),
                            rs.getString("partCode"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("quantity")
                    );
                    order.addPart(part);
                }
            }
        }
    }

    private void saveParts(PreparedStatement partPstmt, List<Part> parts) throws SQLException {
        for (Part part : parts) {
            partPstmt.setString(1, part.getId());
            partPstmt.setString(2, part.getRepairOrderId());
            partPstmt.setString(3, part.getPartCode());
            partPstmt.setString(4, part.getName());
            partPstmt.setString(5, part.getDescription());
            partPstmt.setDouble(6, part.getPrice());
            partPstmt.setInt(7, part.getQuantity());
            partPstmt.executeUpdate();
        }
    }

    private void mapOrderToStatement(PreparedStatement pstmt, RepairOrder order) throws SQLException {
        pstmt.setString(1, order.getId());
        pstmt.setString(2, order.getVehicleId());
        pstmt.setString(3, order.getDescription());
        pstmt.setDouble(4, order.getServiceCost());
        pstmt.setString(5, order.getStatus().name());
        pstmt.setString(6, order.getCreatedAt().toString());
    }

    private void rollback() {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            System.err.println("Failed to rollback transaction " + ex.getMessage());
        }
    }

    private void resetAutoCommit() {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException ex) {
            System.err.println("Failed to reset auto-commit to true " + ex.getMessage());
        }
    }
}
