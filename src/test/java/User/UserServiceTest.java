package User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.sobczak.grzegorz.dao.UserDao;
import pl.sobczak.grzegorz.model.User;
import pl.sobczak.grzegorz.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    private UserService userService;

    @Mock
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userDao);
    }

    @Test
    void shouldRegisterNewUserSuccessfully() {
        // Given
        String email = "new@test.pl";
        when(userDao.getUserByEmail(email)).thenReturn(Optional.empty());

        // When
        User result = userService.registerNewUser("Jan", "Kowalski", email, "test123");

        // Then
        assertNotNull(result);
        assertEquals("Jan", result.getFirstName());
        verify(userDao, times(1)).saveUser(any(User.class));
    }

    @Test
    void shouldUpdateRoleWhenUserExists() {
        // Given
        String email = "admin@test.pl";
        User user = new User("Jan", "Kowalski", email, "test123");
        when(userDao.getUserByEmail(email)).thenReturn(Optional.of(user));

        // When
        userService.changeUserRole(email, "ADMIN");

        // Then
        assertEquals("ADMIN", user.getRole());
        verify(userDao).updateUser(user);
    }

    @Test
    void shouldDeactivateUser() {
        // Given
        String email = "active@test.pl";
        User user = new User("Jan", "Kowalski", email, "test123");
        assertTrue(user.isActive());
        when(userDao.getUserByEmail(email)).thenReturn(Optional.of(user));

        // When
        userService.deactivateUser(email);

        // Then
        assertFalse(user.isActive());
        verify(userDao, times(1)).updateUser(user);
    }

    @Test
    void shouldAuthenticateSuccessfullyWithCorrectCredentials() {
        // Given
        String email = "auth@test.pl";
        String pass = "test123";
        User user = new User("Jan", "Kowalski", email, pass);
        when(userDao.getUserByEmail(email)).thenReturn(Optional.of(user));

        // When
        boolean isAuthenticated = userService.authenticate(email, pass);

        // Then
        assertTrue(isAuthenticated);
    }

    @Test
    void shouldNotAuthenticateWhenUserIsInactive() {
        // Given
        String email = "inactive@test.pl";
        String pass = "test123";
        User user = new User("Jan", "Kowalski", email, pass);
        user.deactivate();
        when(userDao.getUserByEmail(email)).thenReturn(Optional.of(user));

        // When
        boolean isAuthenticated = userService.authenticate(email, pass);

        // Then
        assertFalse(isAuthenticated);
    }

    @Test
    void shouldNotAuthenticateWhenPasswordIsIncorrect() {
        // Given
        String email = "pass@test.pl";
        String pass = "test123";
        User user = new User("Jan", "Kowalski", email, pass);
        when(userDao.getUserByEmail(email)).thenReturn(Optional.of(user));

        // When
        boolean isAuthenticated = userService.authenticate(email, "incorrect");

        // Then
        assertFalse(isAuthenticated);
    }

    @Test
    void shouldThrowExceptionWhenRegisteringExistingEmail() {
        // Given
        String email = "exist@test.pl";
        User existingUser = new User("Jan", "Nowak", email, "test123");
        when(userDao.getUserByEmail(email)).thenReturn(Optional.of(existingUser));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.registerNewUser("Adam", "Nowak", email, "test123")
        );

        assertEquals("User with email " + email + " already exists", exception.getMessage());
        verify(userDao, never()).saveUser(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringRoleChange() {
        // Given
        String email = "empty@test.pl";
        when(userDao.getUserByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.changeUserRole(email, "ADMIN")
        );

        assertEquals("User not found", exception.getMessage());
        verify(userDao, never()).updateUser(any());
    }
}
