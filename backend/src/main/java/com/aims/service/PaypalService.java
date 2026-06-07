package com.aims.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.aims.dto.request.PaypalRequestDto;
import com.aims.dto.response.PaypalResponseDto;
import com.aims.repository.PaymentTransactionRepository;

import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class PaypalService {

    private final PaymentTransactionRepository paymentTransactionRepository;

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.api.url}")
    private String baseUrl;

    private final RestClient restClient;

    public PaypalService(PaymentTransactionRepository paymentTransactionRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.restClient = RestClient.create();
    }

    @SuppressWarnings("unchecked")
    private String getAccessToken() {
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        Map<String, String> response = (Map<String, String>) restClient.post()
                .uri(baseUrl + "/v1/oauth2/token")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=client_credentials")
                .retrieve()
                .body(Map.class);
        return response.get("access_token");
    }

    public String placeOrder(int cartTotal) {
        String accessToken = getAccessToken();

        PaypalRequestDto payload = PaypalRequestDto.builder()
                .intent("CAPTURE")
                .purchaseUnits(List.of(
                        PaypalRequestDto.PurchaseUnit.builder()
                                .amount(PaypalRequestDto.Amount.builder()
                                        .currencyCode("USD")
                                        .value(String.format(Locale.US, "%.2f", (double) cartTotal))
                                        .build())
                                .build()
                ))
                .build();

        try {
            PaypalResponseDto response = restClient.post()
                    .uri(baseUrl + "/v2/checkout/orders")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(PaypalResponseDto.class);

            log.info("Created PayPal Order ID: {} with status: {}", response.getId(), response.getStatus());
            return response.getId();

        } catch (HttpClientErrorException e) {
            log.error("PayPal API error. Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to create PayPal order");
        }
    }

    public String captureOrder(String orderId) {
        String accessToken = getAccessToken();

        try {
            Map<String, Object> emptyPayload = Map.of();

            PaypalResponseDto response = restClient.post()
                    .uri(baseUrl + "/v2/checkout/orders/" + orderId + "/capture")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(emptyPayload)
                    .retrieve()
                    .body(PaypalResponseDto.class);

            if ("COMPLETED".equals(response.getStatus())) {
                log.info("Payment captured for Order: {}", orderId);
                String captureId = "";
                if (response.getPurchaseUnits() != null && !response.getPurchaseUnits().isEmpty() &&
                    response.getPurchaseUnits().get(0).getPayments() != null &&
                    response.getPurchaseUnits().get(0).getPayments().getCaptures() != null &&
                    !response.getPurchaseUnits().get(0).getPayments().getCaptures().isEmpty()) {
                    captureId = response.getPurchaseUnits().get(0).getPayments().getCaptures().get(0).getId();
                }
                return "COMPLETED:" + captureId;
            } else {
                log.warn("Order {} returned status: {}", orderId, response.getStatus());
                return response.getStatus();
            }

        } catch (HttpClientErrorException e) {
            log.error("Failed to capture order {}. Status: {}, Response: {}",
                    orderId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Payment capture failed. User may not have approved the transaction.");
        }
    }

    public boolean refundOrder(String captureId, int amount) {
        if (captureId == null || captureId.isEmpty()) {
            log.error("Cannot refund without a valid capture ID");
            return false;
        }
        
        String accessToken = getAccessToken();
        Map<String, Object> payload = Map.of(
            "amount", Map.of(
                "value", String.format(Locale.US, "%.2f", (double) amount),
                "currency_code", "USD"
            )
        );

        try {
            restClient.post()
                    .uri(baseUrl + "/v2/payments/captures/" + captureId + "/refund")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Refund successful for capture ID: {}", captureId);
            return true;
            
        } catch (HttpClientErrorException e) {
            log.error("Failed to refund capture {}. Status: {}, Response: {}",
                    captureId, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        }
    }
}
