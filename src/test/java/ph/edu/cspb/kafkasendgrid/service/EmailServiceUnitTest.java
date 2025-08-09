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
 * Simple unit tests for EmailService class focused on achieving coverage.
 */
public class EmailServiceUnitTest {

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
        verify(sendGrid, times(1)).api(any(Request.class));
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
        verify(sendGrid, times(1)).api(any(Request.class));
    }

    @Test(expectedExceptions = RuntimeException.class)
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
    public void testSendEmailRequestStructure() throws IOException {
        // Arrange
        EmailMessage emailMessage = new EmailMessage("test@example.com", "Subject", "Body", "from@example.com");
        
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
    public void testSendEmailWithEmptyFromAddress() throws IOException {
        // Arrange
        EmailMessage emailMessage = new EmailMessage("recipient@example.com", "Test Subject", "Test Body", "");
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusCode()).thenReturn(202);
        when(mockResponse.getBody()).thenReturn("Accepted");
        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(emailMessage);

        // Assert
        verify(sendGrid, times(1)).api(any(Request.class));
    }
}