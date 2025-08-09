package ph.edu.cspb.kafkasendgrid.service;

import ph.edu.cspb.kafkasendgrid.model.EmailMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for consuming messages from Kafka and processing email requests.
 * Supports both plain text emails and template-based emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @KafkaListener(topics = "${spring.kafka.topic.name}")
    public void consumeEmailMessage(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received message from topic: {}, partition: {}, offset: {}", topic, partition, offset);
        log.debug("Message content: {}", message);

        try {
            // Parse JSON message
            EmailMessage emailMessage = objectMapper.readValue(message, EmailMessage.class);
            
            // Log message type for debugging
            if (emailMessage.isTemplateEmail()) {
                log.debug("Processing template email with template ID: {} and type: {}", 
                    emailMessage.getTemplateId(), emailMessage.getNotificationType());
            } else {
                log.debug("Processing plain text email");
            }
            
            // Validate the email message
            Set<ConstraintViolation<EmailMessage>> violations = validator.validate(emailMessage);
            
            // Additional validation for content requirements
            if (!emailMessage.isTemplateEmail() && !emailMessage.hasPlainTextContent()) {
                log.error("Email must have either template ID or plain text content (subject and body)");
                acknowledgment.acknowledge();
                return;
            }
            
            if (!violations.isEmpty()) {
                String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
                log.error("Invalid email message: {}", errors);
                // Acknowledge even invalid messages to avoid reprocessing
                acknowledgment.acknowledge();
                return;
            }

            // Send email
            emailService.sendEmail(emailMessage);
            
            // Acknowledge successful processing
            acknowledgment.acknowledge();
            
            if (emailMessage.isTemplateEmail()) {
                log.info("Successfully processed template email for recipient: {} (template: {}, type: {})", 
                    emailMessage.getTo(), emailMessage.getTemplateId(), emailMessage.getNotificationType());
            } else {
                log.info("Successfully processed plain text email for recipient: {} with subject: {}", 
                    emailMessage.getTo(), emailMessage.getSubject());
            }
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid email message format: {}", e.getMessage());
            // Acknowledge to prevent infinite retries for invalid messages
            acknowledgment.acknowledge();
        } catch (IOException e) {
            log.error("Failed to parse or send email message: {}", message, e);
            // Don't acknowledge - let Kafka retry for transient errors
        } catch (Exception e) {
            log.error("Unexpected error processing message: {}", message, e);
            // Acknowledge to prevent infinite retries for permanently broken messages
            acknowledgment.acknowledge();
        }
    }
}