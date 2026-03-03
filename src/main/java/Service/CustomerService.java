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
    private List<Customer> customers;


    public CustomerService(CustomerRepository cRepo, DBConfig db) {
        this.db = db;
        this.cRepo = cRepo;
        try {
            customers = new ArrayList<>(cRepo.getCustomers());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public Customer findCustomer(String name, String phoneNr) {
        for (Customer c : customers) {
            if (c.getName().equals(name) && c.getPhoneNum().equals(phoneNr)) {
                return c;
            }
        }
        return null;
    }

    public Customer createCustomerIfNotExist(String name, String phoneNr) {
        Customer existingCustomer = findCustomer(name, phoneNr);
        if (existingCustomer != null) {
            return existingCustomer;
        }

        Customer newCustomer = new Customer(name, phoneNr);
        customers.add(newCustomer);
        try {
            cRepo.createCustomerIfNotExist(name, phoneNr);
        } catch (SQLException e){
            throw new DataAccessException("Kunne ikke oprette kunde i database");
        }
        return newCustomer;
    }

    public List<Customer> getCustomers() {
        try {
            cRepo.getCustomers();
        } catch (SQLException e){
            throw new DataAccessException("Kunne ikke indlæse kunder fra DB");
        }
        return new ArrayList<>(customers);
    }
}
