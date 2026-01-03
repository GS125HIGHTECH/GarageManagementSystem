package pl.sobczak.grzegorz.dao;

import java.sql.Connection;

public class VehicleDao {
    private final Connection connection;

    public VehicleDao(Connection connection) {
        this.connection = connection;
    }
}
