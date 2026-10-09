package com.example.country_info_st_v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryIsoCodeResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String countryIsoCode;
}
