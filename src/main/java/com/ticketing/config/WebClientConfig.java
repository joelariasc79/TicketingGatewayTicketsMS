package com.ticketing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${notification.service.base-url}")
    private String notificationServiceBaseUrl;

    @Bean // This method creates and exposes a WebClient bean to the Spring context
    public WebClient notificationServiceWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(notificationServiceBaseUrl)
                .build();
    }
}