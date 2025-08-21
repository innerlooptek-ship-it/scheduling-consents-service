// src/main/java/com/cvshealth/digital/microservice/consents/config/WebClientConfig.java
package com.cvshealth.digital.microservice.consents.config;

import io.netty.channel.ChannelOption;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@AllArgsConstructor
public class WebClientConfig {
    ApiConfigs apiConfig;
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean(name = "mcitGetPatientConsents")
    public WebClient webClientMCITGetPatientConsents() {
        ApiConfigs.ApiConfig config = apiConfig.getConfigs().get("consents").get("getPatientConsents");
        return WebClient.builder()
                .baseUrl(config.getBaseUrl() + config.getUri())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeaders(headers -> headers.setBasicAuth(config.getUserName(), config.getPassword()))
                .clientConnector(addTimeout(config.getReadTimeout(), config.getConnectionTimeout()))
                .build();
    }

    public ReactorClientHttpConnector addTimeout(int readTimeout, int connectionTimeout) {
        return new ReactorClientHttpConnector(getHttpClient(readTimeout, connectionTimeout));
    }

    private HttpClient getHttpClient(int readTimeout, int connectionTimeout) {
        return HttpClient.create()
                .responseTimeout(Duration.ofMillis(readTimeout))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout);
    }
}