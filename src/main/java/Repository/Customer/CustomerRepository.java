package Repository.Customer;

import Model.Customer;

import java.sql.SQLException;
import java.util.List;

public interface CustomerRepository {
    void createCustomerIfNotExist(String name, String phoneNr) throws SQLException;
    List<Customer> getCustomer() throws SQLException;
}

