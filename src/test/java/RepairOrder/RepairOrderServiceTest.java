package RepairOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.sobczak.grzegorz.dao.RepairOrderDao;
import pl.sobczak.grzegorz.dao.VehicleDao;
import pl.sobczak.grzegorz.model.Part;
import pl.sobczak.grzegorz.model.RepairOrder;
import pl.sobczak.grzegorz.model.RepairStatus;
import pl.sobczak.grzegorz.model.Vehicle;
import pl.sobczak.grzegorz.service.RepairOrderService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RepairOrderServiceTest {
    private RepairOrderService repairOrderService;

    @Mock
    private RepairOrderDao repairOrderDao;

    @Mock
    private VehicleDao vehicleDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repairOrderService = new RepairOrderService(repairOrderDao, vehicleDao);
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        // Given
        String vehicleId = "vehicle-123";
        RepairOrder order = new RepairOrder(vehicleId, "repair", 123);

        when(vehicleDao.findById(vehicleId)).thenReturn(Optional.of(mock(Vehicle.class)));

        // When
        repairOrderService.createOrder(order);

        // Then
        verify(vehicleDao).findById(vehicleId);
        verify(repairOrderDao).save(order);
    }

    @Test
    void shouldCompleteRepairSuccessfully() {
        // Given
        String orderId = "123";
        RepairOrder order = new RepairOrder(orderId, "vehicle-123", "repair", 123);
        assertEquals(RepairStatus.OPEN, order.getStatus());
        when(repairOrderDao.findById(orderId)).thenReturn(Optional.of(order));

        // When
        repairOrderService.completeRepair(orderId);

        // Then
        assertEquals(RepairStatus.COMPLETED, order.getStatus());
        verify(repairOrderDao).update(order);
    }

    @Test
    void shouldCalculateTotalRepairCostsForVehicle() {
        // Given
        String vehicleId = "vehicle-123";
        RepairOrder o1 = new RepairOrder(vehicleId, "repair1", 100.0);
        RepairOrder o2 = new RepairOrder(vehicleId, "repair2", 200.5);

        when(repairOrderDao.findByVehicleId(vehicleId)).thenReturn(List.of(o1, o2));

        // When
        double total = repairOrderService.getTotalRepairCostsForVehicle(vehicleId);

        // Then
        assertEquals(300.5, total);
        verify(repairOrderDao).findByVehicleId(vehicleId);
    }

    @Test
    void shouldReturnZeroCostWhenNoRepairsFound() {
        // Given
        String vehicleId = "123";
        when(repairOrderDao.findByVehicleId(vehicleId)).thenReturn(List.of());

        // When
        double total = repairOrderService.getTotalRepairCostsForVehicle(vehicleId);

        // Then
        assertEquals(0.0, total);
        verify(repairOrderDao).findByVehicleId(vehicleId);
    }

    @Test
    void shouldGetVehicleHistory() {
        // Given
        String vehicleId = "vehicle-123";
        List<RepairOrder> history = List.of(
                new RepairOrder(vehicleId, "repair1", 100),
                new RepairOrder(vehicleId, "repair2", 200)
        );
        when(repairOrderDao.findByVehicleId(vehicleId)).thenReturn(history);

        // When
        List<RepairOrder> result = repairOrderService.getVehicleHistory(vehicleId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("repair1", result.getFirst().getDescription());
        verify(repairOrderDao).findByVehicleId(vehicleId);
    }

    @Test
    void shouldAddPartToOrderSuccessfully() {
        // Given
        String orderId = "order-123";
        RepairOrder order = new RepairOrder(orderId, "vehicle-123", "repair", 100.0);
        Part part = new Part("part-1", orderId, "P1", "Oil", "Desc", 50.0, 1);

        when(repairOrderDao.findById(orderId)).thenReturn(Optional.of(order));

        // When
        repairOrderService.addPartToOrder(orderId, part);

        // Then
        assertEquals(1, order.getParts().size());
        assertEquals("Oil", order.getParts().getFirst().getName());
        verify(repairOrderDao).update(order);
    }

    @Test
    void shouldCancelRepairSuccessfully() {
        // Given
        String orderId = "order-123";
        RepairOrder order = new RepairOrder(orderId, "vehicle-123", "repair", 100.0);
        when(repairOrderDao.findById(orderId)).thenReturn(Optional.of(order));

        // When
        repairOrderService.cancelRepair(orderId);

        // Then
        assertEquals(RepairStatus.CANCELLED, order.getStatus());
        verify(repairOrderDao).update(order);
    }

    @Test
    void shouldNotUpdateAnythingWhenCompletingNonExistentRepair() {
        // Given
        String orderId = "non-existent";
        when(repairOrderDao.findById(orderId)).thenReturn(Optional.empty());

        // When
        repairOrderService.completeRepair(orderId);

        // Then
        verify(repairOrderDao, never()).update(any());
    }

    @Test
    void shouldThrowExceptionWhenCreatingOrderForNonExistentVehicle() {
        // Given
        String vehicleId = "non-existent";
        RepairOrder order = new RepairOrder(vehicleId, "repair", 123);
        when(vehicleDao.findById(vehicleId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                repairOrderService.createOrder(order)
        );

        assertEquals("Cannot create Order: Vehicle not found", exception.getMessage());
        verify(repairOrderDao, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenAddingPartToClosedOrder() {
        // Given
        String orderId = "order-123";
        RepairOrder order = new RepairOrder(orderId, "vehicle-123", "repair", 100.0);
        order.updateStatus(RepairStatus.COMPLETED);

        Part part = new Part("part-1", orderId, "P1", "Oil", "Desc", 50.0, 1);
        when(repairOrderDao.findById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                repairOrderService.addPartToOrder(orderId, part)
        );

        assertTrue(exception.getMessage().contains("Cannot add parts to a closed or cancelled repair"));
        verify(repairOrderDao, never()).update(any());
    }

    @Test
    void shouldThrowExceptionWhenAddingPartToCancelledOrder() {
        // Given
        String orderId = "order-123";
        RepairOrder order = new RepairOrder(orderId, "vehicle-123", "repair", 100.0);
        order.updateStatus(RepairStatus.CANCELLED);

        Part part = new Part("part-1", orderId, "P1", "Oil", "Desc", 50.0, 1);
        when(repairOrderDao.findById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                repairOrderService.addPartToOrder(orderId, part)
        );

        assertEquals("Cannot add parts to a closed or cancelled repair", exception.getMessage());
        verify(repairOrderDao, never()).update(any());
    }

    @Test
    void shouldThrowExceptionWhenAddingPartToNonExistentOrder() {
        // Given
        String orderId = "ghost-id";
        Part part = new Part("p1", orderId, "P1", "Oil", "Desc", 10.0, 1);
        when(repairOrderDao.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                repairOrderService.addPartToOrder(orderId, part)
        );

        assertEquals("Order not found", exception.getMessage());
    }
}
