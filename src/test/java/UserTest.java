import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sobczak.grzegorz.model.User;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Jan", "Kowalski", "jan@test.pl", "secret123");
    }

    @Test
    void testInitialization() {
        assertEquals("Jan", user.getFirstName());
        assertEquals("Kowalski", user.getLastName());
        assertTrue(user.checkPassword("secret123"));
        assertTrue(user.isActive());
        assertEquals("jan@test.pl", user.getEmail());
    }

    @Test
    void shouldThrowExceptionForInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () -> new User("Jan", "Kowalski", "zly_email", "123456"));

        assertThrows(IllegalArgumentException.class, () -> new User("Jan", "Kowalski", null, "123456"));
    }

    @Test
    void shouldThrowExceptionForInvalidPassword() {
        assertThrows(IllegalArgumentException.class, () -> new User("Jan", "Kowalski", "jan@test.com", "12345"));

        assertThrows(IllegalArgumentException.class, () -> new User("Jan", "Kowalski", "jan@test.com", null));
    }

    @Test
    void testChangeEmail() {
        user.changeEmail("jan@test.com");
        assertEquals("jan@test.com", user.getEmail());

        assertThrows(IllegalArgumentException.class, () -> user.changeEmail("Jan"));

        assertThrows(IllegalArgumentException.class, () -> user.changeEmail(null));
    }

    @Test
    void testChangeFirstName() {
        user.changeFirstName("jan");
        assertEquals("jan", user.getFirstName());

        assertThrows(IllegalArgumentException.class, () -> user.changeFirstName(""));

        assertThrows(IllegalArgumentException.class, () -> user.changeFirstName(null));
    }

    @Test
    void testChangeLastName() {
        user.changeLastName("jan");
        assertEquals("jan", user.getLastName());

        assertThrows(IllegalArgumentException.class, () -> user.changeLastName(""));

        assertThrows(IllegalArgumentException.class, () -> user.changeLastName(null));
    }

    @Test
    void testChangePassword() {
        user.changePassword("123456");
        assertTrue(user.checkPassword("123456"));

        assertThrows(IllegalArgumentException.class, () -> user.changePassword(""));
        assertThrows(IllegalArgumentException.class, () -> user.changePassword("12345"));
        assertThrows(IllegalArgumentException.class, () -> user.changePassword(null));
    }

    @Test
    void testDeactivate() {
        user.deactivate();
        assertFalse(user.isActive());
    }

    @Test
    void shouldUpdateRoleAndConvertToUpperCase() {
        user.updateRole("admin");
        assertEquals("ADMIN", user.getRole());
    }

    @Test
    void shouldThrowExceptionWhenRoleIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> user.updateRole(""));
        assertThrows(IllegalArgumentException.class, () -> user.updateRole(null));
    }

    @Test
    void testEquals() {
        User sameUser = new User(user.getUserId(), "Marek", "Nowak", "marek@test.pl", "123678");
        User differentUser = new User("Jan", "Kowalski", "jan@test.pl", "secret123");

        assertEquals(user, sameUser, "Users with the same ID should be equal");
        assertEquals(user, user, "User should be equal to itself");
        assertNotEquals(user, differentUser, "Users with different IDs should not be equal");
        assertNotEquals(user, null, "User should not be equal to null");
        assertNotEquals(user, "String", "User should not be equal to an object of a different class");
    }

    @Test
    void testHashCode() {
        User sameUser = new User(user.getUserId(), "Jan", "Kowalski", "jan@test.pl", "secret123");

        assertEquals(user.hashCode(), sameUser.hashCode(), "Hash codes must be identical for objects that are equal according to equals()");
    }

    @Test
    void testToString() {
        String result = user.toString();

        assertTrue(result.contains(user.getUserId()));
        assertTrue(result.contains("Jan Kowalski"));
        assertTrue(result.contains("jan@test.pl"));
        assertTrue(result.contains("USER"));
        assertTrue(result.contains("active=true"));
    }
}
