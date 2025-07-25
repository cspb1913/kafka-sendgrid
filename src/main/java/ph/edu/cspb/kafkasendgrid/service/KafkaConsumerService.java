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

/**
 * Service class for consuming messages from Kafka and processing email requests.
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
            
            // Validate the email message
            Set<ConstraintViolation<EmailMessage>> violations = validator.validate(emailMessage);
            if (!violations.isEmpty()) {
                StringBuilder sb = new StringBuilder("Validation errors: ");
                for (ConstraintViolation<EmailMessage> violation : violations) {
                    sb.append(violation.getMessage()).append("; ");
                }
                log.error("Invalid email message: {}", sb.toString());
                // Acknowledge even invalid messages to avoid reprocessing
                acknowledgment.acknowledge();
                return;
            }

            // Send email
            emailService.sendEmail(emailMessage);
            
            // Acknowledge successful processing
            acknowledgment.acknowledge();
            log.info("Successfully processed email message for recipient: {}", emailMessage.getTo());
            
        } catch (IOException e) {
            log.error("Failed to parse or send email message: {}", message, e);
            // Don't acknowledge - let Kafka retry
        } catch (Exception e) {
            log.error("Unexpected error processing message: {}", message, e);
            // Acknowledge to prevent infinite retries for permanently broken messages
            acknowledgment.acknowledge();
        }
    }
}