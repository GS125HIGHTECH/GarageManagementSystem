package pl.sobczak.grzegorz.model;

import java.util.Objects;
import java.util.UUID;

public class Part {
    private final String id;
    private final String repairOrderId;
    private final String partCode;
    private final String name;
    private final String description;
    private final double price;
    private int quantity;

    public Part(String id, String repairOrderId, String partCode, String name, String description, double price, int quantity) {
        validateRequired(id, "ID");
        validateRequired(repairOrderId, "Repair Order ID");
        validateRequired(partCode, "Part Code");

        this.id = id;
        this.repairOrderId = repairOrderId;
        this.partCode = partCode;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public Part(String repairOrderId, String partCode, String name, double price, int quantity) {
        this(UUID.randomUUID().toString(), repairOrderId, partCode, name, "", price, quantity);
    }

    public String getId() { return id; }
    public String getRepairOrderId() { return repairOrderId; }
    public String getPartCode() { return partCode; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return price * quantity;
    }

    public boolean isSameProduct(Part other) {
        if (other == null) return false;
        return Objects.equals(this.partCode, other.partCode);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (x%d) - Jednostkowa: %.2f, Suma: %.2f PLN",
                partCode, name, quantity, price, getTotalPrice());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Part part = (Part) o;
        return Objects.equals(id, part.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
