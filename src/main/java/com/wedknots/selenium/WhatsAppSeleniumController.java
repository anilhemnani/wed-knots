package com.wedknots.selenium;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp")
@ConditionalOnProperty(name = "delivery.whatsapp-personal.enabled", havingValue = "true")
public class WhatsAppSeleniumController {

    private final WhatsAppSeleniumService service;

    public WhatsAppSeleniumController(WhatsAppSeleniumService service) {
        this.service = service;
    }

    public static record SendRequest(String phone, String message) {}

    @PostMapping("/send-personal")
    public ResponseEntity<?> send(@RequestBody SendRequest req) {
        if (req.phone() == null || req.phone().isBlank() ||
            req.message() == null || req.message().isBlank()) {
            return ResponseEntity.badRequest().body("phone and message are required");
        }
        service.sendMessage(req.phone(), req.message());
        return ResponseEntity.ok("Message sent");
    }
}

