package Service;

import Exceptions.DataAccessException;
import Model.Customer;
import DAL.DBConfig;
import Repository.Customer.CustomerRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerService {
    private DBConfig db;
    private final CustomerRepository cRepo;
    private List<Customer> customer;


    public CustomerService(CustomerRepository cRepo, DBConfig db) {
        this.db = db;
        this.cRepo = cRepo;
        try {
            customer = new ArrayList<>(cRepo.getCustomer());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Customer findCustomer(String name, String phoneNum) {
        for (Customer c : customer) {
            if (c.getName().equals(name) && c.getPhoneNum().equals(phoneNum)) {
                return c;
            }
        }
        return null;
    }

    public Customer createCustomerIfNotExist(String name, String phoneNum) {
        Customer existingCustomer = findCustomer(name, phoneNum);
        if (existingCustomer != null) {
            return existingCustomer;
        }

        Customer newCustomer = new Customer(name, phoneNum);
        customer.add(newCustomer);
        try {
            cRepo.createCustomerIfNotExist(name, phoneNum);
        } catch (SQLException e){
            throw new DataAccessException("Kunne ikke oprette kunde i database");
        }
        return newCustomer;
    }

    public List<Customer> getCustomer() {
        return new ArrayList<>(customer);
    }
}
