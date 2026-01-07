package pl.sobczak.grzegorz.service;

import pl.sobczak.grzegorz.dao.RepairOrderDao;
import pl.sobczak.grzegorz.dao.VehicleDao;
import pl.sobczak.grzegorz.model.RepairStatus;
import pl.sobczak.grzegorz.model.RepairOrder;

import java.util.List;

public class RepairOrderService {
    private final RepairOrderDao repairOrderDao;
    private final VehicleDao vehicleDao;

    public RepairOrderService(RepairOrderDao repairOrderDao, VehicleDao vehicleDao) {
        this.repairOrderDao = repairOrderDao;
        this.vehicleDao = vehicleDao;
    }

    public void createOrder(RepairOrder order) {
        if (vehicleDao.findById(order.getVehicleId()).isEmpty()) {
            throw new RuntimeException("Cannot create Order: Vehicle not found");
        }
        repairOrderDao.save(order);
    }

    public void completeRepair(String orderId) {
        repairOrderDao.findById(orderId).ifPresent(order -> {
            order.updateStatus(RepairStatus.COMPLETED);
            repairOrderDao.update(order);
        });
    }

    public double getTotalRepairCostsForVehicle(String vehicleId) {
        return repairOrderDao.findByVehicleId(vehicleId).stream()
                .mapToDouble(RepairOrder::getCost)
                .sum();
    }

    public List<RepairOrder> getVehicleHistory(String vehicleId) {
        return repairOrderDao.findByVehicleId(vehicleId);
    }
}
