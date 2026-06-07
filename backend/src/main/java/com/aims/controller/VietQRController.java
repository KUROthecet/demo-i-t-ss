package com.aims.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/api/vqr")
public class VietQRController {

    private static final String BANK_ID      = "MB";
    private static final String ACCOUNT_NO   = "0909999999";
    private static final String ACCOUNT_NAME = "NGUYEN VAN A";

    @GetMapping("/generate-qr")
    public String generateVietQr(@RequestParam String orderId, @RequestParam int amount) {
        String content = "DH" + orderId;
        return String.format(
                "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                BANK_ID, ACCOUNT_NO, amount, content, ACCOUNT_NAME
        );
    }

    @PostMapping("/api/token_generate")
    public ResponseEntity<String> testTokenGen(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded);
            String[] values    = credentials.split(":", 2);
            String username    = values[0];
            String password    = values[1];
            if ("aims".equals(username) && "0805".equals(password)) {
                return ResponseEntity.ok("{\"access_token\":\"COMPLETED\"}");
            }
        }
        return ResponseEntity.status(401).body("Unauthorized");
    }
}
