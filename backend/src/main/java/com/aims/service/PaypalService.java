package com.aims.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.aims.dto.request.PaypalRequestDto;
import com.aims.dto.response.PaypalResponseDto;
import com.aims.entity.Order;
import com.aims.entity.PaymentTransaction;
import com.aims.entity.Transaction;
import com.aims.enums.PaymentMethod;
import com.aims.repository.PaymentTransactionRepository;

import java.math.BigDecimal;
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
        // RestClient is the modern Spring Boot 3.2+ way to make HTTP calls
    	this.paymentTransactionRepository = paymentTransactionRepository;
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
	public String placeOrder(int cartTotal) {
        String accessToken = getAccessToken();
        log.info("0000000000000000000000");
        // In a real app, map this to a proper DTO class instead of using Maps
        PaypalRequestDto payload = PaypalRequestDto.builder()
        	    .intent("CAPTURE")
        	    .purchaseUnits(List.of(
        	    		PaypalRequestDto.PurchaseUnit.builder()
        	            .amount(PaypalRequestDto.Amount.builder()
        	                .currencyCode("USD")
        	                .value(String.format(Locale.US,"%.2f", (double) cartTotal))
        	                .build())
        	            .build()
        	    ))
        	    .build();
        
        log.info("11111111111111");
        try {
            PaypalResponseDto response = restClient.post()
                    .uri(baseUrl + "/v2/checkout/orders")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload) 
                    .retrieve()
                    .body(PaypalResponseDto.class);
                    
            log.info("22222222222222");
            log.info("Successfully created PayPal Order ID: {} with status: {}", response.getId(), response.getStatus());
            
            return response.getId();
            
        } catch (HttpClientErrorException e) {
            // FIX 2: If PayPal rejects the request, print their exact error message!
            log.error("PayPal API rejected the request! Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to create PayPal order");
        }
    }
	
	public String captureOrder(String orderId) {
	    String accessToken = getAccessToken();
	    log.info("Attempting to capture funds for Order ID: {}", orderId);

	    try {
	        // The capture endpoint requires an empty JSON body or specific payment instructions.
	        // Sending an empty map is the safest way to trigger the default capture.
	        Map<String, Object> emptyPayload = Map.of();

	        PaypalResponseDto response = restClient.post()
	                .uri(baseUrl + "/v2/checkout/orders/" + orderId + "/capture")
	                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
	                .contentType(MediaType.APPLICATION_JSON)
	                .body(emptyPayload)
	                .retrieve()
	                .body(PaypalResponseDto.class);

	        // Verify the money actually moved
	        if ("COMPLETED".equals(response.getStatus())) {
	            log.info("SUCCESS! Payment captured for Order: {}", orderId);
	            // TODO: Here is where you update your database to mark the order as PAID!
	            return "COMPLETED";
	        } else {
	            log.warn("Order {} was processed but returned status: {}", orderId, response.getStatus());
	            return response.getStatus();
	        }

	    } catch (HttpClientErrorException e) {
	        log.error("Failed to capture order {}. Status: {}, Response: {}", 
	                  orderId, e.getStatusCode(), e.getResponseBodyAsString());
	        throw new RuntimeException("Payment capture failed. User may not have approved the transaction.");
	    }
	}
	
}