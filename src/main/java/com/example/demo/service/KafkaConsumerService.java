package com.example.demo.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.demo.model.Customer;
import com.example.demo.repository.CustomerRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaConsumerService {
    @Autowired
    private CustomerRepository customerRepository;

    @KafkaListener(topics = "my-topic", groupId = "my-group")
    public void listen(ConsumerRecord<String, String> record) {
        String customerString = record.value();
        // Customer customer = convertStringToCustomer(customerString);
        saveCustomerToRepository(customerString);
    }

    private Customer convertStringToCustomer(String customerString) {
        String[] parts = customerString.split(", ");
        return new Customer(Long.parseLong(parts[0]), parts[1], parts[2], parts[3]);
    }

    private void saveCustomerToRepository(String customer) {
        log.info(customer);
    }
}
