package ph.edu.cspb.kafkasendgrid.service;

import ph.edu.cspb.kafkasendgrid.model.EmailMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.Acknowledgment;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Simple unit tests for KafkaConsumerService class focused on achieving coverage.
 */
public class KafkaConsumerServiceUnitTest {

    @Mock
    private EmailService emailService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Validator validator;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    private EmailMessage validEmailMessage;
    private String validJsonMessage;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        validEmailMessage = new EmailMessage("test@example.com", "Test Subject", "Test Body", "sender@example.com");
        validJsonMessage = "{\"to\":\"test@example.com\",\"subject\":\"Test Subject\",\"body\":\"Test Body\",\"from\":\"sender@example.com\"}";
    }

    @Test
    public void testConsumeEmailMessageSuccess() throws IOException {
        // Arrange
        when(objectMapper.readValue(validJsonMessage, EmailMessage.class)).thenReturn(validEmailMessage);
        when(validator.validate(validEmailMessage)).thenReturn(Collections.emptySet());
        doNothing().when(emailService).sendEmail(validEmailMessage);

        // Act
        kafkaConsumerService.consumeEmailMessage(validJsonMessage, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(validJsonMessage, EmailMessage.class);
        verify(validator, times(1)).validate(validEmailMessage);
        verify(emailService, times(1)).sendEmail(validEmailMessage);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    public void testConsumeEmailMessageValidationFailure() throws IOException {
        // Arrange
        Set<ConstraintViolation<EmailMessage>> violations = new HashSet<>();
        ConstraintViolation<EmailMessage> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Email address is invalid");
        violations.add(violation);

        when(objectMapper.readValue(validJsonMessage, EmailMessage.class)).thenReturn(validEmailMessage);
        when(validator.validate(validEmailMessage)).thenReturn(violations);

        // Act
        kafkaConsumerService.consumeEmailMessage(validJsonMessage, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(validJsonMessage, EmailMessage.class);
        verify(validator, times(1)).validate(validEmailMessage);
        verify(emailService, never()).sendEmail(any(EmailMessage.class));
        verify(acknowledgment, times(1)).acknowledge(); // Should acknowledge even invalid messages
    }

    @Test
    public void testConsumeEmailMessageJsonParsingFailure() throws IOException {
        // Arrange
        String invalidJson = "invalid-json";
        when(objectMapper.readValue(invalidJson, EmailMessage.class)).thenThrow(new IOException("JSON parsing failed"));

        // Act
        kafkaConsumerService.consumeEmailMessage(invalidJson, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(invalidJson, EmailMessage.class);
        verify(validator, never()).validate(any(EmailMessage.class));
        verify(emailService, never()).sendEmail(any(EmailMessage.class));
        verify(acknowledgment, never()).acknowledge(); // Should not acknowledge parsing failures
    }

    @Test
    public void testConsumeEmailMessageEmailServiceIOException() throws IOException {
        // Arrange
        when(objectMapper.readValue(validJsonMessage, EmailMessage.class)).thenReturn(validEmailMessage);
        when(validator.validate(validEmailMessage)).thenReturn(Collections.emptySet());
        doThrow(new IOException("SendGrid API error")).when(emailService).sendEmail(validEmailMessage);

        // Act
        kafkaConsumerService.consumeEmailMessage(validJsonMessage, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(validJsonMessage, EmailMessage.class);
        verify(validator, times(1)).validate(validEmailMessage);
        verify(emailService, times(1)).sendEmail(validEmailMessage);
        verify(acknowledgment, never()).acknowledge(); // Should not acknowledge on IOException
    }

    @Test
    public void testConsumeEmailMessageUnexpectedException() throws IOException {
        // Arrange
        when(objectMapper.readValue(validJsonMessage, EmailMessage.class)).thenReturn(validEmailMessage);
        when(validator.validate(validEmailMessage)).thenReturn(Collections.emptySet());
        doThrow(new NullPointerException("Unexpected error")).when(emailService).sendEmail(validEmailMessage);

        // Act
        kafkaConsumerService.consumeEmailMessage(validJsonMessage, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(validJsonMessage, EmailMessage.class);
        verify(validator, times(1)).validate(validEmailMessage);
        verify(emailService, times(1)).sendEmail(validEmailMessage);
        verify(acknowledgment, times(1)).acknowledge(); // Should acknowledge unexpected exceptions to prevent infinite retries
    }

    @Test
    public void testConsumeEmailMessageWithNullFromAddress() throws IOException {
        // Arrange
        EmailMessage messageWithNullFrom = new EmailMessage("test@example.com", "Test Subject", "Test Body", null);
        String jsonWithNullFrom = "{\"to\":\"test@example.com\",\"subject\":\"Test Subject\",\"body\":\"Test Body\"}";
        
        when(objectMapper.readValue(jsonWithNullFrom, EmailMessage.class)).thenReturn(messageWithNullFrom);
        when(validator.validate(messageWithNullFrom)).thenReturn(Collections.emptySet());
        doNothing().when(emailService).sendEmail(messageWithNullFrom);

        // Act
        kafkaConsumerService.consumeEmailMessage(jsonWithNullFrom, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(jsonWithNullFrom, EmailMessage.class);
        verify(validator, times(1)).validate(messageWithNullFrom);
        verify(emailService, times(1)).sendEmail(messageWithNullFrom);
        verify(acknowledgment, times(1)).acknowledge();
    }
}