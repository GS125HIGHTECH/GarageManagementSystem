package pl.sobczak.grzegorz.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class RepairOrder {
    private final String id;
    private final String vehicleId;
    private String description;
    private double cost;
    private RepairStatus status;
    private final LocalDateTime createdAt;


    public RepairOrder(String id, String vehicleId, String description, double cost) {
        validateRequired(id, "ID");
        validateRequired(vehicleId, "Vehicle ID");

        this.id = id;
        this.vehicleId = vehicleId;
        this.description = description;
        this.cost = cost;
        this.status = RepairStatus.OPEN;
        this.createdAt = LocalDateTime.now();
    }

    public RepairOrder(String vehicleId, String description, double cost) {
        this(UUID.randomUUID().toString(), vehicleId, description, cost);
    }

    public String getId() { return id; }
    public String getVehicleId() { return vehicleId; }
    public String getDescription() { return description; }
    public double getCost() { return cost; }
    public RepairStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }

    public void updateStatus(RepairStatus newStatus) {
        this.status = newStatus;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateCost(double cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "RepairOrder{" +
                "id='" + id + '\'' +
                ", vehicleId='" + vehicleId + '\'' +
                ", description='" + description + '\'' +
                ", cost=" + cost +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepairOrder ro = (RepairOrder) o;
        return Objects.equals(id, ro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
