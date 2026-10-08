package com.example.country_info_st_v1.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "countries")
public class CountryInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "country_seq")
    @SequenceGenerator(name = "country_seq", sequenceName = "country_seq", allocationSize = 1)
    private Integer id;
    @Column(unique = true)
    private String isoCode;
    private String name;
    private String capitalCity;
    private String phoneCode;
    private String continentCode;
    private String currencyISOCode;
    private String countryFlag;

    // OneToMany relationship with Language entity
    @OneToMany(mappedBy = "countryId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Language> languages = new java.util.ArrayList<>();
}
