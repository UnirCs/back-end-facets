package com.unir.facets.data;

import java.util.List;

import com.unir.facets.data.model.Employee;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface EmployeeRepository extends ElasticsearchRepository<Employee, String> {
	
	List<Employee> findAll();
}
