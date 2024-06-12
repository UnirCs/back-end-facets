package com.unir.facets.service;

import com.unir.facets.model.response.EmployeesQueryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.unir.facets.data.DataAccessRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacetsService {

	private final DataAccessRepository repository;

	public EmployeesQueryResponse getProducts(
			List<String> genderValues,
			List<String> designationValues,
			List<String> civilStatusValues,
			List<String> ageValues,
			List<String> salaryValues,
			String name,
			String address,
			String page) {

		return repository.findProducts(
				genderValues,
				designationValues,
				civilStatusValues,
				ageValues,
				salaryValues,
				name,
				address,
				page);
	}
}
