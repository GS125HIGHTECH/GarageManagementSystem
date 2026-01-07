package pl.sobczak.grzegorz;

import pl.sobczak.grzegorz.dao.RepairOrderDao;
import pl.sobczak.grzegorz.dao.UserDao;
import pl.sobczak.grzegorz.dao.VehicleDao;
import pl.sobczak.grzegorz.db.DatabaseConnection;
import pl.sobczak.grzegorz.model.Role;
import pl.sobczak.grzegorz.model.User;
import pl.sobczak.grzegorz.model.Vehicle;
import pl.sobczak.grzegorz.model.RepairOrder;
import pl.sobczak.grzegorz.service.RepairOrderService;
import pl.sobczak.grzegorz.service.UserService;
import pl.sobczak.grzegorz.service.VehicleService;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection.initDatabase();

        try(Connection conn = DatabaseConnection.getConnection()) {
            UserDao userDao = new UserDao(conn);
            VehicleDao vehicleDao = new VehicleDao(conn);
            RepairOrderDao repairDao = new RepairOrderDao(conn);

            UserService userService = new UserService(userDao);
            VehicleService vehicleService = new VehicleService(vehicleDao, userDao);
            RepairOrderService repairService = new RepairOrderService(repairDao, vehicleDao);

            User testUser = userService.registerNewUser("Jan", "Kowalski", "jan@kowalski.pl", "test123");
            User testInactiveUser = userService.registerNewUser("Jan", "Nowak", "jan@nowak.pl", "test123");
            User testAdminUser = userService.registerNewUser("Adam", "Nowak", "adam@nowak.pl", "test123");

            userService.changeUserRole(testAdminUser.getEmail(), Role.ADMIN);
            userService.deactivateUser(testInactiveUser.getEmail());

            userDao.getUserByEmail("jan@nowak.pl").ifPresent(user ->
                    System.out.println(user.isActive())
            );

            userDao.getUserByEmail("adam@nowak.pl").ifPresent(user ->
                    System.out.println(user.getRole())
            );

            Vehicle a1 = vehicleService.registerNewVehicle(testUser.getUserId(), "Dodge", "Ram", "2B6HB21Y8LK730520", "White");
            Vehicle a2 = vehicleService.registerNewVehicle(testUser.getUserId(), "Honda", "Civic", "JH2SC68W6EK000230", "Red");

            Vehicle b1 = vehicleService.registerNewVehicle(testAdminUser.getUserId(), "Toyota", "Camry", "4T1BF1FK8DU251252", "Blue");

            RepairOrder ro1 = new RepairOrder(a1.getId(), "repair1", 250.0);
            RepairOrder ro2 = new RepairOrder(a2.getId(), "repair2", 1000.0);
            RepairOrder ro3 = new RepairOrder(b1.getId(), "repair3", 100.0);

            repairService.createOrder(ro1);
            repairService.createOrder(ro2);
            repairService.createOrder(ro3);

            repairService.completeRepair(ro1.getId());

            System.out.println(repairService.getTotalRepairCostsForVehicle(a1.getId()) + " PLN");
            System.out.println(vehicleService.getVehiclesByOwner(testUser.getUserId()).size());

        } catch (IllegalArgumentException | SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
