package com.example.country_info_st_v1.repository;

import com.example.country_info_st_v1.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LanguageRepository extends JpaRepository<Language, Integer> {
    List<Language> findByCountryId(Integer countryId);
}
