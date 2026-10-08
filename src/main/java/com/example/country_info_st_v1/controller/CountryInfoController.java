package com.example.country_info_st_v1.controller;

import com.example.country_info_st_v1.dto.request.CountryNameRequest;
import com.example.country_info_st_v1.dto.response.CountryNameResponse;
import com.example.country_info_st_v1.service.CountryInfoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class CountryInfoController {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final CountryInfoService countryInfoService;

    public CountryInfoController(CountryInfoService countryInfoService) {
        this.countryInfoService = countryInfoService;
    }

    @PostMapping("/getCountryName")
    public ResponseEntity<?> getCountryName(@Validated @RequestBody CountryNameRequest countryNameRequest) throws JsonProcessingException {
        log.info("=====INCOMING COUNTRY NAME REQUEST: {}", objectMapper.writeValueAsString(countryNameRequest));

        CountryNameResponse countryNameResponse = countryInfoService.getCountryName(countryNameRequest);
        return ResponseEntity.ok(countryNameResponse);
    }
}
