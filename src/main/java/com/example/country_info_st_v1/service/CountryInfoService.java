package com.example.country_info_st_v1.service;

import com.example.country_info_st_v1.dto.request.CountryNameRequest;
import com.example.country_info_st_v1.dto.response.CountryIsoCodeResponse;
import com.example.country_info_st_v1.dto.response.CountryNameResponse;
import com.example.country_info_st_v1.utils.ApplicationProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import freemarker.template.Configuration;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class CountryInfoService {
    private final ApplicationProperties applicationProperties;
    private final Configuration freemarker;
    private final HttpService httpService;
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public CountryInfoService(ApplicationProperties applicationProperties, Configuration freemarker, HttpService httpService) {
        this.applicationProperties = applicationProperties;
        this.freemarker = freemarker;
        this.httpService = httpService;
    }


    public CountryNameResponse getCountryName(CountryNameRequest countryNameRequest) throws JsonProcessingException {
        // Convert the received country name to sentence case
        String countryName = countryNameRequest.getName().trim();
        countryName = countryName.substring(0, 1).toUpperCase(Locale.ROOT)
                + countryName.substring(1).toLowerCase(Locale.ROOT);

        // Format the request payload using FreeMarker template
        String xmlRequest = formatCountryNameRequest(countryName);

        log.info("=====REQUEST: {}", xmlRequest);

        // Call the external SOAP service to get the ISO Code by country name
        HashMap<String, String> httpResponse = httpService.HttpPOST(xmlRequest, applicationProperties.getCountryInfoUrl());
        if (!httpResponse.get("RESPONSE_CODE").equals("200")) {
            throw new IllegalStateException("Country IsoCode request failed: "
                    + httpResponse.get("RESPONSE_BODY"));
        }

        String responsePayload = httpResponse.get("RESPONSE_BODY");
        if (responsePayload == null) {
            throw new IllegalStateException("Country IsoCode service returned an empty response");
        }

        CountryIsoCodeResponse countryIsoCodeResponse = parseCountryIsoCodeResponse(responsePayload);
        log.info("Country IsoCode response: {}", objectMapper.writeValueAsString(countryIsoCodeResponse));

        return null;
    }

    private CountryIsoCodeResponse parseCountryIsoCodeResponse(String responsePayload) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            Document document = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(responsePayload)));
            NodeList results = document.getElementsByTagNameNS("*", "CountryISOCodeResult");
            if (results.getLength() == 0) {
                throw new IllegalStateException("CountryISOCodeResult is missing from the SOAP response");
            }

            CountryIsoCodeResponse countryIsoCodeResponse = new CountryIsoCodeResponse();
            String countryIsoCode = results.item(0).getTextContent().trim();
            if (!countryIsoCode.isEmpty()) {
                countryIsoCodeResponse.setCountryIsoCode(countryIsoCode);
            }
            return countryIsoCodeResponse;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IllegalStateException("Unable to parse country ISO code SOAP response", e);
        }
    }

    private String formatCountryNameRequest(String countryName) {
        Map<String, Object> templateData = new HashMap<>();
        freemarker.setClassForTemplateLoading(CountryInfoService.class, "/templates");
        try {
            Template template = freemarker.getTemplate("country-iso-code.ftl");
            templateData.put("countryName", countryName);
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, templateData);
        }catch (Exception e) {
            log.error("------EXCEPTION WHILE PREPARING COUNTRY ISO CODE REQUEST --------:\n{}", e.getMessage());
            return null;
        }
    }

}
