package com.example.country_info_st_v1;

import com.example.country_info_st_v1.controller.CountryInfoController;
import com.example.country_info_st_v1.dto.request.CountryInfoRequest;
import com.example.country_info_st_v1.dto.request.CountryRequest;
import com.example.country_info_st_v1.dto.response.CountryInfoResponse;
import com.example.country_info_st_v1.dto.response.GenericResponse;
import com.example.country_info_st_v1.dto.response.PageResponse;
import com.example.country_info_st_v1.exception.GlobalExceptionHandler;
import com.example.country_info_st_v1.service.CountryInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CountryInfoControllerTests {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CountryInfoService countryInfoService;

    @InjectMocks
    private CountryInfoController countryInfoController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(countryInfoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testGetCountryInfoSuccess() throws Exception {
        CountryInfoRequest request = new CountryInfoRequest();
        request.setName("Kenya");

        CountryInfoResponse mockResponse = CountryInfoResponse.builder()
                .isoCode("KE")
                .name("Kenya")
                .capitalCity("Nairobi")
                .build();

        when(countryInfoService.getCountryInfo(any(CountryInfoRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/getCountryInfo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isoCode").value("KE"))
                .andExpect(jsonPath("$.name").value("Kenya"))
                .andExpect(jsonPath("$.capitalCity").value("Nairobi"));
    }

    @Test
    void testGetAllCountries() throws Exception {
        CountryInfoResponse country = CountryInfoResponse.builder()
                .isoCode("KE")
                .name("Kenya")
                .build();

        PageResponse<CountryInfoResponse> pageResponse = PageResponse.<CountryInfoResponse>builder()
                .content(List.of(country))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isFirst(true)
                .isLast(true)
                .build();

        GenericResponse genericResponse = GenericResponse.builder()
                .statusCode("00")
                .message("Countries fetched successfully.")
                .data(pageResponse)
                .build();

        when(countryInfoService.getCountriesPaginated(any(Pageable.class))).thenReturn(genericResponse);

        mockMvc.perform(get("/api/v1/countries")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("00"))
                .andExpect(jsonPath("$.message").value("Countries fetched successfully."))
                .andExpect(jsonPath("$.data.content[0].name").value("Kenya"));
    }

    @Test
    void testGetCountryByIdSuccess() throws Exception {
        CountryInfoResponse country = CountryInfoResponse.builder()
                .isoCode("KE")
                .name("Kenya")
                .build();

        GenericResponse genericResponse = GenericResponse.builder()
                .statusCode("00")
                .message("Country fetched successfully.")
                .data(country)
                .build();

        when(countryInfoService.getCountryById(1)).thenReturn(genericResponse);

        mockMvc.perform(get("/api/v1/countries/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("00"))
                .andExpect(jsonPath("$.data.name").value("Kenya"));
    }

    @Test
    void testGetCountryByIdNotFound() throws Exception {
        when(countryInfoService.getCountryById(999))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Country not found with ID: 999"));

        mockMvc.perform(get("/api/v1/countries/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.message").value("Country not found with ID: 999"));
    }

    @Test
    void testUpdateCountrySuccess() throws Exception {
        CountryRequest request = new CountryRequest();
        request.setIsoCode("KE");
        request.setName("Kenya");

        GenericResponse genericResponse = GenericResponse.builder()
                .statusCode("00")
                .message("Country updated successfully.")
                .build();

        when(countryInfoService.updateCountry(any(CountryRequest.class))).thenReturn(genericResponse);

        mockMvc.perform(put("/api/v1/countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("00"))
                .andExpect(jsonPath("$.message").value("Country updated successfully."));
    }

    @Test
    void testDeleteCountrySuccess() throws Exception {
        GenericResponse genericResponse = GenericResponse.builder()
                .statusCode("00")
                .message("Country deleted successfully.")
                .build();

        when(countryInfoService.deleteCountry(eq(1))).thenReturn(genericResponse);

        mockMvc.perform(delete("/api/v1/countries/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("00"))
                .andExpect(jsonPath("$.message").value("Country deleted successfully."));
    }
}
