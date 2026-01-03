package pl.sobczak.grzegorz.model;

import java.util.Objects;
import java.util.UUID;

public class Vehicle {
    private final String id;
    private final String ownerId;
    private final String brand;
    private final String model;
    private final String vin;
    private String color;

    public Vehicle(String id, String ownerId, String brand, String model, String vin, String color) {
        validateRequired(id, "ID");
        validateRequired(ownerId, "Owner ID");
        validateRequired(brand, "Brand");
        validateRequired(model, "Model");
        validateRequired(color, "Color");

        if (vin == null || vin.length() != 17) {
            throw new IllegalArgumentException("VIN must be exactly 17 characters");
        }
        this.id = id;
        this.ownerId = ownerId;
        this.brand = brand;
        this.model = model;
        this.vin = vin;
        this.color = color;
    }

    public Vehicle(String ownerId, String brand, String model, String vin,  String color) {
        this(UUID.randomUUID().toString(), ownerId, brand, model, vin,  color);
    }

    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getVin() { return vin; }
    public String getColor() { return color; }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }

    public void changeColor(String newColor) {
        validateRequired(newColor, "Color");
        this.color = newColor;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id='" + id + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", vin='" + vin + '\'' +
                ", color='" + color + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(id, vehicle.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
