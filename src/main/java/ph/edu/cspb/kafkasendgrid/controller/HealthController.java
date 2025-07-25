package ph.edu.cspb.kafkasendgrid.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health controller for Kubernetes readiness and liveness probes.
 * Provides a simple /health endpoint that returns application health status.
 */
@RestController
public class HealthController {

    /**
     * Health endpoint for Kubernetes probes.
     * Returns 200 OK if the application is healthy.
     * 
     * @return ResponseEntity with health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("application", "kafka-sendgrid");
        return ResponseEntity.ok(healthStatus);
    }
}