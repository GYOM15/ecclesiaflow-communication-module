package com.ecclesiaflow.communication.web.controller;

import com.ecclesiaflow.communication.business.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Test controller for debugging communication sending.
 * WARNING: Remove in production!
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/test/emails")
@RequiredArgsConstructor
public class EmailTestController {
    
    private final EmailService emailService;
    
    /**
     * Forces manual queue processing for debugging.
     */
    @PostMapping("/process-queue")
    public ResponseEntity<Map<String, Object>> processQueue() {
        try {
            int processed = emailService.processQueuedEmails(10);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "processed", processed,
                "message", "Queue processed successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "error", e.getMessage(),
                "stackTrace", e.getClass().getName()
            ));
        }
    }
}
