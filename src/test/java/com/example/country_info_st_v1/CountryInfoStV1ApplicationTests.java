package com.example.country_info_st_v1;

import com.example.country_info_st_v1.dto.response.GenericResponse;
import com.example.country_info_st_v1.service.CountryInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:country_info_test;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class CountryInfoStV1ApplicationTests {

	@Autowired
	private CountryInfoService countryInfoService;

	@Test
	void contextLoads() {
	}

	@Test
	void testGetCountriesPaginatedCacheSpelEvaluation() {
		GenericResponse response = countryInfoService.getCountriesPaginated(PageRequest.of(0, 10));
		assertNotNull(response);
	}

}
