import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.sobczak.grzegorz.dao.UserDao;
import pl.sobczak.grzegorz.service.UserService;

public class UserServiceTest {
    private UserService userService;

    @Mock
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userDao);
    }
}
