package pl.sobczak.grzegorz.dao;

import pl.sobczak.grzegorz.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDao {
    private final Connection connection;

    public UserDao(Connection connection) {
        this.connection = connection;
    }

    public void saveUser(User user) {
        String sql = "INSERT INTO users(id, firstName, lastName, email, password, role, isActive) VALUES(?,?,?,?,?,?,?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getFirstName());
            pstmt.setString(3, user.getLastName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, "hidden_password");
            pstmt.setString(6, user.getRole());
            pstmt.setInt(7, user.isActive() ? 1 : 0);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user to database", e);
        }
    }

    public void updateUser(User user) {
        String sql = "UPDATE users SET firstName = ?, lastName = ?, role = ?, isActive = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getFirstName());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getRole());
            pstmt.setInt(4, user.isActive() ? 1 : 0);
            pstmt.setString(5, user.getUserId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    public void deleteUser(String id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    public Optional<User> getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getString("id"),
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            rs.getString("email"),
                            "secret"
                    );

                    user.updateRole(rs.getString("role"));
                    if (rs.getInt("isActive") == 0) user.deactivate();

                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching user", e);
        }
        return Optional.empty();
    }
}
