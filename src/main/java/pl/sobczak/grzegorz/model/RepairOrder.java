package pl.sobczak.grzegorz.model;

import java.time.LocalDateTime;
import java.util.*;

public class RepairOrder {
    private final String id;
    private final String vehicleId;
    private String description;
    private double serviceCost;
    private RepairStatus status;
    private final LocalDateTime createdAt;
    private final List<Part> parts = new ArrayList<>();


    public RepairOrder(String id, String vehicleId, String description, double serviceCost) {
        validateRequired(id, "ID");
        validateRequired(vehicleId, "Vehicle ID");

        this.id = id;
        this.vehicleId = vehicleId;
        this.description = description;
        this.serviceCost = serviceCost;
        this.status = RepairStatus.OPEN;
        this.createdAt = LocalDateTime.now();
    }

    public RepairOrder(String vehicleId, String description, double serviceCost) {
        this(UUID.randomUUID().toString(), vehicleId, description, serviceCost);
    }

    public String getId() { return id; }
    public String getVehicleId() { return vehicleId; }
    public String getDescription() { return description; }
    public double getServiceCost() { return serviceCost; }
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

    public void updateServiceCost(double serviceCost) {
        this.serviceCost = serviceCost;
    }

    public void addPart(Part part) {
        if (part == null) throw new IllegalArgumentException("Part cannot be null");
        if (!part.getRepairOrderId().equals(this.id)) {
            throw new IllegalArgumentException("Part belongs to a different repair order");
        }
        this.parts.add(part);
    }

    public List<Part> getParts() {
        return Collections.unmodifiableList(parts);
    }

    public double getTotalCost() {
        double partsTotal = parts.stream()
                .mapToDouble(Part::getTotalPrice)
                .sum();
        return serviceCost + partsTotal;
    }

    @Override
    public String toString() {
        return "RepairOrder{" +
                "id='" + id + '\'' +
                ", vehicleId='" + vehicleId + '\'' +
                ", description='" + description + '\'' +
                ", serviceCost=" + serviceCost +
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
