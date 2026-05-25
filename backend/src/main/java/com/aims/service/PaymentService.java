/*
Coupling level: Common Coupling
Reason why: Stateful singleton relying on globally shared configurations/environment variables.
*/

/*
Coupling level: Control Coupling
Reason why: Passes a String flag (paymentMethod) to control which internal payment logic executes; uses a boolean (isSuccess) to branch flow. 
*/

package com.aims.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.aims.adapter.RefundResult;
import com.aims.dto.request.PaymentRequestDto;
import com.aims.dto.response.PaymentResponseDto;
import com.aims.entity.Order;
import com.aims.entity.PaymentTransaction;
import com.aims.enums.PaymentStatus;
import com.aims.repository.OrderRepository;
import com.aims.repository.PaymentTransactionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    // VietQR Properties
    @Value("${vietqr.api.url}") private String vietQrUrl;
    @Value("${vietqr.bank.bin}") private String bankBin;
    @Value("${vietqr.bank.account}") private String bankAccount;
    @Value("${vietqr.bank.account-name}") private String bankAccountName;
    @Value("${vietqr.client-id}") private String vietQrClientId;
    @Value("${vietqr.api-key}") private String vietQrApiKey;

    // PayPal Properties
    @Value("${paypal.api.base-url}") private String paypalBaseUrl;
    @Value("${paypal.client-id}") private String paypalClientId;
    @Value("${paypal.client-secret}") private String paypalSecret;

    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        if ("VIETQR".equalsIgnoreCase(request.getPaymentMethod())) {
            return generateVietQr(request);
        } else if ("PAYPAL".equalsIgnoreCase(request.getPaymentMethod())) {
            return generatePaypalUrl(request);
        } else {
            throw new IllegalArgumentException("Unsupported payment method");
        }
    }

    // --- VIETQR LOGIC ---
    private PaymentResponseDto generateVietQr(PaymentRequestDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", vietQrClientId);
        headers.set("x-api-key", vietQrApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("accountNo", bankAccount);
        body.put("accountName", bankAccountName);
        body.put("acqId", Integer.parseInt(bankBin));
        body.put("amount", request.getAmount()); // VietQR uses integers for VND
        body.put("addInfo", request.getOrderInfo() + " " + request.getOrderId());
        body.put("format", "text");
        body.put("template", "compact");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(vietQrUrl, entity, Map.class);
            Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");
            String qrDataURL = (String) responseData.get("qrDataURL"); // Returns Base64 image string

            return PaymentResponseDto.builder()
                    .paymentMethod("VIETQR")
                    .qrCodeData(qrDataURL)
                    .message("QR Code generated successfully. Scan to pay.")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate VietQR code: " + e.getMessage());
        }
    }

    // --- PAYPAL LOGIC ---
    private PaymentResponseDto generatePaypalUrl(PaymentRequestDto request) {
        // 1. Get Access Token
        String accessToken = getPaypalAccessToken();

        // 2. Create Order
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> amount = Map.of(
                "currency_code", "USD", // Convert your VND amount to USD if necessary!
                "value", request.getAmount()
        );

        Map<String, Object> purchaseUnit = Map.of(
                "reference_id", request.getOrderId(),
                "amount", amount
        );

        Map<String, Object> body = Map.of(
                "intent", "CAPTURE",
                "purchase_units", List.of(purchaseUnit),
                "application_context", Map.of(
                        "return_url", "http://localhost:4200/payment/success",
                        "cancel_url", "http://localhost:4200/payment/cancel"
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(paypalBaseUrl + "/v2/checkout/orders", entity, Map.class);
            List<Map<String, String>> links = (List<Map<String, String>>) response.getBody().get("links");
            
            // Find the approval URL to send to the frontend
            String approveUrl = links.stream()
                    .filter(link -> "approve".equals(link.get("rel")))
                    .findFirst()
                    .map(link -> link.get("href"))
                    .orElseThrow(() -> new RuntimeException("No approve URL found"));

            return PaymentResponseDto.builder()
                    .paymentMethod("PAYPAL")
                    .paypalApproveUrl(approveUrl)
                    .message("Redirect to PayPal to complete payment.")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PayPal order: " + e.getMessage());
        }
    }

    private String getPaypalAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(paypalClientId, paypalSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(paypalBaseUrl + "/v1/oauth2/token", entity, Map.class);
        return (String) response.getBody().get("access_token");
    }

    @Transactional
    public void verifyTransactionAndUpdateOrder(long orderId, boolean isSuccess, String paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
                
        if (isSuccess) {
            order.setPaymentStatus(PaymentStatus.PAID);
            
            PaymentTransaction pt = new PaymentTransaction();
            pt.setAmount(order.getTotalAmount());
            pt.setTransactionDate(LocalDateTime.now());
            pt.setPaymentMethod(paymentMethod);
            pt.setTransactionContent("Payment for Order " + orderId);
            pt.setDatetime(LocalDateTime.now());
            
            paymentTransactionRepository.save(pt);
            
        } else {
        }
        orderRepository.save(order);
    }

	public RefundResult processRefund(String name, String orderCode, int totalAmount) {
		// TODO Auto-generated method stub
		return null;
	}
}