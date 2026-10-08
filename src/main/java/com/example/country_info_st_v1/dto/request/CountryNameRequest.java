package com.example.country_info_st_v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CountryNameRequest {
    @NotBlank(message = "Country name is required")
    private String name;
}
