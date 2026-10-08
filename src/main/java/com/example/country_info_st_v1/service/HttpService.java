package com.example.country_info_st_v1.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.function.Consumer;

@Service
@Slf4j
public class HttpService {
    private final WebClient webClient;

    public HttpService(WebClient webClient) {
        this.webClient = webClient;
    }

    public HashMap<String, String> HttpPOST(String xmlRequest, String url) {

        String HttpMessageCode="";
        String HttpMessage;
        HashMap<String, String> responsePayload = new HashMap<String, String>();
        Consumer<HttpHeaders> customHeaders =
                httpHeaders -> httpHeaders.setContentType(MediaType.TEXT_XML);

        try {
            ResponseEntity<String> clientResponse = webClient
                    .post()
                    .uri(url)
                    .headers(customHeaders)
                    .body(BodyInserters.fromValue(xmlRequest))
                    .exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            return response.toEntity(String.class);
                        } else {
                            return response.createException()
                                    .flatMap(exception
                                            -> Mono.error(new RuntimeException("HTTP request failed with status code: " + response.statusCode() + ", message: " + exception.getMessage())));
                        }
                    })
                    .block();

            assert clientResponse != null;
            if (clientResponse.getStatusCode().is2xxSuccessful()) {
                HttpMessageCode = "200";
            } else {
                HttpMessageCode = "500";
            }
            HttpMessage = clientResponse.getBody();
        } catch (Exception  e) {
            log.error("HTTP Error: [{}]", e.getMessage());
            HttpMessage = e.getMessage();

        }
        responsePayload.put("RESPONSE_CODE", HttpMessageCode);
        responsePayload.put("RESPONSE_BODY", HttpMessage);
        log.info("HTTP STATUS CODE :: {}", HttpMessageCode);
        log.info("HTTP MESSAGE :: {}", HttpMessage);
        log.info("----------- HTTP Response End  ---------------------------");
        return responsePayload;
    }
}
