package RepairOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.sobczak.grzegorz.dao.RepairOrderDao;
import pl.sobczak.grzegorz.model.RepairOrder;
import pl.sobczak.grzegorz.model.RepairStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
    void shouldSaveRepairOrder() throws SQLException {
        // Given
        RepairOrder order = new RepairOrder("123", "vehicle-123", "repair", 123);

        // When
        repairOrderDao.save(order);

        // Then
        verify(mockPreparedStatement).setString(1, order.getId());
        verify(mockPreparedStatement).setString(2, "vehicle-123");
        verify(mockPreparedStatement).setString(3, "repair");
        verify(mockPreparedStatement).setDouble(4, 123);
        verify(mockPreparedStatement).setString(5, "OPEN");
        verify(mockPreparedStatement, times(1)).executeUpdate();
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

        // When
        var result = repairOrderDao.findById("123");

        // Then
        assertTrue(result.isPresent());
        assertEquals("repair", result.get().getDescription());
        assertEquals(RepairStatus.IN_PROGRESS, result.get().getStatus());
        verify(mockPreparedStatement).setString(1, "123");
    }

    @Test
    void shouldFindOrdersByVehicleId() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getString("id")).thenReturn("o1", "o2");
        when(mockResultSet.getString("vehicleId")).thenReturn("vehicle-123");
        when(mockResultSet.getString("description")).thenReturn("repair1", "repair2");
        when(mockResultSet.getDouble("cost")).thenReturn(100.0, 200.0);
        when(mockResultSet.getString("status")).thenReturn("OPEN", "COMPLETED");

        // When
        List<RepairOrder> result = repairOrderDao.findByVehicleId("vehicle-123");

        // Then
        assertEquals(2, result.size());
        assertEquals(RepairStatus.COMPLETED, result.get(1).getStatus());
        verify(mockPreparedStatement).setString(1, "vehicle-123");
    }

    @Test
    void shouldUpdateRepairOrder() throws SQLException {
        // Given
        RepairOrder order = new RepairOrder("123", "vehicle-123", "repair", 300.0);
        order.updateStatus(RepairStatus.COMPLETED);

        // When
        repairOrderDao.update(order);

        // Then
        verify(mockPreparedStatement).setString(1, "repair");
        verify(mockPreparedStatement).setDouble(2, 300.0);
        verify(mockPreparedStatement).setString(3, "COMPLETED");
        verify(mockPreparedStatement).setString(4, "123");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void shouldDeleteRepairOrder() throws SQLException {
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
        assertEquals("Error updating repair order", exception.getMessage());
    }

    @Test
    void deleteVehicleShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Delete failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> repairOrderDao.delete("123"));
        assertEquals("Error deleting repair order", exception.getMessage());
    }
}
