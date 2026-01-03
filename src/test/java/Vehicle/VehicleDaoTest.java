package Vehicle;

import org.mockito.Mock;
import pl.sobczak.grzegorz.dao.VehicleDao;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class VehicleDaoTest {
    private VehicleDao vehicleDao;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;
}
