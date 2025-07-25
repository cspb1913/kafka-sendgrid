package com.cspb1913.kafkasendgrid.service;

import com.cspb1913.kafkasendgrid.model.EmailMessage;
import com.sendgrid.SendGrid;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * Simple integration test for EmailService using Spring Boot Test.
 */
@SpringBootTest
public class EmailServiceIntegrationTest extends AbstractTestNGSpringContextTests {

    @MockBean
    private SendGrid sendGrid;

    @Test
    public void testEmailServiceBean() {
        // This test verifies that the EmailService can be instantiated within Spring context
        assertNotNull(applicationContext);
    }

    @Test
    public void testEmailMessageCreation() {
        EmailMessage message = new EmailMessage("test@example.com", "Subject", "Body", null);
        assertNotNull(message);
        assertNotNull(message.getTo());
        assertNotNull(message.getSubject());
        assertNotNull(message.getBody());
    }
}