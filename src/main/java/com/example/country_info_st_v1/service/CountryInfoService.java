package com.example.country_info_st_v1.service;

import com.example.country_info_st_v1.dto.request.CountryInfoRequest;
import com.example.country_info_st_v1.dto.response.CountryIsoCodeResponse;
import com.example.country_info_st_v1.dto.response.CountryInfoResponse;
import com.example.country_info_st_v1.dto.response.Language;
import com.example.country_info_st_v1.utils.ApplicationProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import freemarker.template.Configuration;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
    String xmlRequest = null;
    HashMap<String, String> httpResponse = null;
    String responsePayload = null;
    Map<String, Object> templateData = null;
    Template template = null;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    // Constructor injection for dependencies
    public CountryInfoService(ApplicationProperties applicationProperties, Configuration freemarker, HttpService httpService) {
        this.applicationProperties = applicationProperties;
        this.freemarker = freemarker;
        this.httpService = httpService;
    }


    public CountryInfoResponse getCountryInfo(CountryInfoRequest countryInfoRequest) throws JsonProcessingException {
        CountryInfoResponse countryInfoResponse = new CountryInfoResponse();
        // Convert the received country name to sentence case
        String countryName = countryInfoRequest.getName().trim();
        countryName = countryName.substring(0, 1).toUpperCase(Locale.ROOT)
                + countryName.substring(1).toLowerCase(Locale.ROOT);

        // Format the request payload using FreeMarker template
        xmlRequest = formatCountryNameRequest(countryName);

        // Call the external SOAP service to get the ISO Code by country name
        httpResponse = httpService.HttpPOST(xmlRequest, applicationProperties.getCountryInfoUrl());
        if (!httpResponse.get("RESPONSE_CODE").equals("200")) {
            throw new IllegalStateException("Country IsoCode request failed: "
                    + httpResponse.get("RESPONSE_BODY"));
        }

        responsePayload = httpResponse.get("RESPONSE_BODY");
        if (responsePayload == null) {
            throw new IllegalStateException("Country IsoCode service returned an empty response");
        }

        CountryIsoCodeResponse countryIsoCodeResponse = parseCountryIsoCodeResponse(responsePayload);
        log.info("Country IsoCode response: {}", objectMapper.writeValueAsString(countryIsoCodeResponse));

        // Use the extracted ISO code to call another SOAP endpoint that takes
        //sCountryISOCode as a request body parameter to fetch FullCountryInfo
        if(countryIsoCodeResponse.getCountryIsoCode() != null) {
            xmlRequest = formatFullCountryInfoRequest(countryIsoCodeResponse.getCountryIsoCode());

            httpResponse = httpService.HttpPOST(xmlRequest, applicationProperties.getCountryInfoUrl());

            if (!httpResponse.get("RESPONSE_CODE").equals("200")) {
                throw new IllegalStateException("Full Country Info request failed: "
                        + httpResponse.get("RESPONSE_BODY"));
            }

            responsePayload = httpResponse.get("RESPONSE_BODY");
            if (responsePayload == null) {
                throw new IllegalStateException("Full Country Info service returned an empty response");
            }

            // Parse the FullCountryInfo response and return the result
            countryInfoResponse = parseFullCountryInfoResponse(responsePayload);
        }

        return countryInfoResponse;
    }

    private String formatCountryNameRequest(String countryName) {
        templateData = new HashMap<>();
        freemarker.setClassForTemplateLoading(CountryInfoService.class, "/templates");
        try {
            template = freemarker.getTemplate("country-iso-code.ftl");
            templateData.put("countryName", countryName);
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, templateData);
        }catch (Exception e) {
            log.error("------EXCEPTION WHILE PREPARING COUNTRY ISO CODE REQUEST --------:\n{}", e.getMessage());
            return null;
        }
    }

    private String formatFullCountryInfoRequest(String countryIsoCode) {
        log.info("Country IsoCode request: {}", countryIsoCode);
        templateData = new HashMap<>();
        freemarker.setClassForTemplateLoading(CountryInfoService.class, "/templates");
        try {
            template = freemarker.getTemplate("country-info.ftl");
            templateData.put("countryISOCode", countryIsoCode);
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, templateData);
        } catch (Exception e) {
            log.error("------EXCEPTION WHILE PREPARING FULL COUNTRY INFO REQUEST --------:\n{}", e.getMessage());
            return null;
        }
    }

    private CountryIsoCodeResponse parseCountryIsoCodeResponse(String responsePayload) {
        try {
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

    private CountryInfoResponse parseFullCountryInfoResponse(String responsePayload) {
        try {
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            Document document = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(responsePayload)));
            NodeList results = document.getElementsByTagNameNS("*", "FullCountryInfoResult");
            if (results.getLength() == 0) {
                throw new IllegalStateException("FullCountryInfoResult is missing from the SOAP response");
            }

            CountryInfoResponse countryInfoResponse = new CountryInfoResponse();
            // Extract and set the required fields from the SOAP response
            countryInfoResponse.setIsoCode(getNodeValue(results.item(0), "sISOCode"));
            countryInfoResponse.setName(getNodeValue(results.item(0), "sName"));
            countryInfoResponse.setCapitalCity(getNodeValue(results.item(0), "sCapitalCity"));
            countryInfoResponse.setPhoneCode(getNodeValue(results.item(0), "sPhoneCode"));
            countryInfoResponse.setContinentCode(getNodeValue(results.item(0), "sContinentCode"));
            countryInfoResponse.setCurrencyISOCode(getNodeValue(results.item(0), "sCurrencyISOCode"));
            countryInfoResponse.setCountryFlag(getNodeValue(results.item(0), "sCountryFlag"));

            NodeList languageContainers = ((Element) results.item(0))
                    .getElementsByTagNameNS("*", "Languages");
            if (languageContainers.getLength() > 0) {
                NodeList languages = ((Element) languageContainers.item(0))
                        .getElementsByTagNameNS("*", "tLanguage");
                for (int i = 0; i < languages.getLength(); i++) {
                    Element languageElement = (Element) languages.item(i);
                    Language language = new Language();
                    language.setIsoCode(getNodeValue(languageElement, "sISOCode"));
                    language.setName(getNodeValue(languageElement, "sName"));
                    countryInfoResponse.getLanguages().add(language);
                }
            }
            return countryInfoResponse;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IllegalStateException("Unable to parse full country info SOAP response", e);
        }
    }

    private String getNodeValue(Node parent, String localName) {
        NodeList nodes = ((Element) parent).getElementsByTagNameNS("*", localName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return nodes.item(0).getTextContent().trim();
    }
}
