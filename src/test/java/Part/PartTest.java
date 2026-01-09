package Part;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sobczak.grzegorz.model.Part;

import static org.junit.jupiter.api.Assertions.*;

public class PartTest {
    private Part part;
    private final String orderId = "order-123";

    @BeforeEach
    void setUp() {
        part = new Part("p123", orderId, "P1","Part1", "Desc", 50.0, 1);
    }

    @Test
    void testInitialization() {
        assertEquals("p123", part.getId());
        assertEquals(orderId, part.getRepairOrderId());
        assertEquals("P1", part.getPartCode());
        assertEquals("Part1", part.getName());
        assertEquals(50.0, part.getPrice());
        assertEquals(1, part.getQuantity());
    }

    @Test
    void shouldCalculateTotalPrice() {
        Part part = new Part(orderId, "P2", "Part2", 45.50, 4);

        double total = part.getTotalPrice();

        assertEquals(182.00, total, 0.001);
    }

    @Test
    void testChangeQuantity() {
        part.setQuantity(4);
        assertEquals(4, part.getQuantity());

        assertThrows(IllegalArgumentException.class, () -> part.setQuantity(-4));
    }

    @Test
    void shouldIdentifySameProductByCode() {
        Part part1 = new Part("p123", orderId, "P1","Part1", "Desc", 50.0, 3);
        Part part2 = new Part("p123", orderId, "P2","Part2", "Desc", 50.0, 4);

        assertTrue(part.isSameProduct(part1), "Same code should mean same product");
        assertFalse(part1.isSameProduct(part2), "Different code should be different product");
        assertFalse(part1.isSameProduct(null), "Should handle null gracefully");
    }

    @Test
    void shouldThrowExceptionWhenRequiredFieldsAreEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Part(null, "ord-1", "CODE", "Name", "Desc", 10.0, 1), "ID cannot be null");

        assertThrows(IllegalArgumentException.class, () ->
                new Part("id-1", "  ", "CODE", "Name", "Desc", 10.0, 1), "Repair Order ID cannot be empty");

        assertThrows(IllegalArgumentException.class, () ->
                new Part("id-1", "ord-1", "", "Name", "Desc", 10.0, 1), "Part code cannot be empty");
    }

    @Test
    void testToStringFormatting() {
        Part part = new Part("ord-1", "DISK-V", "Tarcze hamulcowe", 150.00, 2);

        String output = part.toString();

        assertTrue(output.contains("[DISK-V]"));
        assertTrue(output.contains("Tarcze hamulcowe"));
        assertTrue(output.contains("x2"));
        assertTrue(output.contains("300,00"));
    }

    @Test
    void testEquals() {
        Part samePart = new Part("p123", "123", "P1", "Oil", "Desc", 50.0, 1);
        Part differentPart = new Part("different", "123", "P1", "Oil", "Desc", 50.0, 1);

        assertEquals(part, samePart, "Parts with the same ID should be equal");
        assertEquals(part, part, "Part should be equal to itself");
        assertNotEquals(part, differentPart, "Parts with different IDs should not be equal");
        assertNotEquals(part, null, "Part should not be equal to null");
        assertNotEquals(part, "String", "Part should not be equal to an object of a different class");
    }

    @Test
    void testHashCode() {
        Part samePart = new Part("p123", "123", "P1", "Oil", "Desc", 50.0, 1);

        assertEquals(part.hashCode(), samePart.hashCode(), "Hash codes must be identical for objects with same ID");
    }
}
