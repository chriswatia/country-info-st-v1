package com.example.country_info_st_v1.repository;

import com.example.country_info_st_v1.model.CountryInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryInfoRepository  extends JpaRepository<CountryInfo, Integer> {
    CountryInfo findByIsoCode(String isoCode);
}
