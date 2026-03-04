package Repository.Hairdresser;

import Model.Hairdresser;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface HairdresserRepository {
    Hairdresser getHairdresserById(int id) throws SQLException;
    Optional<String> getHairdresserNameById(int id) throws SQLException;
    List<Hairdresser> getHairdressers() throws SQLException;

}
