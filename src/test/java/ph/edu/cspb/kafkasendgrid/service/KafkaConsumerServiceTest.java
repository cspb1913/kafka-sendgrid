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
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Unit tests for KafkaConsumerService class with comprehensive mocking.
 */
public class KafkaConsumerServiceTest {

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
    private EmailMessage validTemplateEmailMessage;
    private String validJsonMessage;
    private String validTemplateJsonMessage;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Plain text email message
        validEmailMessage = EmailMessage.builder()
            .to("test@example.com")
            .subject("Test Subject")
            .body("Test Body")
            .from("sender@example.com")
            .build();
        validJsonMessage = "{\"to\":\"test@example.com\",\"subject\":\"Test Subject\",\"body\":\"Test Body\",\"from\":\"sender@example.com\"}";
        
        // Template email message
        validTemplateEmailMessage = EmailMessage.builder()
            .to("student@example.com")
            .templateId("d-template-123")
            .notificationType("submission")
            .templateData(Map.of("student_name", "John Doe", "form_id", "F137-001"))
            .build();
        validTemplateJsonMessage = "{\"to\":\"student@example.com\",\"templateId\":\"d-template-123\",\"notificationType\":\"submission\",\"templateData\":{\"student_name\":\"John Doe\",\"form_id\":\"F137-001\"}}";
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
    public void testConsumeEmailMessageWithNullFromAddress() throws IOException {
        // Arrange
        EmailMessage messageWithNullFrom = EmailMessage.builder()
            .to("test@example.com")
            .subject("Test Subject")
            .body("Test Body")
            .build();
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
    public void testConsumeEmailMessageMultipleValidationFailures() throws IOException {
        // Arrange
        Set<ConstraintViolation<EmailMessage>> violations = new HashSet<>();
        ConstraintViolation<EmailMessage> violation1 = mock(ConstraintViolation.class);
        when(violation1.getMessage()).thenReturn("Email address is invalid");
        ConstraintViolation<EmailMessage> violation2 = mock(ConstraintViolation.class);
        when(violation2.getMessage()).thenReturn("Subject is required");
        violations.add(violation1);
        violations.add(violation2);

        when(objectMapper.readValue(validJsonMessage, EmailMessage.class)).thenReturn(validEmailMessage);
        when(validator.validate(validEmailMessage)).thenReturn(violations);

        // Act
        kafkaConsumerService.consumeEmailMessage(validJsonMessage, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(validJsonMessage, EmailMessage.class);
        verify(validator, times(1)).validate(validEmailMessage);
        verify(emailService, never()).sendEmail(any(EmailMessage.class));
        verify(acknowledgment, times(1)).acknowledge();
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
    public void testConsumeEmailMessageEmailServiceRuntimeException() throws IOException {
        // Arrange
        when(objectMapper.readValue(validJsonMessage, EmailMessage.class)).thenReturn(validEmailMessage);
        when(validator.validate(validEmailMessage)).thenReturn(Collections.emptySet());
        doThrow(new RuntimeException("SendGrid API rate limit")).when(emailService).sendEmail(validEmailMessage);

        // Act
        kafkaConsumerService.consumeEmailMessage(validJsonMessage, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(validJsonMessage, EmailMessage.class);
        verify(validator, times(1)).validate(validEmailMessage);
        verify(emailService, times(1)).sendEmail(validEmailMessage);
        verify(acknowledgment, never()).acknowledge(); // Should not acknowledge on RuntimeException from email service
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
    public void testConsumeEmailMessageWithDifferentTopicAndPartition() throws IOException {
        // Arrange
        when(objectMapper.readValue(validJsonMessage, EmailMessage.class)).thenReturn(validEmailMessage);
        when(validator.validate(validEmailMessage)).thenReturn(Collections.emptySet());
        doNothing().when(emailService).sendEmail(validEmailMessage);

        // Act
        kafkaConsumerService.consumeEmailMessage(validJsonMessage, "different-topic", 3, 999L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(validJsonMessage, EmailMessage.class);
        verify(validator, times(1)).validate(validEmailMessage);
        verify(emailService, times(1)).sendEmail(validEmailMessage);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    public void testConsumeEmailMessageWithEmptyMessage() throws IOException {
        // Arrange
        String emptyMessage = "";
        when(objectMapper.readValue(emptyMessage, EmailMessage.class)).thenThrow(new IOException("Empty message"));

        // Act
        kafkaConsumerService.consumeEmailMessage(emptyMessage, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(emptyMessage, EmailMessage.class);
        verify(validator, never()).validate(any(EmailMessage.class));
        verify(emailService, never()).sendEmail(any(EmailMessage.class));
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    public void testConsumeEmailMessageWithNullMessage() throws IOException {
        // Arrange
        when(objectMapper.readValue((String) null, EmailMessage.class)).thenThrow(new IOException("Null message"));

        // Act
        kafkaConsumerService.consumeEmailMessage(null, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue((String) null, EmailMessage.class);
        verify(validator, never()).validate(any(EmailMessage.class));
        verify(emailService, never()).sendEmail(any(EmailMessage.class));
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    public void testConsumeEmailMessageObjectMapperThrowsRuntimeException() throws IOException {
        // Arrange
        when(objectMapper.readValue(anyString(), eq(EmailMessage.class)))
            .thenThrow(new RuntimeException("ObjectMapper internal error"));

        // Act
        kafkaConsumerService.consumeEmailMessage(validJsonMessage, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(validJsonMessage, EmailMessage.class);
        verify(validator, never()).validate(any(EmailMessage.class));
        verify(emailService, never()).sendEmail(any(EmailMessage.class));
        verify(acknowledgment, times(1)).acknowledge(); // Should acknowledge unexpected exceptions
    }

    @Test
    public void testConsumeEmailMessageValidatorThrowsException() throws IOException {
        // Arrange
        when(objectMapper.readValue(validJsonMessage, EmailMessage.class)).thenReturn(validEmailMessage);
        when(validator.validate(validEmailMessage)).thenThrow(new RuntimeException("Validator error"));

        // Act
        kafkaConsumerService.consumeEmailMessage(validJsonMessage, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(validJsonMessage, EmailMessage.class);
        verify(validator, times(1)).validate(validEmailMessage);
        verify(emailService, never()).sendEmail(any(EmailMessage.class));
        verify(acknowledgment, times(1)).acknowledge(); // Should acknowledge unexpected exceptions
    }
    
    // Template-based email tests
    
    @Test
    public void testConsumeTemplateEmailMessageSuccess() throws IOException {
        // Arrange
        when(objectMapper.readValue(validTemplateJsonMessage, EmailMessage.class)).thenReturn(validTemplateEmailMessage);
        when(validator.validate(validTemplateEmailMessage)).thenReturn(Collections.emptySet());
        doNothing().when(emailService).sendEmail(validTemplateEmailMessage);

        // Act
        kafkaConsumerService.consumeEmailMessage(validTemplateJsonMessage, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(validTemplateJsonMessage, EmailMessage.class);
        verify(validator, times(1)).validate(validTemplateEmailMessage);
        verify(emailService, times(1)).sendEmail(validTemplateEmailMessage);
        verify(acknowledgment, times(1)).acknowledge();
    }
    
    @Test
    public void testConsumeTemplateEmailWithNotificationTypeOnly() throws IOException {
        // Arrange
        EmailMessage notificationOnlyMessage = EmailMessage.builder()
            .to("student@example.com")
            .notificationType("submission")
            .templateData(Map.of("student_name", "Jane Doe"))
            .build();
        String notificationJson = "{\"to\":\"student@example.com\",\"notificationType\":\"submission\",\"templateData\":{\"student_name\":\"Jane Doe\"}}";
        
        when(objectMapper.readValue(notificationJson, EmailMessage.class)).thenReturn(notificationOnlyMessage);
        when(validator.validate(notificationOnlyMessage)).thenReturn(Collections.emptySet());
        doNothing().when(emailService).sendEmail(notificationOnlyMessage);

        // Act
        kafkaConsumerService.consumeEmailMessage(notificationJson, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(notificationJson, EmailMessage.class);
        verify(validator, times(1)).validate(notificationOnlyMessage);
        verify(emailService, times(1)).sendEmail(notificationOnlyMessage);
        verify(acknowledgment, times(1)).acknowledge();
    }
    
    @Test
    public void testConsumeEmailMessageWithoutTemplateOrContent() throws IOException {
        // Arrange - message with neither template nor plain text content
        EmailMessage invalidMessage = EmailMessage.builder()
            .to("student@example.com")
            .notificationType("unknown")
            .build();
        String invalidJson = "{\"to\":\"student@example.com\",\"notificationType\":\"unknown\"}";
        
        when(objectMapper.readValue(invalidJson, EmailMessage.class)).thenReturn(invalidMessage);
        when(validator.validate(invalidMessage)).thenReturn(Collections.emptySet());

        // Act
        kafkaConsumerService.consumeEmailMessage(invalidJson, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(invalidJson, EmailMessage.class);
        verify(validator, times(1)).validate(invalidMessage);
        verify(emailService, never()).sendEmail(any(EmailMessage.class));
        verify(acknowledgment, times(1)).acknowledge(); // Should acknowledge to prevent reprocessing
    }
    
    @Test
    public void testConsumeEmailMessageTemplateServiceFailure() throws IOException {
        // Arrange
        when(objectMapper.readValue(validTemplateJsonMessage, EmailMessage.class)).thenReturn(validTemplateEmailMessage);
        when(validator.validate(validTemplateEmailMessage)).thenReturn(Collections.emptySet());
        doThrow(new IllegalArgumentException("Template not found")).when(emailService).sendEmail(validTemplateEmailMessage);

        // Act
        kafkaConsumerService.consumeEmailMessage(validTemplateJsonMessage, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(validTemplateJsonMessage, EmailMessage.class);
        verify(validator, times(1)).validate(validTemplateEmailMessage);
        verify(emailService, times(1)).sendEmail(validTemplateEmailMessage);
        verify(acknowledgment, times(1)).acknowledge(); // Should acknowledge IllegalArgumentException
    }
    
    @Test
    public void testConsumeEmailMessageMixedContent() throws IOException {
        // Arrange - message with both template and plain text content
        EmailMessage mixedMessage = EmailMessage.builder()
            .to("student@example.com")
            .subject("Plain Text Subject")
            .body("Plain text body")
            .templateId("d-template-123")
            .templateData(Map.of("name", "John"))
            .build();
        String mixedJson = "{\"to\":\"student@example.com\",\"subject\":\"Plain Text Subject\",\"body\":\"Plain text body\",\"templateId\":\"d-template-123\",\"templateData\":{\"name\":\"John\"}}";
        
        when(objectMapper.readValue(mixedJson, EmailMessage.class)).thenReturn(mixedMessage);
        when(validator.validate(mixedMessage)).thenReturn(Collections.emptySet());
        doNothing().when(emailService).sendEmail(mixedMessage);

        // Act
        kafkaConsumerService.consumeEmailMessage(mixedJson, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(mixedJson, EmailMessage.class);
        verify(validator, times(1)).validate(mixedMessage);
        verify(emailService, times(1)).sendEmail(mixedMessage);
        verify(acknowledgment, times(1)).acknowledge();
    }
    
    @Test
    public void testConsumeEmailMessageWithEmptyTemplateData() throws IOException {
        // Arrange
        EmailMessage emptyDataMessage = EmailMessage.builder()
            .to("student@example.com")
            .templateId("d-template-123")
            .templateData(Collections.emptyMap())
            .build();
        String emptyDataJson = "{\"to\":\"student@example.com\",\"templateId\":\"d-template-123\",\"templateData\":{}}";
        
        when(objectMapper.readValue(emptyDataJson, EmailMessage.class)).thenReturn(emptyDataMessage);
        when(validator.validate(emptyDataMessage)).thenReturn(Collections.emptySet());
        doNothing().when(emailService).sendEmail(emptyDataMessage);

        // Act
        kafkaConsumerService.consumeEmailMessage(emptyDataJson, "test-topic", 0, 100L, acknowledgment);

        // Assert
        verify(objectMapper, times(1)).readValue(emptyDataJson, EmailMessage.class);
        verify(validator, times(1)).validate(emptyDataMessage);
        verify(emailService, times(1)).sendEmail(emptyDataMessage);
        verify(acknowledgment, times(1)).acknowledge();
    }
}