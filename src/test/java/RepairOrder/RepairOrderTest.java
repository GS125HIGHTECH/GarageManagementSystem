package RepairOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sobczak.grzegorz.model.RepairOrder;
import pl.sobczak.grzegorz.model.RepairStatus;

import static org.junit.jupiter.api.Assertions.*;

public class RepairOrderTest {
    private RepairOrder repairOrder;

    @BeforeEach
    void setUp() {
        repairOrder = new RepairOrder("vehicle-123", "repair", 123);
    }

    @Test
    void testInitialization() {
        assertNotNull(repairOrder.getId());
        assertEquals("vehicle-123", repairOrder.getVehicleId());
        assertEquals("repair", repairOrder.getDescription());
        assertEquals(123, repairOrder.getCost());
        assertEquals(RepairStatus.OPEN, repairOrder.getStatus());
        assertNotNull(repairOrder.getCreatedAt());
    }

    @Test
    void shouldUpdateStatus() {
        // When
        repairOrder.updateStatus(RepairStatus.IN_PROGRESS);

        // Then
        assertEquals(RepairStatus.IN_PROGRESS, repairOrder.getStatus());
    }

    @Test
    void shouldUpdateDescription() {
        // When
        repairOrder.updateDescription("repair123");

        // Then
        assertEquals("repair123", repairOrder.getDescription());
    }

    @Test
    void shouldUpdateCost() {
        // When
        repairOrder.updateCost(321);

        // Then
        assertEquals(321, repairOrder.getCost());
    }

    @Test
    void shouldMaintainCreationDate() {
        assertNotNull(repairOrder.getCreatedAt());
        System.out.println(repairOrder.getCreatedAt());
        assertTrue(repairOrder.getCreatedAt().isBefore(java.time.LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void shouldThrowExceptionWhenRequiredFieldsAreEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new RepairOrder("", "vehicle-123", "repair", 123));

        assertThrows(IllegalArgumentException.class, () ->
                new RepairOrder("123", null, "repair", 123));

        assertThrows(IllegalArgumentException.class, () ->
                new RepairOrder("123", "", "repair", 123));
    }

    @Test
    void testEquals() {
        RepairOrder sameRepairOrder = new RepairOrder(repairOrder.getId(), "vehicle-999", "same", 123);
        RepairOrder differentRepairOrder = new RepairOrder("vehicle-123", "same", 123);

        assertEquals(repairOrder, sameRepairOrder, "RepairOrders with the same ID should be equal");
        assertEquals(repairOrder, repairOrder, "RepairOrder should be equal to itself");
        assertNotEquals(repairOrder, differentRepairOrder, "RepairOrders with different IDs should not be equal");
        assertNotEquals(repairOrder, null, "RepairOrder should not be equal to null");
        assertNotEquals(repairOrder, "String", "RepairOrder should not be equal to an object of a different class");
    }

    @Test
    void testHashCode() {
        RepairOrder sameRepairOrder = new RepairOrder(repairOrder.getId(), "vehicle-123", "repair", 123);

        assertEquals(repairOrder.hashCode(), sameRepairOrder.hashCode(), "Hash codes must be identical for objects with same ID");
    }

    @Test
    void testToString() {
        String result = repairOrder.toString();

        assertTrue(result.contains(repairOrder.getId()));
        assertTrue(result.contains("vehicle-123"));
        assertTrue(result.contains("repair"));
        assertTrue(result.contains("123.0"));
        assertTrue(result.contains("OPEN"));
    }
}
