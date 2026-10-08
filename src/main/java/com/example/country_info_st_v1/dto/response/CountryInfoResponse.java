package com.example.country_info_st_v1.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CountryInfoResponse {
    private String isoCode;
    private String name;
    private String capitalCity;
    private String phoneCode;
    private String continentCode;
    private String currencyISOCode;
    private String countryFlag;
    private List<Language> languages = new ArrayList<>();
}
