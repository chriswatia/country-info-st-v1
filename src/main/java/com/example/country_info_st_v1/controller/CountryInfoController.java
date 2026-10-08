package com.example.country_info_st_v1.controller;

import com.example.country_info_st_v1.dto.request.CountryInfoRequest;
import com.example.country_info_st_v1.dto.request.CountryRequest;
import com.example.country_info_st_v1.dto.response.CountryInfoResponse;
import com.example.country_info_st_v1.dto.response.GenericResponse;
import com.example.country_info_st_v1.service.CountryInfoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class CountryInfoController {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final CountryInfoService countryInfoService;

    public CountryInfoController(CountryInfoService countryInfoService) {
        this.countryInfoService = countryInfoService;
    }

    // Fetches Country Info from SOAP Service and save to Database
    @PostMapping("/getCountryInfo")
    public ResponseEntity<?> getCountryInfo(@Validated @RequestBody CountryInfoRequest countryInfoRequest) throws JsonProcessingException {
        log.info("=====INCOMING COUNTRY INFO REQUEST: {}", objectMapper.writeValueAsString(countryInfoRequest));

        CountryInfoResponse countryInfoResponse = countryInfoService.getCountryInfo(countryInfoRequest);
        return ResponseEntity.ok(countryInfoResponse);
    }

    // Fetch all country information
    @GetMapping("/countries")
    public ResponseEntity<GenericResponse> getAllCountries() {
        log.info("=====INCOMING FETCH ALL COUNTRY INFORMATION REQUEST=====");
        return ResponseEntity.ok(countryInfoService.getAllCountries());
    }

    // Fetch country information by ID
    @GetMapping("/countries/{id}")
    public ResponseEntity<GenericResponse> getCountryById(@PathVariable Integer id) {
        log.info("=====INCOMING FETCH COUNTRY INFORMATION BY ID REQUEST: {}", id);
        return ResponseEntity.ok(countryInfoService.getCountryById(id));
    }

    // Update country information
    @PutMapping("/countries")
    public ResponseEntity<GenericResponse> updateCountry(@Validated @RequestBody CountryRequest countryRequest) throws JsonProcessingException {
        log.info("=====INCOMING UPDATE COUNTRY INFORMATION REQUEST: {}", objectMapper.writeValueAsString(countryRequest));
        return ResponseEntity.ok(countryInfoService.updateCountry(countryRequest));
    }

    // Delete country information
    @DeleteMapping("/countries/{id}")
    public ResponseEntity<GenericResponse> deleteCountry(@PathVariable Integer id) {
        log.info("=====INCOMING DELETE COUNTRY INFORMATION REQUEST: {}", id);
        GenericResponse response = countryInfoService.deleteCountry(id);
        return ResponseEntity.ok(response);
    }

}
