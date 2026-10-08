package com.example.country_info_st_v1.repository;

import com.example.country_info_st_v1.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageRepository extends JpaRepository<Language, Integer> {
}
