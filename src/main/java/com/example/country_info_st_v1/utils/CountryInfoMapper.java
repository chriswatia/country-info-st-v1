package com.example.country_info_st_v1.utils;

import com.example.country_info_st_v1.dto.response.CountryInfoResponse;
import com.example.country_info_st_v1.dto.response.LanguageResponse;
import com.example.country_info_st_v1.model.CountryInfo;
import com.example.country_info_st_v1.model.Language;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CountryInfoMapper {
    CountryInfoResponse toResponse(CountryInfo countryInfo);

    List<CountryInfoResponse> toResponseList(List<CountryInfo> countryInfos);

    LanguageResponse toResponse(Language language);
}
