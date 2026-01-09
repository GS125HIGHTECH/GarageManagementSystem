package RepairOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.sobczak.grzegorz.dao.RepairOrderDao;
import pl.sobczak.grzegorz.model.Part;
import pl.sobczak.grzegorz.model.RepairOrder;
import pl.sobczak.grzegorz.model.RepairStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RepairOrderDaoTest {
    private RepairOrderDao repairOrderDao;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        repairOrderDao = new RepairOrderDao(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    }

    @Test
    void shouldSaveRepairOrderWithParts() throws SQLException {
        // Given
        RepairOrder order = new RepairOrder("123", "vehicle-123", "repair", 123);
        order.addPart(new Part("p123", "123", "P1", "Part1", "Desc", 50.0, 2));

        // When
        repairOrderDao.save(order);

        // Then
        verify(mockConnection).setAutoCommit(false);
        verify(mockPreparedStatement).setString(1, order.getId());
        verify(mockPreparedStatement).setString(2, "vehicle-123");
        verify(mockPreparedStatement).setString(3, "repair");
        verify(mockPreparedStatement).setDouble(4, 123);
        verify(mockPreparedStatement).setString(5, "OPEN");
        verify(mockPreparedStatement, times(2)).executeUpdate();
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
    }

    @Test
    void shouldFindRepairOrderById() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("id")).thenReturn("123");
        when(mockResultSet.getString("vehicleId")).thenReturn("vehicle-123");
        when(mockResultSet.getString("description")).thenReturn("repair");
        when(mockResultSet.getDouble("cost")).thenReturn(500.0);
        when(mockResultSet.getString("status")).thenReturn("IN_PROGRESS");

        ResultSet mockPartsResultSet = mock(ResultSet.class);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet, mockPartsResultSet);
        when(mockPartsResultSet.next()).thenReturn(false);

        // When
        var result = repairOrderDao.findById("123");

        // Then
        assertTrue(result.isPresent());
        assertEquals("repair", result.get().getDescription());
        assertEquals(RepairStatus.IN_PROGRESS, result.get().getStatus());
        verify(mockPreparedStatement, times(2)).setString(1, "123");
        verify(mockPreparedStatement, times(2)).executeQuery();
    }

    @Test
    void shouldFindOrdersByVehicleId() throws SQLException {
        // Given
        ResultSet mockPartsResultSet = mock(ResultSet.class);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet, mockPartsResultSet, mockPartsResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getString("id")).thenReturn("o1", "o2");
        when(mockResultSet.getString("vehicleId")).thenReturn("vehicle-123");
        when(mockResultSet.getString("description")).thenReturn("repair1", "repair2");
        when(mockResultSet.getDouble("cost")).thenReturn(100.0, 200.0);
        when(mockResultSet.getString("status")).thenReturn("OPEN", "COMPLETED");
        when(mockPartsResultSet.next()).thenReturn(false);

        // When
        List<RepairOrder> result = repairOrderDao.findByVehicleId("vehicle-123");

        // Then
        assertEquals(2, result.size());
        assertEquals(RepairStatus.COMPLETED, result.get(1).getStatus());
        verify(mockPreparedStatement).setString(1, "vehicle-123");
        verify(mockPreparedStatement, times(3)).executeQuery();
    }

    @Test
    void findByVehicleIdShouldLoadPartsForEveryOrder() throws SQLException {
        // Given
        ResultSet rsOrders = mock(ResultSet.class);
        ResultSet rsParts = mock(ResultSet.class);

        when(mockPreparedStatement.executeQuery()).thenReturn(rsOrders, rsParts, rsParts);
        when(rsOrders.next()).thenReturn(true, true, false);
        when(rsParts.next()).thenReturn(false);

        when(rsOrders.getString("id")).thenReturn("o1", "o2");
        when(rsOrders.getString("vehicleId")).thenReturn("vehicle-123");
        when(rsOrders.getString("status")).thenReturn("OPEN");

        // When
        List<RepairOrder> results = repairOrderDao.findByVehicleId("v-1");

        // Then
        assertEquals(2, results.size());
        verify(mockPreparedStatement, times(3)).executeQuery();
    }

    @Test
    void shouldUpdateRepairOrderAndSyncParts() throws SQLException {
        // Given
        RepairOrder order = new RepairOrder("123", "vehicle-123", "repair", 300.0);
        order.addPart(new Part("p123", "123", "P1", "Part1", "Desc", 50.0, 2));
        order.updateStatus(RepairStatus.COMPLETED);

        // When
        repairOrderDao.update(order);

        // Then
        verify(mockConnection).setAutoCommit(false);
        verify(mockPreparedStatement).setString(1, "repair");
        verify(mockPreparedStatement).setDouble(2, 300.0);
        verify(mockPreparedStatement).setString(3, "COMPLETED");
        verify(mockPreparedStatement).setString(4, "123");
        verify(mockPreparedStatement, times(3)).executeUpdate();
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
    }

    @Test
    void shouldLoadPartsSuccessfully() throws SQLException {
        // Given
        ResultSet mockOrderRs = mock(ResultSet.class);
        when(mockOrderRs.next()).thenReturn(true);
        when(mockOrderRs.getString("id")).thenReturn("o1");
        when(mockOrderRs.getString("vehicleId")).thenReturn("vehicle-123");
        when(mockOrderRs.getString("description")).thenReturn("desc");
        when(mockOrderRs.getString("status")).thenReturn("OPEN");
        when(mockOrderRs.getDouble("cost")).thenReturn(100.0);

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("id")).thenReturn("p1", "p2");
        when(mockResultSet.getString("repairOrderId")).thenReturn("o1");
        when(mockResultSet.getString("partCode")).thenReturn("PC1", "PC2");
        when(mockResultSet.getString("name")).thenReturn("Oil", "Filter");
        when(mockResultSet.getDouble("price")).thenReturn(50.0, 20.0);
        when(mockResultSet.getInt("quantity")).thenReturn(1, 1);

        when(mockPreparedStatement.executeQuery()).thenReturn(mockOrderRs, mockResultSet);

        // When
        Optional<RepairOrder> result = repairOrderDao.findById("o1");

        // Then
        assertTrue(result.isPresent());
        assertEquals(2, result.get().getParts().size());
        assertEquals("Oil", result.get().getParts().get(0).getName());
        assertEquals("Filter", result.get().getParts().get(1).getName());
        assertEquals(170.0, result.get().getTotalCost());
    }

    @Test
    void shouldDeleteOrderAndRelyOnCascade() throws SQLException {
        // When
        repairOrderDao.delete("123");

        // Then
        verify(mockPreparedStatement).setString(1, "123");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void shouldReturnOptionalEmptyWhenOrderNotFound() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // When
        var result = repairOrderDao.findById("123");

        // Then
        assertTrue(result.isEmpty());
        verify(mockPreparedStatement).setString(1, "123");
        verify(mockResultSet, never()).getString(anyString());
    }

    @Test
    void shouldReturnEmptyListWhenNoOrdersForVehicle() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // When
        List<RepairOrder> result = repairOrderDao.findByVehicleId("vehicle-123");

        // Then
        assertTrue(result.isEmpty());
        verify(mockPreparedStatement).setString(1, "vehicle-123");
    }

    @Test
    void saveShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        RepairOrder order = new RepairOrder("vehicle-123", "fail", 0);
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> repairOrderDao.save(order));
    }

    @Test
    void findByVehicleIdShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Query failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> repairOrderDao.findByVehicleId("vehicle-123"));
        assertEquals("Error fetching repair orders for vehicle", exception.getMessage());
    }

    @Test
    void findByIdShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Query failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> repairOrderDao.findById("123"));
        assertEquals("Error finding repair order by ID", exception.getMessage());
    }

    @Test
    void updateShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        RepairOrder repairOrder = new RepairOrder("123", "vehicle-123", "invalid", 123);
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Update failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> repairOrderDao.update(repairOrder));
        assertEquals("Error updating repair order and its parts", exception.getMessage());
    }

    @Test
    void deleteRepairOrderShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Delete failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> repairOrderDao.delete("123"));
        assertEquals("Error deleting repair order", exception.getMessage());
    }

    @Test
    void saveShouldRollbackOnException() throws SQLException {
        // Given
        RepairOrder order = new RepairOrder("vehicle-123", "fail", 0);
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("DB Fail"));

        // When & Then
        assertThrows(RuntimeException.class, () -> repairOrderDao.save(order));
        verify(mockConnection).rollback();
        verify(mockConnection).setAutoCommit(true);
    }

    @Test
    void updateShouldRollbackOnException() throws SQLException {
        // Given
        RepairOrder order = new RepairOrder("123", "vehicle-123", "fail", 0);
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("DB Fail"));

        // When & Then
        assertThrows(RuntimeException.class, () -> repairOrderDao.update(order));
        verify(mockConnection).rollback();
    }

    @Test
    void shouldHandleRollbackExceptionQuietly() throws SQLException {
        // Given
        RepairOrder order = new RepairOrder("vehicle-123", "fail", 0);
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("First Error"));
        doThrow(new SQLException("Rollback Error")).when(mockConnection).rollback();

        // When & Then
        assertThrows(RuntimeException.class, () -> repairOrderDao.save(order));
    }

    @Test
    void shouldHandleResetAutoCommitExceptionQuietly() throws SQLException {
        // Given
        RepairOrder order = new RepairOrder("vehicle-123", "desc", 0);
        doThrow(new SQLException("AutoCommit Error")).when(mockConnection).setAutoCommit(true);

        // When
        repairOrderDao.save(order);

        // Then
        verify(mockConnection).commit();
    }
}
