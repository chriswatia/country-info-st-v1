package com.example.country_info_st_v1.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class ApplicationProperties {
    @Value("${settings.country-info-url}")
    private String countryInfoUrl;
}
