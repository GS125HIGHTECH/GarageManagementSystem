package Vehicle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.sobczak.grzegorz.dao.UserDao;
import pl.sobczak.grzegorz.dao.VehicleDao;
import pl.sobczak.grzegorz.model.User;
import pl.sobczak.grzegorz.model.Vehicle;
import pl.sobczak.grzegorz.service.VehicleService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VehicleServiceTest {
    private VehicleService vehicleService;

    @Mock
    private VehicleDao vehicleDao;

    @Mock
    private UserDao userDao;

    private final String validVin = "12345678901234567";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        vehicleService = new VehicleService(vehicleDao, userDao);
    }

    @Test
    void shouldRegisterNewVehicleSuccessfully() {
        // Given
        String ownerId = "own123";
        User owner = new User(ownerId, "Jan", "Kowalski", "jan@test.pl", "123456");

        when(userDao.getUserById(ownerId)).thenReturn(Optional.of(owner));
        when(vehicleDao.findByVin(validVin)).thenReturn(Optional.empty());

        // When
        Vehicle result = vehicleService.registerNewVehicle(ownerId, "Toyota", "Corolla", validVin, "Red");

        // Then
        assertNotNull(result);
        assertEquals("Toyota", result.getBrand());
        assertEquals(ownerId, result.getOwnerId());
        verify(vehicleDao, times(1)).save(any(Vehicle.class));
    }
    @Test
    void shouldReturnVehiclesForOwner() {
        // Given
        String ownerId = "own123";
        when(vehicleDao.findByOwnerId(ownerId)).thenReturn(List.of(
                new Vehicle(ownerId, "Ford", "Focus", "12345678901234567", "Blue")
        ));

        // When
        var result = vehicleService.getVehiclesByOwner(ownerId);

        // Then
        assertEquals(1, result.size());
        verify(vehicleDao).findByOwnerId(ownerId);
    }

    @Test
    void shouldChangeColorSuccessfully() {
        // Given
        Vehicle vehicle = new Vehicle("own123", "Ford", "Focus", validVin, "Blue");
        when(vehicleDao.findByVin(validVin)).thenReturn(Optional.of(vehicle));

        // When
        vehicleService.changeVehicleColor(validVin, "Yellow");

        // Then
        assertEquals("Yellow", vehicle.getColor());
        verify(vehicleDao).update(vehicle);
    }

    @Test
    void shouldThrowExceptionWhenOwnerDoesNotExist() {
        // Given
        String ownerId = "nonexist";
        when(userDao.getUserById(ownerId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                vehicleService.registerNewVehicle(ownerId, "Audi", "A4", validVin, "Black")
        );

        assertEquals("Cannot register vehicle: Owner not found", exception.getMessage());
        verify(vehicleDao, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowExceptionWhenVinAlreadyExists() {
        // Given
        String ownerId = "own123";
        User owner = new User(ownerId, "Jan", "Kowalski", "jan@test.pl", "123456");
        Vehicle existingVehicle = new Vehicle(ownerId, "BMW", "M3", validVin, "Blue");

        when(userDao.getUserById(ownerId)).thenReturn(Optional.of(owner));
        when(vehicleDao.findByVin(validVin)).thenReturn(Optional.of(existingVehicle));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                vehicleService.registerNewVehicle(ownerId, "Toyota", "Corolla", validVin, "Red")
        );

        assertEquals("Vehicle with this VIN already exists", exception.getMessage());
        verify(vehicleDao, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowExceptionWhenChangingColorForNonExistentVehicle() {
        // Given
        when(vehicleDao.findByVin(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () ->
                vehicleService.changeVehicleColor("invalid", "Red")
        );
    }
}
