package User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.sobczak.grzegorz.dao.UserDao;
import pl.sobczak.grzegorz.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserDaoTest {
    private UserDao userDao;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        userDao = new UserDao(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    }

    @Test
    void shouldSaveUser() throws SQLException {
        // Given
        User user = new User("123", "Jan", "Kowalski", "jan@test.pl", "password123");

        // When
        userDao.saveUser(user);

        // Then
        verify(mockPreparedStatement).setString(1, "123");
        verify(mockPreparedStatement).setString(2, "Jan");
        verify(mockPreparedStatement).setString(3, "Kowalski");
        verify(mockPreparedStatement).setString(4, "jan@test.pl");
        verify(mockPreparedStatement).setString(5, "hidden_password");
        verify(mockPreparedStatement).setString(6, "USER");
        verify(mockPreparedStatement).setInt(7, 1);

        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    void shouldUpdateUser() throws SQLException {
        // Given
        User user = new User("123", "Marek", "Nowak", "marek@test.pl", "123456");
        user.updateRole("ADMIN");

        // When
        userDao.updateUser(user);

        // Then
        verify(mockPreparedStatement).setString(1, "Marek");
        verify(mockPreparedStatement).setString(2, "Nowak");
        verify(mockPreparedStatement).setString(3, "ADMIN");
        verify(mockPreparedStatement).setInt(4, 1);
        verify(mockPreparedStatement).setString(5, "123");
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    void shouldDeleteUser() throws SQLException {
        // When
        userDao.deleteUser("123");

        // Then
        verify(mockPreparedStatement).setString(1, "123");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void shouldReturnUserWhenEmailExists() throws SQLException {
        // Given
        String email = "jan@test.pl";
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getString("id")).thenReturn("123");
        when(mockResultSet.getString("firstName")).thenReturn("Jan");
        when(mockResultSet.getString("lastName")).thenReturn("Kowalski");
        when(mockResultSet.getString("email")).thenReturn(email);
        when(mockResultSet.getString("role")).thenReturn("USER");
        when(mockResultSet.getInt("isActive")).thenReturn(1);

        // When
        var result = userDao.getUserByEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Jan", result.get().getFirstName());
        assertEquals("Kowalski", result.get().getLastName());
        assertEquals("USER", result.get().getRole());
        assertTrue(result.get().isActive());

        verify(mockPreparedStatement).setString(1, email);
        verify(mockPreparedStatement, times(1)).executeQuery();
    }

    @Test
    void shouldReturnDeactivatedUserWhenIsActiveIsZero() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("id")).thenReturn("123");
        when(mockResultSet.getString("firstName")).thenReturn("Jan");
        when(mockResultSet.getString("lastName")).thenReturn("Kowalski");
        when(mockResultSet.getString("email")).thenReturn("jan@test.pl");
        when(mockResultSet.getString("role")).thenReturn("USER");
        when(mockResultSet.getInt("isActive")).thenReturn(0);

        // When
        var result = userDao.getUserByEmail("jan@test.pl");

        // Then
        assertTrue(result.isPresent());
        assertFalse(result.get().isActive());
    }

    @Test
    void shouldReturnUserWhenIdExists() throws SQLException {
        // Given
        String userId = "123";
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getString("id")).thenReturn(userId);
        when(mockResultSet.getString("firstName")).thenReturn("Jan");
        when(mockResultSet.getString("lastName")).thenReturn("Kowalski");
        when(mockResultSet.getString("email")).thenReturn("jan@test.pl");
        when(mockResultSet.getString("role")).thenReturn("USER");
        when(mockResultSet.getInt("isActive")).thenReturn(1);

        // When
        var result = userDao.getUserById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getUserId());
        assertEquals("Jan", result.get().getFirstName());
        verify(mockPreparedStatement).setString(1, userId);
        verify(mockPreparedStatement, times(1)).executeQuery();
    }

    @Test
    void shouldReturnEmptyOptionalWhenUserNotFoundByIdOrEmail() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // When
        var result = userDao.getUserByEmail("not@found.pl");
        var result2 = userDao.getUserById("123");

        // Then
        assertTrue(result.isEmpty());
        assertTrue(result2.isEmpty());

        verify(mockPreparedStatement, times(2)).executeQuery();
    }

    @Test
    void saveUserShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        User user = new User("Jan", "Kowalski", "jan@test.pl", "password123");
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userDao.saveUser(user));
        assertEquals("Error saving user to database", exception.getMessage());
    }

    @Test
    void updateUserShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        User user = new User("123", "Jan", "Kowalski", "jan@test.pl", "password123");
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Update failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userDao.updateUser(user));
        assertEquals("Error updating user", exception.getMessage());
    }

    @Test
    void deleteUserShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Delete failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userDao.deleteUser("123"));
        assertEquals("Error deleting user", exception.getMessage());
    }

    @Test
    void getUserByEmailShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Fetch failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userDao.getUserByEmail("test@pl"));
        assertEquals("Error fetching user", exception.getMessage());
    }

    @Test
    void getUserByIdShouldThrowRuntimeExceptionOnSqlException() throws SQLException {
        // Given
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Fetch failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userDao.getUserById("123"));
        assertEquals("Error fetching user", exception.getMessage());
    }
}
