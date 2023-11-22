package com.example.demo.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.example.demo.model.CustomerES;

public interface CustomerElasticRepository extends ElasticsearchRepository<CustomerES, String> {

}
