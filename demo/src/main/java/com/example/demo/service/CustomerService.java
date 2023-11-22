package com.example.demo.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.model.Customer;
import com.example.demo.model.CustomerES;
import com.example.demo.repository.CustomerElasticRepository;
import com.example.demo.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
// @RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerElasticRepository customerElasticRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private KafkaProducerService kafkaProducerService;

    private static final String REDIS_KEY_PREFIX = "customer:";

    public List<Customer> getAllCustomers() {
        List<Customer> customers = getDataFromRedis("all");
        if (customers == null) {
            customers = customerRepository.findAll();
            saveDataToRedis("all", customers);
        }
        return customers;
    }

    public Iterable<CustomerES> findAll() {
        Iterable<CustomerES> customerES = customerElasticRepository.findAll();
        sendCustomerDataToKafka(customerES);
        return customerElasticRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        Customer customer = getDataFromRedisId(String.valueOf(id));
        if (customer == null) {
            customer = customerRepository.findById(id).orElse(null);
            if (customer != null) {
                saveDataToRedis(String.valueOf(id), customer);
            }
        }
        return customer;
    }

    public Customer createCustomer(Customer customer) {
        Customer customer2 = customerRepository.save(customer);
        saveDataToElastic(customer2);
        return customer2;
    }

    public Customer updateCustomer(Long id, Customer customer) {
        if (customerRepository.existsById(id)) {
            customer.setCustomerId(id);
            return customerRepository.save(customer);
        }
        return null;
    }

    public boolean deleteCustomer(Long id) {
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
            customerElasticRepository.deleteById(String.valueOf(id));
            return true;
        }
        return false;
    }

    private List<Customer> getDataFromRedis(String key) {
        return (List<Customer>) redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + key);
    }

    private Customer getDataFromRedisId(String key) {
        return (Customer) redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + key);
    }

    private void saveDataToRedis(String key, Object data) {
        String fullKey = REDIS_KEY_PREFIX + key;
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + key, data);
        redisTemplate.expire(fullKey, 1, TimeUnit.HOURS);
    }

    private void saveDataToElastic(Customer customer) {
        customerElasticRepository.save(mappingCustomerES(customer));
    }

    private CustomerES mappingCustomerES(Customer customer) {
        return CustomerES.builder()
                .customerId(String.valueOf(customer.getCustomerId()))
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .build();
    }

    private void sendCustomerDataToKafka(Iterable<CustomerES> customers) {
        for (CustomerES customer : customers) {
            kafkaProducerService.sendMessage("my-topic", convertCustomerToString(customer));
        }
    }

    private String convertCustomerToString(CustomerES customer) {
        return customer.toString();
    }
}
