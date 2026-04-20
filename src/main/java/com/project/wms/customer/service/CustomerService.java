package com.project.wms.customer.service;

import com.project.wms.customer.entity.Customer;
import com.project.wms.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer createCustomer(Customer customer) {
        if (customerRepository.findByCode(customer.getCode()).isPresent()) {
            throw new RuntimeException("Mã đối tác đã tồn tại");
        }
        return customerRepository.save(customer);
    }
}
