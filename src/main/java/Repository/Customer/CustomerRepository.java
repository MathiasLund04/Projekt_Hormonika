package Repository.Customer;

import Model.Customer;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    void createCustomerIfNotExist(String name, String phoneNr) throws SQLException;
    Optional<String> getCustomerNameByPhoneNr(String phoneNr);
    List<Customer> getCustomers() throws SQLException;
}
