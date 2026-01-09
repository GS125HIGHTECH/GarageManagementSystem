package RepairOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sobczak.grzegorz.model.Part;
import pl.sobczak.grzegorz.model.RepairOrder;
import pl.sobczak.grzegorz.model.RepairStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RepairOrderTest {
    private RepairOrder repairOrder;

    @BeforeEach
    void setUp() {
        repairOrder = new RepairOrder("123", "vehicle-123", "repair", 123);
    }

    @Test
    void testInitialization() {
        assertEquals("123", repairOrder.getId());
        assertEquals("vehicle-123", repairOrder.getVehicleId());
        assertEquals("repair", repairOrder.getDescription());
        assertEquals(123, repairOrder.getServiceCost());
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
    void shouldUpdateServiceCost() {
        // When
        repairOrder.updateServiceCost(321);

        // Then
        assertEquals(321, repairOrder.getServiceCost());
    }

    @Test
    void shouldAddPartSuccessfully() {
        // Given
        Part part = new Part("p123", "123", "P1", "Oil", "Desc", 50.0, 1);

        // When
        repairOrder.addPart(part);

        // Then
        List<Part> parts = repairOrder.getParts();
        assertEquals(1, parts.size());
        assertEquals("Oil", parts.getFirst().getName());
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
    void shouldThrowExceptionWhenAddingNullPart() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repairOrder.addPart(null),
                "Should throw exception when adding null part");
    }

    @Test
    void shouldThrowExceptionWhenPartBelongsToDifferentOrder() {
        // Given
        Part partForOtherOrder = new Part("p123", "another", "P1", "Oil", "Desc", 50.0, 1);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> repairOrder.addPart(partForOtherOrder));

        assertEquals("Part belongs to a different repair order", exception.getMessage());
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
