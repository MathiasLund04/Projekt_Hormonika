package Repository.Hairdresser;

import DAL.DBConfig;
import Model.Hairdresser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLHairdresserRepository implements HairdresserRepository {

    private final DBConfig db;

    public MySQLHairdresserRepository(DBConfig db) {
        this.db = db;
    }

    public Hairdresser getHairdresserById(int id) throws SQLException {
        String sql = "SELECT * FROM Hairdresser WHERE id = ?";

        Hairdresser hairdresser;
        try (Connection conn = db.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()){

                    String name = rs.getString("name");
                    String username = rs.getString("Username");
                    String password = rs.getString("Password");

                    hairdresser = new Hairdresser(name, id, username, password);


                    return hairdresser;
                }
            }
        }
        return null;
    }

    public Optional<String> getHairdresserNameById(int id) throws SQLException {
        String sql = "SELECT name FROM hairdresser WHERE id = ?";

            try(Connection con = db.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)){
                ps.setInt(1,id);
    
                try(ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.ofNullable(rs.getString("name"));
                    }
                }
    
            }
        return Optional.empty();
    }

    public List<Hairdresser> getHairdressers() throws SQLException {
        List<Hairdresser> hairdressers = new ArrayList<>();

        String sql = "SELECT * FROM hairdresser";

        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()){
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String username = rs.getString("Username");
                String password = rs.getString("Password");

                Hairdresser hairdresser = new Hairdresser(name, id, username,password);
                hairdressers.add(hairdresser);
            }
            return hairdressers;

        } catch (SQLException e) {
            throw new SQLException("Kunne ikke indlæse frisører fra DB");
        }

    }
}
