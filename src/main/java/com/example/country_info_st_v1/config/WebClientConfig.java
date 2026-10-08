package com.example.country_info_st_v1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient() throws SSLException {
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        // Create a custom HttpClient with SSL context and timeout settings
        HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
                .secure(t -> t.sslContext(sslContext));
        httpClient.doOnConnected(
                conn -> conn.addHandlerLast((ChannelHandler) new ReadTimeoutHandler(60000, TimeUnit.MILLISECONDS)));
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
