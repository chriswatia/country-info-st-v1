package com.example.country_info_st_v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class CountryRequest {
    @NotBlank(message = "Country ISO code is required")
    private String isoCode;
    @NotBlank(message = "Country name is required")
    private String name;
    private String capitalCity;
    private String phoneCode;
    private String continentCode;
    private String currencyISOCode;
    private String countryFlag;
    private List<LanguageRequest> languages;

    @Data
    public static class LanguageRequest {
        private String isoCode;
        private String name;
    }
}
