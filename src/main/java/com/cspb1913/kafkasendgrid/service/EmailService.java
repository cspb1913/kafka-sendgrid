package com.cspb1913.kafkasendgrid.service;

import com.cspb1913.kafkasendgrid.model.EmailMessage;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Service class for sending emails via SendGrid API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final SendGrid sendGrid;

    @Value("${sendgrid.from-email}")
    private String defaultFromEmail;

    /**
     * Sends an email using SendGrid API.
     *
     * @param emailMessage the email message to send
     * @throws IOException if sending fails
     */
    public void sendEmail(EmailMessage emailMessage) throws IOException {
        Email from = new Email(emailMessage.getFrom() != null ? emailMessage.getFrom() : defaultFromEmail);
        Email to = new Email(emailMessage.getTo());
        Content content = new Content("text/plain", emailMessage.getBody());
        
        Mail mail = new Mail(from, emailMessage.getSubject(), to, content);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendGrid.api(request);
        
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            log.info("Email sent successfully to {} with subject: {}", emailMessage.getTo(), emailMessage.getSubject());
        } else {
            log.error("Failed to send email to {}. Status: {}, Body: {}", 
                emailMessage.getTo(), response.getStatusCode(), response.getBody());
            throw new RuntimeException("Failed to send email via SendGrid. Status: " + response.getStatusCode());
        }
    }
}