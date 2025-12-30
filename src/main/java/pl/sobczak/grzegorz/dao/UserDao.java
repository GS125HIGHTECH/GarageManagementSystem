package pl.sobczak.grzegorz.dao;

import pl.sobczak.grzegorz.db.DatabaseConnection;
import pl.sobczak.grzegorz.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class UserDao {
    public void saveUser(User user) {
        String sql = "INSERT INTO users(firstName, lastName, email, password) VALUES(?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getFirstName());
            pstmt.setString(3, user.getEmail());

            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
