package ph.edu.cspb.kafkasendgrid.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for managing SendGrid dynamic template IDs.
 * Maps notification types to their corresponding SendGrid template IDs.
 */
@Configuration
@ConfigurationProperties(prefix = "sendgrid.templates")
@Data
public class TemplateConfig {
    
    /**
     * Map of notification types to SendGrid template IDs
     * Key: notification type (e.g., "submission", "status_update", "approval")
     * Value: SendGrid dynamic template ID
     */
    private Map<String, String> ids = new HashMap<>();
    
    /**
     * Default template ID to use if notification type is not found
     */
    private String defaultTemplateId;
    
    /**
     * Whether to use templates by default when available
     */
    private boolean useTemplatesByDefault = true;
    
    /**
     * Get template ID for a specific notification type
     * @param notificationType the type of notification
     * @return template ID or null if not found
     */
    public String getTemplateId(String notificationType) {
        if (notificationType == null || notificationType.trim().isEmpty()) {
            return defaultTemplateId;
        }
        
        return ids.getOrDefault(notificationType.toLowerCase(), defaultTemplateId);
    }
    
    /**
     * Check if a template exists for the given notification type
     * @param notificationType the type of notification
     * @return true if template exists
     */
    public boolean hasTemplate(String notificationType) {
        if (notificationType == null || notificationType.trim().isEmpty()) {
            return defaultTemplateId != null && !defaultTemplateId.trim().isEmpty();
        }
        
        String templateId = ids.get(notificationType.toLowerCase());
        return templateId != null && !templateId.trim().isEmpty();
    }
}