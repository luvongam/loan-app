package com.jamlech.loanapp.services;

import com.jamlech.loanapp.entities.Customer;
import com.jamlech.loanapp.repositories.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    public Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(
                        () -> new RuntimeException("Customer not found")
                );
    }
    public Customer updateCustomer(Long id, Customer customerDetails) {
        Customer customer =getCustomer(id);
        customer.setName(customerDetails.getName());
        customer.setEmail(customerDetails.getEmail());
        customer.setPhone(customerDetails.getPhone());
        customer.setUpdatedAt(LocalDateTime.now());
        return customerRepository.save(customer);

    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}
