package com.cspb1913.kafkasendgrid.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.io.IOException;
import java.util.Set;

import static org.testng.Assert.*;

/**
 * Test class for EmailMessage model using TestNG.
 */
public class EmailMessageTest {

    private ObjectMapper objectMapper;
    private Validator validator;

    @BeforeMethod
    public void setUp() {
        objectMapper = new ObjectMapper();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testEmailMessageCreation() {
        // Arrange & Act
        EmailMessage emailMessage = new EmailMessage("test@example.com", "Test Subject", "Test Body", "from@example.com");

        // Assert
        assertEquals(emailMessage.getTo(), "test@example.com");
        assertEquals(emailMessage.getSubject(), "Test Subject");
        assertEquals(emailMessage.getBody(), "Test Body");
        assertEquals(emailMessage.getFrom(), "from@example.com");
    }

    @Test
    public void testEmailMessageDefaultConstructor() {
        // Arrange & Act
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setTo("test@example.com");
        emailMessage.setSubject("Test Subject");
        emailMessage.setBody("Test Body");

        // Assert
        assertEquals(emailMessage.getTo(), "test@example.com");
        assertEquals(emailMessage.getSubject(), "Test Subject");
        assertEquals(emailMessage.getBody(), "Test Body");
        assertNull(emailMessage.getFrom());
    }

    @Test
    public void testEmailMessageValidation() {
        // Arrange
        EmailMessage validMessage = new EmailMessage("test@example.com", "Test Subject", "Test Body", null);

        // Act
        Set<ConstraintViolation<EmailMessage>> violations = validator.validate(validMessage);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testEmailMessageValidationInvalidEmail() {
        // Arrange
        EmailMessage invalidMessage = new EmailMessage("invalid-email", "Test Subject", "Test Body", null);

        // Act
        Set<ConstraintViolation<EmailMessage>> violations = validator.validate(invalidMessage);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Invalid recipient email address format")));
    }

    @Test
    public void testEmailMessageValidationBlankFields() {
        // Arrange
        EmailMessage invalidMessage = new EmailMessage("", "", "", null);

        // Act
        Set<ConstraintViolation<EmailMessage>> violations = validator.validate(invalidMessage);

        // Assert
        assertTrue(violations.size() >= 3); // At least 3 violations for blank fields
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("required")));
    }

    @Test
    public void testEmailMessageJSONSerialization() throws IOException {
        // Arrange
        EmailMessage emailMessage = new EmailMessage("test@example.com", "Test Subject", "Test Body", "from@example.com");

        // Act
        String json = objectMapper.writeValueAsString(emailMessage);
        EmailMessage deserializedMessage = objectMapper.readValue(json, EmailMessage.class);

        // Assert
        assertEquals(deserializedMessage.getTo(), emailMessage.getTo());
        assertEquals(deserializedMessage.getSubject(), emailMessage.getSubject());
        assertEquals(deserializedMessage.getBody(), emailMessage.getBody());
        assertEquals(deserializedMessage.getFrom(), emailMessage.getFrom());
    }

    @Test
    public void testEmailMessageJSONDeserializationWithMissingFields() throws IOException {
        // Arrange
        String json = "{\"to\":\"test@example.com\",\"subject\":\"Test Subject\",\"body\":\"Test Body\"}";

        // Act
        EmailMessage emailMessage = objectMapper.readValue(json, EmailMessage.class);

        // Assert
        assertEquals(emailMessage.getTo(), "test@example.com");
        assertEquals(emailMessage.getSubject(), "Test Subject");
        assertEquals(emailMessage.getBody(), "Test Body");
        assertNull(emailMessage.getFrom());
    }

    @Test
    public void testEmailMessageEqualsAndHashCode() {
        // Arrange
        EmailMessage message1 = new EmailMessage("test@example.com", "Test Subject", "Test Body", "from@example.com");
        EmailMessage message2 = new EmailMessage("test@example.com", "Test Subject", "Test Body", "from@example.com");
        EmailMessage message3 = new EmailMessage("different@example.com", "Test Subject", "Test Body", "from@example.com");

        // Assert
        assertEquals(message1, message2);
        assertEquals(message1.hashCode(), message2.hashCode());
        assertNotEquals(message1, message3);
        assertNotEquals(message1.hashCode(), message3.hashCode());
    }

    @Test
    public void testEmailMessageToString() {
        // Arrange
        EmailMessage emailMessage = new EmailMessage("test@example.com", "Test Subject", "Test Body", "from@example.com");

        // Act
        String toString = emailMessage.toString();

        // Assert
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("Test Subject"));
        assertTrue(toString.contains("Test Body"));
        assertTrue(toString.contains("from@example.com"));
    }
}