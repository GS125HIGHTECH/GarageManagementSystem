package Vehicle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.sobczak.grzegorz.dao.VehicleDao;
import pl.sobczak.grzegorz.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class VehicleDaoTest {
    private VehicleDao vehicleDao;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private final String validVin = "12345678901234567";

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        vehicleDao = new VehicleDao(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    }

    @Test
    void shouldSaveVehicle() throws SQLException {
        // Given
        Vehicle vehicle = new Vehicle("123", "own123", "Toyota", "Corolla", validVin, "Red");

        // When
        vehicleDao.save(vehicle);

        // Then
        verify(mockPreparedStatement).setString(1, "123");
        verify(mockPreparedStatement).setString(2, "own123");
        verify(mockPreparedStatement).setString(3, "Toyota");
        verify(mockPreparedStatement).setString(4, "Corolla");
        verify(mockPreparedStatement).setString(5, validVin);
        verify(mockPreparedStatement).setString(6, "Red");
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    void shouldFindVehicleById() throws SQLException {
        // Given
        String id = "123";
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("id")).thenReturn(id);
        when(mockResultSet.getString("ownerId")).thenReturn("own123");
        when(mockResultSet.getString("brand")).thenReturn("Toyota");
        when(mockResultSet.getString("model")).thenReturn("Corolla");
        when(mockResultSet.getString("vin")).thenReturn(validVin);
        when(mockResultSet.getString("color")).thenReturn("Red");

        // When
        var result = vehicleDao.findById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(mockPreparedStatement).setString(1, id);
    }

    @Test
    void shouldReturnOptionalEmptyWhenIdNotFound() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // When
        var result = vehicleDao.findById("non-existent");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindVehicleByVin() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("id")).thenReturn("123");
        when(mockResultSet.getString("ownerId")).thenReturn("own123");
        when(mockResultSet.getString("brand")).thenReturn("Toyota");
        when(mockResultSet.getString("model")).thenReturn("Corolla");
        when(mockResultSet.getString("vin")).thenReturn(validVin);
        when(mockResultSet.getString("color")).thenReturn("Red");

        // When
        var result = vehicleDao.findByVin(validVin);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Toyota", result.get().getBrand());
        assertEquals(validVin, result.get().getVin());
        verify(mockPreparedStatement).setString(1, validVin);
    }

    @Test
    void shouldReturnEmptyWhenVinNotFound() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // When
        var result = vehicleDao.findByVin("non-existent");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindVehiclesByOwnerId() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getString("id")).thenReturn("123", "1234");
        when(mockResultSet.getString("ownerId")).thenReturn("own123");
        when(mockResultSet.getString("brand")).thenReturn("Toyota", "Honda");
        when(mockResultSet.getString("model")).thenReturn("Corolla", "Civic");
        when(mockResultSet.getString("vin")).thenReturn(validVin, "76543210987654321");
        when(mockResultSet.getString("color")).thenReturn("Red", "Blue");

        // When
        List<Vehicle> result = vehicleDao.findByOwnerId("own123");

        // Then
        assertEquals(2, result.size());
        assertEquals("Toyota", result.get(0).getBrand());
        assertEquals("Honda", result.get(1).getBrand());
        verify(mockPreparedStatement).setString(1, "own123");
    }

    @Test
    void shouldUpdateVehicleColor() throws SQLException {
        // Given
        Vehicle vehicle = new Vehicle("123", "own123", "Toyota", "Corolla", validVin, "Blue");

        // When
        vehicleDao.update(vehicle);

        // Then
        verify(mockPreparedStatement).setString(1, "Blue");
        verify(mockPreparedStatement).setString(2, "123");
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    void shouldDeleteVehicle() throws SQLException {
        // When
        vehicleDao.delete("123");

        // Then
        verify(mockPreparedStatement).setString(1, "123");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void saveShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        Vehicle vehicle = new Vehicle("123", "Toyota", "Corolla", validVin, "Red");
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("DB Error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> vehicleDao.save(vehicle));
        assertEquals("Error saving vehicle", exception.getMessage());
    }

    @Test
    void findByOwnerIdShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Select failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> vehicleDao.findByOwnerId("own123"));
        assertTrue(exception.getMessage().contains("Error fetching vehicles for owner"));
    }

    @Test
    void findByVinShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Query failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> vehicleDao.findByVin(validVin));
        assertEquals("Error finding vehicle by VIN", exception.getMessage());
    }

    @Test
    void updateShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        Vehicle vehicle = new Vehicle("123", "own123", "Toyota", "Corolla", validVin, "Blue");
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Update failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> vehicleDao.update(vehicle));
        assertEquals("Error updating vehicle color", exception.getMessage());
    }

    @Test
    void deleteVehicleShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Delete failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> vehicleDao.delete("123"));
        assertEquals("Error deleting vehicle", exception.getMessage());
    }

    @Test
    void findByIdShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Query failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> vehicleDao.findById("123"));
        assertEquals("Error finding vehicle by ID", exception.getMessage());
    }
}
