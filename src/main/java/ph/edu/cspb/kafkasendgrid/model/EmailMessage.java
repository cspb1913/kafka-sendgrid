package ph.edu.cspb.kafkasendgrid.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Model class representing an email message to be sent via SendGrid.
 * Supports both plain text emails and SendGrid dynamic templates.
 * This class is used to deserialize JSON messages from Kafka.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailMessage {

    @JsonProperty("to")
    @NotBlank(message = "Recipient email address is required")
    @Email(message = "Invalid recipient email address format")
    private String to;

    @JsonProperty("subject")
    private String subject; // Optional for template emails

    @JsonProperty("body")
    private String body; // Optional for template emails

    @JsonProperty("from")
    private String from; // Optional, will use default if not provided
    
    @JsonProperty("templateId")
    private String templateId; // SendGrid dynamic template ID
    
    @JsonProperty("templateData")
    private Map<String, Object> templateData; // Dynamic data for template substitution
    
    @JsonProperty("notificationType")
    private String notificationType; // Type of notification (submission, status_update, etc.)
    
    /**
     * Determines if this message should use a SendGrid template
     * @return true if templateId is provided, false otherwise
     */
    public boolean isTemplateEmail() {
        return templateId != null && !templateId.trim().isEmpty();
    }
    
    /**
     * Validates if the message has required fields for plain text email
     * @return true if subject and body are present
     */
    public boolean hasPlainTextContent() {
        return subject != null && !subject.trim().isEmpty() 
            && body != null && !body.trim().isEmpty();
    }
}