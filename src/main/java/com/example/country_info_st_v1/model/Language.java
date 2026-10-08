package com.example.country_info_st_v1.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "languages")
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "language_seq")
    @SequenceGenerator(name = "language_seq", sequenceName = "language_seq", allocationSize = 1)
    private Integer id;
    private String isoCode;
    private String name;
    private Integer countryId;
}
