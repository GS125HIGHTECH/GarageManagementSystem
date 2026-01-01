package pl.sobczak.grzegorz.model;

import java.util.Objects;
import java.util.UUID;

public class User {
    private final String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
    private boolean isActive;

    public User(String userId, String firstName, String lastName, String email, String password) {
        validateName(firstName, "First name");
        validateName(lastName, "Last name");
        validateEmail(email);
        validatePassword(password);

        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = "USER";
        this.isActive = true;
    }

    public User(String firstName, String lastName, String email, String password) {
        this(UUID.randomUUID().toString(), firstName, lastName, email, password);
    }

    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUserId() { return userId; }
    public boolean isActive() { return isActive; }
    public String getRole() { return role; }

    public void changeFirstName(String newFirstName) {
        validateName(newFirstName, "First name");
        this.firstName = newFirstName;
    }

    public void changeLastName(String newLastName) {
        validateName(newLastName, "Last name");
        this.lastName = newLastName;
    }

    public void changePassword(String newPassword) {
        validatePassword(newPassword);
        this.password = newPassword;
    }

    public void changeEmail(String newEmail) {
        validateEmail(newEmail);
        this.email = newEmail;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean checkPassword(String candidate) {
        return this.password.equals(candidate);
    }

    public void updateRole(String newRole) {
        if (newRole == null || newRole.isEmpty()) {
            throw new IllegalArgumentException("Role cannot be empty");
        }
        this.role = newRole.toUpperCase();
    }

    private void validateEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password too short");
        }
    }

    private void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + userId + '\'' +
                ", name='" + firstName + " " + lastName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", active=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
