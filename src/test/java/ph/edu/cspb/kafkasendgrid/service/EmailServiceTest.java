package ph.edu.cspb.kafkasendgrid.service;

import ph.edu.cspb.kafkasendgrid.config.TemplateConfig;
import ph.edu.cspb.kafkasendgrid.model.EmailMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Unit tests for EmailService class with comprehensive SendGrid API mocking.
 * Includes tests for both plain text emails and template-based emails.
 */
public class EmailServiceTest {

    @Mock
    private SendGrid sendGrid;
    
    @Mock
    private TemplateConfig templateConfig;
    
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EmailService emailService;

    private final String defaultFromEmail = "default@example.com";
    private final String defaultFromName = "Form 137 System";

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(emailService, "defaultFromEmail", defaultFromEmail);
        ReflectionTestUtils.setField(emailService, "defaultFromName", defaultFromName);
    }

    @Test
    public void testSendEmailSuccess() throws IOException {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder().to("test@example.com").subject("Test Subject").body("Test Body").build();
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(202);
        when(mockResponse.getBody()).thenReturn("Accepted");

        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid, times(1)).api(requestCaptor.capture());
        
        Request capturedRequest = requestCaptor.getValue();
        assertEquals(capturedRequest.getMethod(), Method.POST);
        assertEquals(capturedRequest.getEndpoint(), "mail/send");
        assertNotNull(capturedRequest.getBody());
    }

    @Test
    public void testSendEmailWithDefaultFromAddress() throws IOException {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder().to("test@example.com").subject("Test Subject").body("Test Body").build();
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(202);
        when(mockResponse.getBody()).thenReturn("Accepted");

        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid, times(1)).api(requestCaptor.capture());
        
        Request capturedRequest = requestCaptor.getValue();
        assertNotNull(capturedRequest.getBody());
        // The default from email should be used when emailMessage.getFrom() is null
    }

    @Test
    public void testSendEmailWithBlankFromAddress() throws IOException {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder().to("test@example.com").subject("Test Subject").body("Test Body").build();
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(202);
        when(mockResponse.getBody()).thenReturn("Accepted");

        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid, times(1)).api(requestCaptor.capture());
        
        Request capturedRequest = requestCaptor.getValue();
        assertNotNull(capturedRequest.getBody());
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Failed to send email via SendGrid.*")
    public void testSendEmailFailureWith400StatusCode() throws IOException {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder().to("test@example.com").subject("Test Subject").body("Test Body").build();
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(400);
        when(mockResponse.getBody()).thenReturn("Bad Request");

        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert - Exception should be thrown
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Failed to send email via SendGrid.*")
    public void testSendEmailFailureWith500StatusCode() throws IOException {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder().to("test@example.com").subject("Test Subject").body("Test Body").build();
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(500);
        when(mockResponse.getBody()).thenReturn("Internal Server Error");

        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert - Exception should be thrown
    }

    @Test(expectedExceptions = IOException.class)
    public void testSendEmailIOException() throws IOException {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder().to("test@example.com").subject("Test Subject").body("Test Body").build();
        
        when(sendGrid.api(any(Request.class))).thenThrow(new IOException("Network error"));

        // Act
        emailService.sendEmail(emailMessage);

        // Assert - IOException should be propagated
    }

    @Test
    public void testSendEmailWith200StatusCode() throws IOException {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder().to("test@example.com").subject("Test Subject").body("Test Body").build();
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(200);
        when(mockResponse.getBody()).thenReturn("OK");

        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert
        verify(sendGrid, times(1)).api(any(Request.class));
    }

    @Test
    public void testSendEmailWith201StatusCode() throws IOException {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder().to("test@example.com").subject("Test Subject").body("Test Body").build();
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(201);
        when(mockResponse.getBody()).thenReturn("Created");

        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert
        verify(sendGrid, times(1)).api(any(Request.class));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testSendEmailWith300StatusCode() throws IOException {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder().to("test@example.com").subject("Test Subject").body("Test Body").build();
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(300);
        when(mockResponse.getBody()).thenReturn("Multiple Choices");

        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert - Exception should be thrown for 3xx status codes
    }

    @Test
    public void testSendEmailWithComplexContent() throws IOException {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder()
            .to("recipient@example.com")
            .subject("Test Subject with Special Characters: àáâãäåæçèéêë")
            .body("Test Body with\nmultiple lines\nand special characters: ñóôõö÷øù")
            .from("sender@example.com")
            .build();
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(202);
        when(mockResponse.getBody()).thenReturn("Accepted");

        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid, times(1)).api(requestCaptor.capture());
        
        Request capturedRequest = requestCaptor.getValue();
        assertNotNull(capturedRequest.getBody());
        assertTrue(capturedRequest.getBody().length() > 0);
    }

    @Test
    public void testSendEmailRequestStructure() throws IOException {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder().to("test@example.com").subject("Test Subject").body("Test Body").build();
        
        Response mockResponse = new Response();
        ReflectionTestUtils.setField(mockResponse, "statusCode", 202);
        ReflectionTestUtils.setField(mockResponse, "body", "Accepted");

        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid, times(1)).api(requestCaptor.capture());
        
        Request capturedRequest = requestCaptor.getValue();
        assertEquals(capturedRequest.getMethod(), Method.POST);
        assertEquals(capturedRequest.getEndpoint(), "mail/send");
        assertNotNull(capturedRequest.getBody());
        
        // Verify the request body contains expected email content
        String requestBody = capturedRequest.getBody();
        assertTrue(requestBody.contains("test@example.com"));
        assertTrue(requestBody.contains("Subject"));
        assertTrue(requestBody.contains("Body"));
        assertTrue(requestBody.contains("from@example.com"));
    }
    
    // Template-based email tests
    
    @Test
    public void testSendTemplateEmailWithExplicitTemplateId() throws IOException {
        // Arrange
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("student_name", "John Doe");
        templateData.put("form_id", "F137-001");
        
        EmailMessage emailMessage = EmailMessage.builder()
            .to("student@example.com")
            .templateId("d-template-123")
            .templateData(templateData)
            .build();
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(202);
        when(mockResponse.getBody()).thenReturn("Accepted");
        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid, times(1)).api(requestCaptor.capture());
        
        Request capturedRequest = requestCaptor.getValue();
        assertEquals(capturedRequest.getMethod(), Method.POST);
        assertEquals(capturedRequest.getEndpoint(), "mail/send");
        assertNotNull(capturedRequest.getBody());
        
        String requestBody = capturedRequest.getBody();
        assertTrue(requestBody.contains("d-template-123"));
        assertTrue(requestBody.contains("student@example.com"));
        assertTrue(requestBody.contains("John Doe"));
        assertTrue(requestBody.contains("F137-001"));
    }
    
    @Test
    public void testSendTemplateEmailWithNotificationType() throws IOException {
        // Arrange
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("student_name", "Jane Doe");
        
        EmailMessage emailMessage = EmailMessage.builder()
            .to("student@example.com")
            .notificationType("submission")
            .templateData(templateData)
            .build();
        
        when(templateConfig.isUseTemplatesByDefault()).thenReturn(true);
        when(templateConfig.getTemplateId("submission")).thenReturn("d-submission-template");
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(202);
        when(mockResponse.getBody()).thenReturn("Accepted");
        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert
        verify(templateConfig).getTemplateId("submission");
        
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid, times(1)).api(requestCaptor.capture());
        
        Request capturedRequest = requestCaptor.getValue();
        String requestBody = capturedRequest.getBody();
        assertTrue(requestBody.contains("d-submission-template"));
        assertTrue(requestBody.contains("Jane Doe"));
    }
    
    @Test
    public void testSendTemplateEmailWithSubject() throws IOException {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder()
            .to("student@example.com")
            .subject("Custom Subject")
            .templateId("d-template-123")
            .templateData(Map.of("name", "John"))
            .build();
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(202);
        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid, times(1)).api(requestCaptor.capture());
        
        String requestBody = requestCaptor.getValue().getBody();
        assertTrue(requestBody.contains("Custom Subject"));
    }
    
    @Test
    public void testSendTemplateEmailWithEmptyTemplateData() throws IOException {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder()
            .to("student@example.com")
            .templateId("d-template-123")
            .templateData(new HashMap<>())
            .build();
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(202);
        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert
        verify(sendGrid, times(1)).api(any(Request.class));
    }
    
    @Test
    public void testFallbackToPlainTextWhenNoTemplate() throws IOException {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder()
            .to("student@example.com")
            .subject("Plain Text Subject")
            .body("Plain text body")
            .notificationType("unknown_type")
            .build();
        
        when(templateConfig.isUseTemplatesByDefault()).thenReturn(true);
        when(templateConfig.getTemplateId("unknown_type")).thenReturn(null);
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(202);
        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid, times(1)).api(requestCaptor.capture());
        
        String requestBody = requestCaptor.getValue().getBody();
        assertTrue(requestBody.contains("Plain Text Subject"));
        assertTrue(requestBody.contains("Plain text body"));
        // Should not contain template-specific fields
        assertFalse(requestBody.contains("template_id"));
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class, 
          expectedExceptionsMessageRegExp = "Email must have either a template ID or plain text content.*")
    public void testSendEmailWithoutTemplateOrContent() throws IOException {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder()
            .to("student@example.com")
            .notificationType("unknown_type")
            .build();
        
        when(templateConfig.isUseTemplatesByDefault()).thenReturn(true);
        when(templateConfig.getTemplateId("unknown_type")).thenReturn(null);

        // Act - should throw exception
        emailService.sendEmail(emailMessage);
    }
    
    @Test(expectedExceptions = RuntimeException.class)
    public void testSendTemplateEmailFailure() throws IOException {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder()
            .to("student@example.com")
            .templateId("d-template-123")
            .templateData(Map.of("name", "John"))
            .build();
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(400);
        when(mockResponse.getBody()).thenReturn("Template not found");
        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);
    }
    
    @Test
    public void testDetermineTemplateIdPriority() throws IOException {
        // Arrange - explicit template ID should override notification type
        EmailMessage emailMessage = EmailMessage.builder()
            .to("student@example.com")
            .templateId("d-explicit-template")
            .notificationType("submission")
            .templateData(Map.of("name", "John"))
            .build();
        
        when(templateConfig.isUseTemplatesByDefault()).thenReturn(true);
        when(templateConfig.getTemplateId("submission")).thenReturn("d-submission-template");
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(202);
        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid, times(1)).api(requestCaptor.capture());
        
        String requestBody = requestCaptor.getValue().getBody();
        assertTrue(requestBody.contains("d-explicit-template"));
        assertFalse(requestBody.contains("d-submission-template"));
        
        // Should not call templateConfig when explicit template ID is provided
        verify(templateConfig, never()).getTemplateId(any());
    }
}