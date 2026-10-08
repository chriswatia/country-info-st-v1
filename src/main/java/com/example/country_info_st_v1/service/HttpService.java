package com.example.country_info_st_v1.service;

import lombok.extern.slf4j.Slf4j;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@Service
@Slf4j
public class HttpService {
    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker = CircuitBreaker.of("country-info-provider",
            CircuitBreakerConfig.custom()
                    .failureRateThreshold(50)
                    .slidingWindowSize(10)
                    .minimumNumberOfCalls(5)
                    .waitDurationInOpenState(Duration.ofSeconds(30))
                    .build());

    public HttpService(WebClient webClient) {
        this.webClient = webClient;
    }

    public HashMap<String, String> HttpPOST(String xmlRequest, String url) {
        log.info("SOAP provider request started: {}", url);
        HashMap<String, String> responsePayload = new HashMap<String, String>();
        Consumer<HttpHeaders> customHeaders =
                httpHeaders -> httpHeaders.setContentType(MediaType.TEXT_XML);

        try {
            ResponseEntity<String> clientResponse = circuitBreaker.executeSupplier(() -> webClient
                    .post()
                    .uri(url)
                    .headers(customHeaders)
                    .body(BodyInserters.fromValue(xmlRequest))
                    .exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            return response.toEntity(String.class);
                        }
                        return response.createException().flatMap(Mono::error);
                    })
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(250))
                            .maxBackoff(Duration.ofSeconds(1))
                            .filter(HttpService::isRetryable))
                    .block(Duration.ofSeconds(12)));

            assert clientResponse != null;
            if (clientResponse.getStatusCode().is2xxSuccessful()) {
                responsePayload.put("RESPONSE_CODE", "200");
            } else {
                responsePayload.put("RESPONSE_CODE", "503");
            }
            responsePayload.put("RESPONSE_BODY", clientResponse.getBody());
            log.info("SOAP provider request completed with status {}", responsePayload.get("RESPONSE_CODE"));
            return responsePayload;
        } catch (CallNotPermittedException e) {
            log.warn("SOAP provider circuit is open");
        } catch (Exception e) {
            log.warn("SOAP provider request failed ({})", e.getClass().getSimpleName());
        }
        responsePayload.put("RESPONSE_CODE", "503");
        responsePayload.put("RESPONSE_BODY", "Country information provider is temporarily unavailable");
        return responsePayload;
    }

    private static boolean isRetryable(Throwable error) {
        if (error instanceof WebClientResponseException responseException) {
            return responseException.getStatusCode().is5xxServerError()
                    || responseException.getStatusCode().value() == 429;
        }
        return error instanceof WebClientRequestException || error instanceof TimeoutException;
    }
}
