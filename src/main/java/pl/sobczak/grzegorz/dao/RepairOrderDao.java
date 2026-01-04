package pl.sobczak.grzegorz.dao;

import java.sql.Connection;

public class RepairOrderDao {
    private final Connection connection;

    public RepairOrderDao(Connection connection) {
        this.connection = connection;
    }
}
