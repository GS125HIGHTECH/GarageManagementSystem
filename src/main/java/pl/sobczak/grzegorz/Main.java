package pl.sobczak.grzegorz;

import pl.sobczak.grzegorz.dao.UserDao;
import pl.sobczak.grzegorz.db.DatabaseConnection;
import pl.sobczak.grzegorz.model.User;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection.initDatabase();

        try {
            User testUser = new User("Jan", "Kowalski", "jan@kowalski.pl", "test123");

            UserDao userDao = new UserDao();
            userDao.saveUser(testUser);

        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
