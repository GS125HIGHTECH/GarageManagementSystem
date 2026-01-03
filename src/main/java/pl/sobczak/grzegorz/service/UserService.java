package pl.sobczak.grzegorz.service;

import pl.sobczak.grzegorz.dao.UserDao;
import pl.sobczak.grzegorz.model.User;

public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User registerNewUser(String firstName, String lastName, String email, String password) {
        if (userDao.getUserByEmail(email).isPresent()) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }

        User newUser = new User(firstName, lastName, email, password);
        userDao.saveUser(newUser);
        return newUser;
    }

    public void changeUserRole(String email, String newRole) {
        User user = userDao.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.updateRole(newRole);
        userDao.updateUser(user);
    }

    public void deactivateUser(String email) {
        User user = userDao.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.deactivate();
        userDao.updateUser(user);
    }

    public boolean authenticate(String email, String password) {
        return userDao.getUserByEmail(email)
                .map(user -> user.checkPassword(password) && user.isActive())
                .orElse(false);
    }
}
