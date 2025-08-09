package ph.edu.cspb.kafkasendgrid.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.*;

/**
 * Unit tests for HealthController class.
 */
public class HealthControllerTest {

    private HealthController healthController;

    @BeforeMethod
    public void setUp() {
        healthController = new HealthController();
    }

    @Test
    public void testHealthEndpointReturnsCorrectStatus() {
        // Act
        ResponseEntity<Map<String, String>> response = healthController.health();

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testHealthEndpointReturnsCorrectBody() {
        // Act
        ResponseEntity<Map<String, String>> response = healthController.health();

        // Assert
        assertNotNull(response.getBody());
        Map<String, String> body = response.getBody();
        
        assertTrue(body.containsKey("status"));
        assertEquals(body.get("status"), "UP");
        
        assertTrue(body.containsKey("application"));
        assertEquals(body.get("application"), "kafka-sendgrid");
    }

    @Test
    public void testHealthEndpointBodyStructure() {
        // Act
        ResponseEntity<Map<String, String>> response = healthController.health();

        // Assert
        assertNotNull(response.getBody());
        Map<String, String> body = response.getBody();
        
        // Should have exactly 2 keys
        assertEquals(body.size(), 2);
        
        // Verify the keys exist
        assertTrue(body.containsKey("status"));
        assertTrue(body.containsKey("application"));
        
        // Verify values are not null or empty
        assertNotNull(body.get("status"));
        assertNotNull(body.get("application"));
        assertFalse(body.get("status").isEmpty());
        assertFalse(body.get("application").isEmpty());
    }

    @Test
    public void testHealthEndpointMultipleCalls() {
        // Act - Call the endpoint multiple times
        ResponseEntity<Map<String, String>> response1 = healthController.health();
        ResponseEntity<Map<String, String>> response2 = healthController.health();
        ResponseEntity<Map<String, String>> response3 = healthController.health();

        // Assert - All responses should be identical
        assertEquals(response1.getStatusCode(), HttpStatus.OK);
        assertEquals(response2.getStatusCode(), HttpStatus.OK);
        assertEquals(response3.getStatusCode(), HttpStatus.OK);
        
        assertEquals(response1.getBody(), response2.getBody());
        assertEquals(response2.getBody(), response3.getBody());
        assertEquals(response1.getBody(), response3.getBody());
    }

    @Test
    public void testHealthEndpointResponseNotNull() {
        // Act
        ResponseEntity<Map<String, String>> response = healthController.health();

        // Assert
        assertNotNull(response);
        assertNotNull(response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testHealthEndpointStatusValues() {
        // Act
        ResponseEntity<Map<String, String>> response = healthController.health();

        // Assert
        Map<String, String> body = response.getBody();
        
        // Verify specific values
        assertEquals(body.get("status"), "UP");
        assertEquals(body.get("application"), "kafka-sendgrid");
        
        // Verify case sensitivity
        assertNotEquals(body.get("status"), "up");
        assertNotEquals(body.get("status"), "Up");
        assertNotEquals(body.get("application"), "Kafka-sendgrid");
        assertNotEquals(body.get("application"), "KAFKA-SENDGRID");
    }

    @Test
    public void testHealthEndpointResponseHeaders() {
        // Act
        ResponseEntity<Map<String, String>> response = healthController.health();

        // Assert
        assertNotNull(response.getHeaders());
        // Spring Boot automatically adds Content-Type header for JSON responses
        // The actual headers testing would be better done in integration tests
    }

    @Test
    public void testHealthEndpointImmutability() {
        // Act
        ResponseEntity<Map<String, String>> response = healthController.health();
        Map<String, String> body = response.getBody();

        // Try to modify the response body
        try {
            body.put("newKey", "newValue");
            // If we can modify it, ensure our original values are still there
            assertEquals(body.get("status"), "UP");
            assertEquals(body.get("application"), "kafka-sendgrid");
        } catch (UnsupportedOperationException e) {
            // It's fine if the map is immutable
        }

        // Verify original values are intact
        assertTrue(body.containsKey("status"));
        assertTrue(body.containsKey("application"));
    }

    @Test
    public void testHealthControllerInstantiation() {
        // Act
        HealthController controller = new HealthController();

        // Assert
        assertNotNull(controller);
        
        // Verify it can be called immediately after instantiation
        ResponseEntity<Map<String, String>> response = controller.health();
        assertNotNull(response);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }
}