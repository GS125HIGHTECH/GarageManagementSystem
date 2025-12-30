import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sobczak.grzegorz.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Jan", "Kowalski", "jan@test.pl", "tajne123");
    }

    @Test
    void testInitialization() {
        assertEquals("Jan", user.getFirstName());
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
}
