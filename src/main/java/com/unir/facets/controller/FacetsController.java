package com.unir.facets.controller;

import com.unir.facets.model.response.EmployeesQueryResponse;
import com.unir.facets.service.FacetsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class FacetsController {

    private final FacetsService service;

    @GetMapping("/facets")
    public ResponseEntity<EmployeesQueryResponse> getProducts(
            @RequestParam(required = false) List<String> genderValues,
            @RequestParam(required = false) List<String> designationValues,
            @RequestParam(required = false) List<String> civilStatusValues,
            @RequestParam(required = false) List<String> ageValues,
            @RequestParam(required = false) List<String> salaryValues,
            @RequestParam(required = false, defaultValue = "0") String page) {

        EmployeesQueryResponse response = service.getProducts(
				genderValues,
				designationValues,
				civilStatusValues,
				ageValues,
				salaryValues,
                page);
        return ResponseEntity.ok(response);
    }
}
