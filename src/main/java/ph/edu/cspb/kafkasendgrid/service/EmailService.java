package ph.edu.cspb.kafkasendgrid.service;

import ph.edu.cspb.kafkasendgrid.config.TemplateConfig;
import ph.edu.cspb.kafkasendgrid.model.EmailMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * Service class for sending emails via SendGrid API.
 * Supports both plain text emails and dynamic template emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final SendGrid sendGrid;
    private final TemplateConfig templateConfig;
    private final ObjectMapper objectMapper;

    @Value("${sendgrid.from-email}")
    private String defaultFromEmail;
    
    @Value("${sendgrid.from-name:Form 137 System}")
    private String defaultFromName;

    /**
     * Sends an email using SendGrid API.
     * Automatically detects whether to use template or plain text based on message content.
     *
     * @param emailMessage the email message to send
     * @throws IOException if sending fails
     */
    public void sendEmail(EmailMessage emailMessage) throws IOException {
        // Determine if we should use a template
        String templateId = determineTemplateId(emailMessage);
        
        if (templateId != null && !templateId.isEmpty()) {
            sendTemplateEmail(emailMessage, templateId);
        } else if (emailMessage.hasPlainTextContent()) {
            sendPlainTextEmail(emailMessage);
        } else {
            throw new IllegalArgumentException("Email must have either a template ID or plain text content (subject and body)");
        }
    }
    
    /**
     * Determines which template ID to use based on the email message
     * @param emailMessage the email message
     * @return template ID or null if none should be used
     */
    private String determineTemplateId(EmailMessage emailMessage) {
        // If template ID is explicitly provided, use it
        if (emailMessage.getTemplateId() != null && !emailMessage.getTemplateId().trim().isEmpty()) {
            return emailMessage.getTemplateId();
        }
        
        // If notification type is provided and templates are enabled, look up template
        if (templateConfig.isUseTemplatesByDefault() && emailMessage.getNotificationType() != null) {
            String templateId = templateConfig.getTemplateId(emailMessage.getNotificationType());
            if (templateId != null && !templateId.trim().isEmpty()) {
                log.debug("Using template {} for notification type {}", templateId, emailMessage.getNotificationType());
                return templateId;
            }
        }
        
        return null;
    }
    
    /**
     * Sends a template-based email using SendGrid dynamic templates
     * @param emailMessage the email message
     * @param templateId the SendGrid template ID
     * @throws IOException if sending fails
     */
    private void sendTemplateEmail(EmailMessage emailMessage, String templateId) throws IOException {
        Email from = new Email(
            emailMessage.getFrom() != null ? emailMessage.getFrom() : defaultFromEmail,
            defaultFromName
        );
        Email to = new Email(emailMessage.getTo());
        
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setTemplateId(templateId);
        
        Personalization personalization = new Personalization();
        personalization.addTo(to);
        
        // Add dynamic template data if provided
        if (emailMessage.getTemplateData() != null && !emailMessage.getTemplateData().isEmpty()) {
            for (Map.Entry<String, Object> entry : emailMessage.getTemplateData().entrySet()) {
                personalization.addDynamicTemplateData(entry.getKey(), entry.getValue());
            }
        }
        
        // Add subject if provided (some templates may use dynamic subjects)
        if (emailMessage.getSubject() != null && !emailMessage.getSubject().trim().isEmpty()) {
            personalization.setSubject(emailMessage.getSubject());
        }
        
        mail.addPersonalization(personalization);
        
        sendMail(mail, emailMessage, "template");
    }
    
    /**
     * Sends a plain text email (backward compatibility)
     * @param emailMessage the email message
     * @throws IOException if sending fails
     */
    private void sendPlainTextEmail(EmailMessage emailMessage) throws IOException {
        Email from = new Email(
            emailMessage.getFrom() != null ? emailMessage.getFrom() : defaultFromEmail,
            defaultFromName
        );
        Email to = new Email(emailMessage.getTo());
        Content content = new Content("text/plain", emailMessage.getBody());
        
        Mail mail = new Mail(from, emailMessage.getSubject(), to, content);
        
        sendMail(mail, emailMessage, "plain text");
    }
    
    /**
     * Common method to send mail via SendGrid API
     * @param mail the Mail object to send
     * @param emailMessage the original email message for logging
     * @param emailType type of email for logging
     * @throws IOException if sending fails
     */
    private void sendMail(Mail mail, EmailMessage emailMessage, String emailType) throws IOException {
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendGrid.api(request);
        
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            log.info("Successfully sent {} email to {} (type: {})", 
                emailType, emailMessage.getTo(), emailMessage.getNotificationType());
            log.debug("Response: Status={}, Headers={}", response.getStatusCode(), response.getHeaders());
        } else {
            log.error("Failed to send {} email to {}. Status: {}, Body: {}", 
                emailType, emailMessage.getTo(), response.getStatusCode(), response.getBody());
            throw new RuntimeException(String.format(
                "Failed to send %s email via SendGrid. Status: %d, Error: %s", 
                emailType, response.getStatusCode(), response.getBody()
            ));
        }
    }
}