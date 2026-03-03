package Repository.Hairdresser;

import Model.Hairdresser;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface HairdresserRepository {
    Optional<String> getHairdresserNameById(int id) throws SQLException;
    List<Hairdresser> getHairdressers() throws SQLException;

}
