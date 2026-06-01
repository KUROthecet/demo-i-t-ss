package com.aims.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.aims.repository.HistoryLogRepository;
import com.aims.repository.MediaRepository;
import com.aims.repository.OrderRepository;

import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaypalService {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.api.url}")
    private String baseUrl;

    private final RestClient restClient;

    public PaypalService() {
        // RestClient is the modern Spring Boot 3.2+ way to make HTTP calls
        this.restClient = RestClient.create();
    }

    /**
     * 1. Exchange credentials for a temporary access token
     */
    private String getAccessToken() {
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        Map<String, String> response = restClient.post()
                .uri(baseUrl + "/v1/oauth2/token")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=client_credentials")
                .retrieve()
                .body(Map.class);

        return response.get("access_token");
    }

    /**
     * 2. Tell PayPal the cart total and get an Order ID back
     */
    public String createOrder(Double cartTotal) {
        String accessToken = getAccessToken();

        // In a real app, map this to a proper DTO class instead of using Maps
        Map<String, Object> payload = Map.of(
                "intent", "CAPTURE",
                "purchase_units", new Object[]{
                        Map.of(
                                "amount", Map.of(
                                        "currency_code", "VND",
                                        "value", String.format("%.2f", cartTotal)
                                )
                        )
                }
        );

        Map<String, Object> response = restClient.post()
        		.uri(baseUrl + "/v2/checkout/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(Map.class);

        log.info("Successfully created PayPal Order ID: {}", response.get("id"));
        return (String) response.get("id");
    }
}
