package com.aims.controller;

import java.util.Base64;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vqr")
@RequiredArgsConstructor
public class VietQRController {
	
	// Cấu hình thông tin ngân hàng cố định của bạn
    private final String BANK_ID = "MB"; // Mã ngân hàng (VD: MB, VCB, TCB...)
    private final String ACCOUNT_NO = "0909999999"; // Số tài khoản thụ hưởng
    private final String ACCOUNT_NAME = "NGUYEN VAN A"; // Tên chủ tài khoản

    @GetMapping("/generate-qr")
    public String generateVietQr(
            @RequestParam String orderId, 
            @RequestParam int amount) {
            
        // Nội dung chuyển khoản nên là một chuỗi duy nhất, không dấu. Ví dụ: DH12345
        String content = "DH" + orderId; 
        
        // URL ảnh QR của vietqr.io (có thể trả về cho Frontend hiển thị thẻ <img src="...">)
        String qrUrl = String.format(
            "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
            BANK_ID, ACCOUNT_NO, amount, content, ACCOUNT_NAME
        );
        
        return qrUrl;
    }
	
	@PostMapping("/api/token_generate") 
    public ResponseEntity<String> testTokenGen(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // VietQR sends credentials via HTTP Basic Auth header: "Basic <base64String>"
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded);
            
            // credentials will look like "username:password" (e.g., "aims:0805")
            String[] values = credentials.split(":", 2);
            String username = values[0];
            String password = values[1];
            
            System.out.println("Received Auth - Username: " + username + ", Password: " + password);
            
            // Validate the credentials matching your VietQR portal settings
            if ("aims".equals(username) && "0805".equals(password)) {
            	return ResponseEntity.ok("{\"access_token\":\"COMPLETED\"}");
            }
        }

        // Return 401 if authentication fails or header is missing
        return ResponseEntity.status(401).body("Unauthorized");
    }
}
