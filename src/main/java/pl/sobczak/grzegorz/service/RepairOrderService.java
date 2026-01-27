package pl.sobczak.grzegorz.service;

import pl.sobczak.grzegorz.dao.RepairOrderDao;
import pl.sobczak.grzegorz.dao.VehicleDao;
import pl.sobczak.grzegorz.model.Part;
import pl.sobczak.grzegorz.model.RepairStatus;
import pl.sobczak.grzegorz.model.RepairOrder;

import java.util.List;

public record RepairOrderService(RepairOrderDao repairOrderDao, VehicleDao vehicleDao) {

    public void createOrder(RepairOrder order) {
        if (vehicleDao.findById(order.getVehicleId()).isEmpty()) {
            throw new RuntimeException("Cannot create Order: Vehicle not found");
        }
        repairOrderDao.save(order);
    }

    public void addPartToOrder(String orderId, Part part) {
        RepairOrder order = repairOrderDao.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == RepairStatus.COMPLETED || order.getStatus() == RepairStatus.CANCELLED) {
            throw new IllegalStateException("Cannot add parts to a closed or cancelled repair");
        }

        order.addPart(part);

        repairOrderDao.update(order);
    }

    public void completeRepair(String orderId) {
        repairOrderDao.findById(orderId).ifPresent(order -> {
            order.updateStatus(RepairStatus.COMPLETED);
            repairOrderDao.update(order);
        });
    }

    public void cancelRepair(String orderId) {
        repairOrderDao.findById(orderId).ifPresent(order -> {
            order.updateStatus(RepairStatus.CANCELLED);
            repairOrderDao.update(order);
        });
    }

    public double getTotalRepairCostsForVehicle(String vehicleId) {
        return repairOrderDao.findByVehicleId(vehicleId).stream()
                .mapToDouble(RepairOrder::getTotalCost)
                .sum();
    }

    public List<RepairOrder> getVehicleHistory(String vehicleId) {
        return repairOrderDao.findByVehicleId(vehicleId);
    }
}
