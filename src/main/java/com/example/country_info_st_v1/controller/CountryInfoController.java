package com.example.country_info_st_v1.controller;

import com.example.country_info_st_v1.dto.request.CountryInfoRequest;
import com.example.country_info_st_v1.dto.request.CountryRequest;
import com.example.country_info_st_v1.dto.response.CountryInfoResponse;
import com.example.country_info_st_v1.dto.response.GenericResponse;
import com.example.country_info_st_v1.service.CountryInfoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    // Fetch all country information with optional pagination
    @Operation(
            summary = "Fetch all countries",
            description = "Fetch all country information with optional page and size parameters",
            parameters = {
                    @Parameter(name = "page", in = ParameterIn.QUERY, description = "Zero-based page index", schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", in = ParameterIn.QUERY, description = "The size of the page to be returned", schema = @Schema(type = "integer", defaultValue = "10"))
            }
    )
    @GetMapping("/countries")
    public ResponseEntity<GenericResponse> getAllCountries(
            @Parameter(hidden = true) @PageableDefault(page = 0, size = 10) Pageable pageable) {
        log.info("=====INCOMING FETCH COUNTRY INFORMATION REQUEST: page={}, size={}=====",
                pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(countryInfoService.getCountriesPaginated(pageable));
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
