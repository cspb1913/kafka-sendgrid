package ph.edu.cspb.kafkasendgrid.integration;

import ph.edu.cspb.kafkasendgrid.model.EmailMessage;
import ph.edu.cspb.kafkasendgrid.service.EmailService;
import ph.edu.cspb.kafkasendgrid.service.KafkaConsumerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Integration tests for Kafka message consumption using embedded Kafka.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, 
               topics = {"test-email-notifications"}, 
               brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.topic.name=test-email-notifications",
    "spring.kafka.consumer.group-id=test-consumer-group",
    "spring.kafka.consumer.auto-offset-reset=earliest",
    "sendgrid.api-key=test-api-key",
    "sendgrid.from-email=test@example.com"
})
@DirtiesContext
@Test(enabled = false) // Temporarily disable integration test
public class KafkaIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private KafkaConsumerService kafkaConsumerService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SendGrid sendGrid;

    @MockitoBean
    private EmailService emailService;

    @Test
    public void testKafkaConsumerServiceIsLoaded() {
        // Assert
        assertNotNull(kafkaConsumerService);
        assertNotNull(objectMapper);
    }

    @Test
    public void testSpringContextLoads() {
        // Assert that the Spring context loads successfully
        assertNotNull(applicationContext);
        assertTrue(applicationContext.containsBean("kafkaConsumerService"));
        assertTrue(applicationContext.containsBean("emailService"));
        assertTrue(applicationContext.containsBean("objectMapper"));
    }

    @Test
    public void testKafkaConsumerServiceConfiguration() {
        // Verify that the KafkaConsumerService is properly configured
        assertNotNull(kafkaConsumerService);
        
        // The service should be a Spring-managed bean
        Object bean = applicationContext.getBean("kafkaConsumerService");
        assertNotNull(bean);
        assertTrue(bean instanceof KafkaConsumerService);
    }

    @Test
    public void testEmailServiceMockIsConfigured() {
        // Verify that EmailService is properly mocked
        assertNotNull(emailService);
        
        // The service should be a mock
        assertTrue(org.mockito.Mockito.mockingDetails(emailService).isMock());
    }

    @Test
    public void testSendGridMockIsConfigured() {
        // Verify that SendGrid is properly mocked
        assertNotNull(sendGrid);
        
        // The service should be a mock
        assertTrue(org.mockito.Mockito.mockingDetails(sendGrid).isMock());
    }

    @Test
    public void testObjectMapperFunctionality() throws Exception {
        // Test that ObjectMapper is working correctly
        EmailMessage emailMessage = EmailMessage.builder()
            .to("test@example.com")
            .subject("Test Subject")
            .body("Test Body")
            .from("from@example.com")
            .build();
        
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
    public void testKafkaConsumerServiceCanProcessMessage() throws Exception {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder()
            .to("test@example.com")
            .subject("Test Subject")
            .body("Test Body")
            .from("from@example.com")
            .build();
        String jsonMessage = objectMapper.writeValueAsString(emailMessage);
        
        // Mock the email service to not throw exceptions
        doNothing().when(emailService).sendEmail(any(EmailMessage.class));
        
        // Create a mock acknowledgment
        org.springframework.kafka.support.Acknowledgment mockAck = mock(org.springframework.kafka.support.Acknowledgment.class);
        
        // Act - Directly call the consumer method (simulating Kafka message delivery)
        kafkaConsumerService.consumeEmailMessage(jsonMessage, "test-email-notifications", 0, 100L, mockAck);
        
        // Assert
        verify(emailService, times(1)).sendEmail(any(EmailMessage.class));
        verify(mockAck, times(1)).acknowledge();
    }

    @Test
    public void testKafkaConsumerServiceHandlesInvalidJson() throws Exception {
        // Arrange
        String invalidJson = "invalid-json-message";
        
        // Create a mock acknowledgment
        org.springframework.kafka.support.Acknowledgment mockAck = mock(org.springframework.kafka.support.Acknowledgment.class);
        
        // Act - Directly call the consumer method with invalid JSON
        kafkaConsumerService.consumeEmailMessage(invalidJson, "test-email-notifications", 0, 100L, mockAck);
        
        // Assert
        verify(emailService, never()).sendEmail(any(EmailMessage.class));
        verify(mockAck, never()).acknowledge(); // Should not acknowledge invalid JSON
    }

    @Test
    public void testKafkaConsumerServiceHandlesEmailServiceFailure() throws Exception {
        // Arrange
        EmailMessage emailMessage = EmailMessage.builder()
            .to("test@example.com")
            .subject("Test Subject")
            .body("Test Body")
            .from("from@example.com")
            .build();
        String jsonMessage = objectMapper.writeValueAsString(emailMessage);
        
        // Mock the email service to throw an IOException
        doThrow(new java.io.IOException("SendGrid API error")).when(emailService).sendEmail(any(EmailMessage.class));
        
        // Create a mock acknowledgment
        org.springframework.kafka.support.Acknowledgment mockAck = mock(org.springframework.kafka.support.Acknowledgment.class);
        
        // Act - Directly call the consumer method
        kafkaConsumerService.consumeEmailMessage(jsonMessage, "test-email-notifications", 0, 100L, mockAck);
        
        // Assert
        verify(emailService, times(1)).sendEmail(any(EmailMessage.class));
        verify(mockAck, never()).acknowledge(); // Should not acknowledge on IOException
    }

    @Test
    public void testApplicationContextContainsBeans() {
        // Assert that all necessary beans are present in the context
        assertTrue(applicationContext.containsBean("kafkaConsumerService"));
        assertTrue(applicationContext.containsBean("emailService"));
        assertTrue(applicationContext.containsBean("sendGrid"));
        assertTrue(applicationContext.containsBean("objectMapper"));
        assertTrue(applicationContext.containsBean("validator"));
        assertTrue(applicationContext.containsBean("healthController"));
    }

    @Test
    public void testKafkaConfigurationBeans() {
        // Assert that Kafka configuration beans are present
        assertTrue(applicationContext.containsBean("consumerFactory"));
        assertTrue(applicationContext.containsBean("kafkaListenerContainerFactory"));
    }

    @Test
    public void testBeanInstances() {
        // Get bean instances and verify they are not null
        KafkaConsumerService consumerService = applicationContext.getBean(KafkaConsumerService.class);
        assertNotNull(consumerService);
        
        ObjectMapper mapper = applicationContext.getBean(ObjectMapper.class);
        assertNotNull(mapper);
        
        jakarta.validation.Validator validator = applicationContext.getBean(jakarta.validation.Validator.class);
        assertNotNull(validator);
    }
}