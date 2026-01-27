package pl.sobczak.grzegorz.service;

import pl.sobczak.grzegorz.dao.UserDao;
import pl.sobczak.grzegorz.dao.VehicleDao;
import pl.sobczak.grzegorz.model.Vehicle;

import java.util.List;

public record VehicleService(VehicleDao vehicleDao, UserDao userDao) {

    public Vehicle registerNewVehicle(String ownerId, String brand, String model, String vin, String color) {
        if (userDao.getUserById(ownerId).isEmpty()) {
            throw new RuntimeException("Cannot register vehicle: Owner not found");
        }

        if (vehicleDao.findByVin(vin).isPresent()) {
            throw new RuntimeException("Vehicle with this VIN already exists");
        }

        Vehicle vehicle = new Vehicle(ownerId, brand, model, vin, color);
        vehicleDao.save(vehicle);
        return vehicle;
    }

    public List<Vehicle> getVehiclesByOwner(String ownerId) {
        return vehicleDao.findByOwnerId(ownerId);
    }

    public void changeVehicleColor(String vin, String newColor) {
        Vehicle vehicle = vehicleDao.findByVin(vin)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        vehicle.changeColor(newColor);
        vehicleDao.update(vehicle);
    }
}
