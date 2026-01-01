package pl.sobczak.grzegorz;

import pl.sobczak.grzegorz.dao.UserDao;
import pl.sobczak.grzegorz.db.DatabaseConnection;
import pl.sobczak.grzegorz.model.User;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection.initDatabase();

        try(Connection conn = DatabaseConnection.getConnection()) {
            User testUser = new User("Jan", "Kowalski", "jan@kowalski.pl", "test123");

            UserDao userDao = new UserDao(conn);
            userDao.saveUser(testUser);

        } catch (IllegalArgumentException | SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
