package Repository.Customer;

import DAL.DBConfig;
import Model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLCustomerRepository implements CustomerRepository {
    public final DBConfig db;

    public MySQLCustomerRepository(DBConfig db) {
        this.db = db;
    }

    public void createCustomerIfNotExist(String name, String phoneNum) throws SQLException {
        String sql = "INSERT INTO customer (name, phoneNUM) VALUES (?, ?)";
            try(Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, phoneNum);
                ps.executeUpdate();
            } catch (SQLException e){
                throw new SQLException("Fejl i tilføjelse af Ny Kunde");
            }
    }

    public Optional<String> getCustomerNameByPhoneNum(String phoneNum) {
        String  sql = "SELECT name FROM customer WHERE phoneNUM = ?";
        try(Connection c = db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1,phoneNum);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return Optional.ofNullable(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public List<Customer> getCustomer() throws SQLException {
        List<Customer> customers = new ArrayList<>();

        String sql = "SELECT * FROM customer";

        try (Connection con = db.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){
            while (rs.next()) {
                String name = rs.getString("name");
                String phoneNr = rs.getString("PhoneNUM");

                Customer newCustomer = new Customer(name,phoneNr);
                customers.add(newCustomer);
            }
            return customers;
        } catch (SQLException e) {
            throw new SQLException("Kunne ikke indlæse kunder fra DB");
        }

    }
}
