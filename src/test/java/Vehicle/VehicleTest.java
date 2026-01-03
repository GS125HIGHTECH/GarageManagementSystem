package Vehicle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sobczak.grzegorz.model.Vehicle;

import static org.junit.jupiter.api.Assertions.*;

public class VehicleTest {
    private Vehicle vehicle;

    private final String validVin = "12345678901234567";

    @BeforeEach
    void setUp() {
        vehicle = new Vehicle("owner-123", "Toyota", "Corolla", validVin, "Red");
    }

    @Test
    void testInitialization() {
        assertNotNull(vehicle.getId());
        assertEquals("owner-123", vehicle.getOwnerId());
        assertEquals("Toyota", vehicle.getBrand());
        assertEquals("Corolla", vehicle.getModel());
        assertEquals(validVin, vehicle.getVin());
        assertEquals("Red", vehicle.getColor());
    }

    @Test
    void shouldThrowExceptionForInvalidVin() {
        assertThrows(IllegalArgumentException.class, () ->
                new Vehicle("owner-1", "Audi", "A3", "123", "Blue"));

        assertThrows(IllegalArgumentException.class, () ->
                new Vehicle("owner-1", "Audi", "A3", null, "Blue"));
    }

    @Test
    void shouldThrowExceptionWhenRequiredFieldsAreEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Vehicle("owner-1", "", "Model", validVin, "Black"));

        assertThrows(IllegalArgumentException.class, () ->
                new Vehicle("owner-1", "Brand", null, validVin, "Black"));

        assertThrows(IllegalArgumentException.class, () ->
                new Vehicle(" ", "Brand", "Model", validVin, "Black"));
    }

    @Test
    void testChangeColor() {
        vehicle.changeColor("Blue");
        assertEquals("Blue", vehicle.getColor());

        assertThrows(IllegalArgumentException.class, () -> vehicle.changeColor(""));
        assertThrows(IllegalArgumentException.class, () -> vehicle.changeColor(null));
    }

    @Test
    void testEquals() {
        Vehicle sameVehicle = new Vehicle(vehicle.getId(), "owner-999", "Other", "Other", "11122233344455566", "Blue");
        Vehicle differentVehicle = new Vehicle("owner-123", "Toyota", "Corolla", validVin, "Red");

        assertEquals(vehicle, sameVehicle, "Vehicles with the same ID should be equal");
        assertEquals(vehicle, vehicle, "Vehicle should be equal to itself");
        assertNotEquals(vehicle, differentVehicle, "Vehicles with different IDs should not be equal");
        assertNotEquals(vehicle, null, "Vehicle should not be equal to null");
        assertNotEquals(vehicle, "String", "Vehicle should not be equal to an object of a different class");
    }

    @Test
    void testHashCode() {
        Vehicle sameVehicle = new Vehicle(vehicle.getId(), "owner-123", "Toyota", "Corolla", validVin, "Red");

        assertEquals(vehicle.hashCode(), sameVehicle.hashCode(), "Hash codes must be identical for objects with same ID");
    }

    @Test
    void testToString() {
        String result = vehicle.toString();

        assertTrue(result.contains(vehicle.getId()));
        assertTrue(result.contains("Toyota"));
        assertTrue(result.contains("Corolla"));
        assertTrue(result.contains(validVin));
        assertTrue(result.contains("Red"));
    }
}
