package com.example.country_info_st_v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CountryInfoRequest {
    @NotBlank(message = "Country name is required")
    private String name;
}
