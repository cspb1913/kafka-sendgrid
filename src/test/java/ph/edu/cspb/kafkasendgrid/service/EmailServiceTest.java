package ph.edu.cspb.kafkasendgrid.service;

import ph.edu.cspb.kafkasendgrid.model.EmailMessage;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Unit tests for EmailService class with comprehensive SendGrid API mocking.
 */
public class EmailServiceTest {

    @Mock
    private SendGrid sendGrid;

    @InjectMocks
    private EmailService emailService;

    private final String defaultFromEmail = "default@example.com";

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(emailService, "defaultFromEmail", defaultFromEmail);
    }

    @Test
    public void testSendEmailSuccess() throws IOException {
        // Arrange
        EmailMessage emailMessage = new EmailMessage("recipient@example.com", "Test Subject", "Test Body", "sender@example.com");
        
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
        EmailMessage emailMessage = new EmailMessage("recipient@example.com", "Test Subject", "Test Body", null);
        
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
        EmailMessage emailMessage = new EmailMessage("recipient@example.com", "Test Subject", "Test Body", "");
        
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
        EmailMessage emailMessage = new EmailMessage("recipient@example.com", "Test Subject", "Test Body", "sender@example.com");
        
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
        EmailMessage emailMessage = new EmailMessage("recipient@example.com", "Test Subject", "Test Body", "sender@example.com");
        
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
        EmailMessage emailMessage = new EmailMessage("recipient@example.com", "Test Subject", "Test Body", "sender@example.com");
        
        when(sendGrid.api(any(Request.class))).thenThrow(new IOException("Network error"));

        // Act
        emailService.sendEmail(emailMessage);

        // Assert - IOException should be propagated
    }

    @Test
    public void testSendEmailWith200StatusCode() throws IOException {
        // Arrange
        EmailMessage emailMessage = new EmailMessage("recipient@example.com", "Test Subject", "Test Body", "sender@example.com");
        
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
        EmailMessage emailMessage = new EmailMessage("recipient@example.com", "Test Subject", "Test Body", "sender@example.com");
        
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
        EmailMessage emailMessage = new EmailMessage("recipient@example.com", "Test Subject", "Test Body", "sender@example.com");
        
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
        EmailMessage emailMessage = new EmailMessage(
            "recipient@example.com", 
            "Test Subject with Special Characters: àáâãäåæçèéêë", 
            "Test Body with\nmultiple lines\nand special characters: ñóôõö÷øù",
            "sender@example.com"
        );
        
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
        EmailMessage emailMessage = new EmailMessage("test@example.com", "Subject", "Body", "from@example.com");
        
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
}