package com.aims.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.aims.dto.request.OrderRequestDto;
import com.aims.service.PaypalService;
import com.aims.service.PaypalService;

import java.util.Map;

@RestController
@RequestMapping("/api/paypal")
@RequiredArgsConstructor
public class PaypalController {

    private final PaypalService paypalService;
    
    @PostMapping("/capture/{id}") 
    public ResponseEntity<String> capturePaypalOrder(
            @PathVariable("id") String id // 3. Catch the {id} from the URL
    ) {
        // Call the service method we built earlier
        String status = paypalService.captureOrder(id);
        
        // 4. Return plain text to match Angular's responseType: 'text'
        if ("COMPLETED".equals(status)) {
            return ResponseEntity.ok("COMPLETED");
        } else {
            return ResponseEntity.badRequest().body("Capture failed with status: " + status);
        }
    }
}
