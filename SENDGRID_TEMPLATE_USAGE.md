# SendGrid Dynamic Template Usage Guide

This document explains how to use the newly implemented SendGrid dynamic template functionality in the f137-k-sendgrid microservice.

## Overview

The microservice now supports both traditional plain text emails and SendGrid dynamic templates. This allows email templates to be managed in SendGrid's UI rather than composed in code.

## Configuration

### 1. Application Configuration (application.yml)

```yaml
sendgrid:
  api-key: ${SENDGRID_API_KEY:your-sendgrid-api-key}
  from-email: ${SENDGRID_FROM_EMAIL:no-reply@yourdomain.com}
  from-name: ${SENDGRID_FROM_NAME:Form 137 System}
  templates:
    use-templates-by-default: ${SENDGRID_USE_TEMPLATES:true}
    default-template-id: ${SENDGRID_DEFAULT_TEMPLATE_ID:}
    ids:
      submission: ${SENDGRID_TEMPLATE_SUBMISSION:d-xxx-submission-template-id}
      status_update: ${SENDGRID_TEMPLATE_STATUS_UPDATE:d-xxx-status-update-template-id}
      approval: ${SENDGRID_TEMPLATE_APPROVAL:d-xxx-approval-template-id}
      rejection: ${SENDGRID_TEMPLATE_REJECTION:d-xxx-rejection-template-id}
      reminder: ${SENDGRID_TEMPLATE_REMINDER:d-xxx-reminder-template-id}
      completion: ${SENDGRID_TEMPLATE_COMPLETION:d-xxx-completion-template-id}
```

### 2. Environment Variables

Set these environment variables in your deployment:

```bash
SENDGRID_API_KEY=SG.your-actual-api-key
SENDGRID_FROM_EMAIL=no-reply@yourschool.edu
SENDGRID_FROM_NAME=Form 137 System
SENDGRID_USE_TEMPLATES=true
SENDGRID_TEMPLATE_SUBMISSION=d-12345678901234567890123456789012
SENDGRID_TEMPLATE_STATUS_UPDATE=d-98765432109876543210987654321098
# ... add more template IDs as needed
```

## Message Formats

### 1. Template-based Email Message

Send a message with a specific template ID:

```json
{
  "to": "student@example.com",
  "templateId": "d-12345678901234567890123456789012",
  "templateData": {
    "student_name": "John Doe",
    "form_id": "F137-2024-001",
    "submission_date": "2024-01-15",
    "status": "submitted",
    "tracking_url": "https://portal.yourschool.edu/form137/track/F137-2024-001"
  }
}
```

### 2. Notification Type-based Template

Send a message that automatically maps to a template based on notification type:

```json
{
  "to": "student@example.com",
  "notificationType": "submission",
  "templateData": {
    "student_name": "Jane Smith",
    "form_id": "F137-2024-002",
    "submission_date": "2024-01-16"
  }
}
```

### 3. Plain Text Email (Backward Compatibility)

Traditional plain text email still works:

```json
{
  "to": "admin@example.com",
  "subject": "Form 137 System Alert",
  "body": "This is a plain text notification email.",
  "from": "system@yourschool.edu"
}
```

### 4. Mixed Content (Template with Subject Override)

You can combine template usage with custom subjects:

```json
{
  "to": "student@example.com",
  "subject": "Custom Subject for Your Form 137",
  "templateId": "d-12345678901234567890123456789012",
  "templateData": {
    "student_name": "Bob Johnson",
    "form_id": "F137-2024-003"
  }
}
```

## Template Data Variables

Common template variables you can use in SendGrid templates:

### Student Information
- `student_name`: Full name of the student
- `student_id`: Student ID number
- `student_email`: Student email address

### Form Information  
- `form_id`: Unique Form 137 identifier
- `form_type`: Type of Form 137 request
- `submission_date`: Date form was submitted
- `completion_date`: Date form was completed

### Status Information
- `status`: Current status (submitted, processing, approved, rejected, completed)
- `status_message`: Detailed status message
- `next_action`: What the student needs to do next

### System Information
- `tracking_url`: URL to track form progress
- `deadline`: Important deadlines
- `contact_email`: Support contact email
- `contact_phone`: Support contact phone

## Template Examples in SendGrid

### Submission Confirmation Template

Subject: `Form 137 Submitted - {{form_id}}`

```html
<h1>Form 137 Submission Confirmed</h1>
<p>Dear {{student_name}},</p>
<p>Your Form 137 request ({{form_id}}) has been successfully submitted on {{submission_date}}.</p>
<p><strong>Next Steps:</strong></p>
<ul>
  <li>Your request is being reviewed</li>
  <li>You will receive updates via email</li>
  <li>Track your progress: <a href="{{tracking_url}}">Click here</a></li>
</ul>
<p>Thank you!</p>
```

### Status Update Template

Subject: `Form 137 Status Update - {{form_id}}`

```html
<h1>Form 137 Status Update</h1>
<p>Dear {{student_name}},</p>
<p>Your Form 137 request ({{form_id}}) status has been updated:</p>
<div style="background-color: #f0f0f0; padding: 15px; margin: 10px 0;">
  <strong>Status:</strong> {{status}}<br>
  <strong>Message:</strong> {{status_message}}
</div>
{{#if next_action}}
<p><strong>Action Required:</strong> {{next_action}}</p>
{{/if}}
<p>Track your progress: <a href="{{tracking_url}}">Click here</a></p>
```

## Producer Integration

When publishing messages to Kafka from other services in the Form 137 system:

### Java Example (using Spring Kafka)

```java
@Service
public class Form137NotificationService {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public void sendSubmissionNotification(String studentEmail, String studentName, String formId) {
        EmailMessage message = EmailMessage.builder()
            .to(studentEmail)
            .notificationType("submission")
            .templateData(Map.of(
                "student_name", studentName,
                "form_id", formId,
                "submission_date", LocalDate.now().toString(),
                "tracking_url", "https://portal.yourschool.edu/form137/track/" + formId
            ))
            .build();
            
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send("form137-email-notifications", messageJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to send notification", e);
        }
    }
}
```

## Benefits of Template-based Emails

1. **Centralized Template Management**: Update email designs in SendGrid UI without code changes
2. **Professional Design**: Use SendGrid's template editor for better-looking emails
3. **Multi-language Support**: Easy to manage different language versions
4. **Analytics**: Track email performance in SendGrid dashboard
5. **Responsive Design**: Templates automatically work on mobile devices
6. **Brand Consistency**: Maintain consistent branding across all emails

## Fallback Behavior

The system is designed to be backward compatible:

1. If a `templateId` is provided, it uses the template
2. If only `notificationType` is provided, it looks up the template ID from configuration
3. If no template is found, it falls back to plain text email (requires `subject` and `body`)
4. If neither template nor plain text content is provided, the message is rejected

## Troubleshooting

### Common Issues

1. **Template not found**: Verify template ID in SendGrid dashboard
2. **Missing template data**: Check that all required variables are provided
3. **Fallback to plain text**: Ensure templates are properly configured in application.yml

### Logging

Enable debug logging to troubleshoot:

```yaml
logging:
  level:
    ph.edu.cspb.kafkasendgrid: DEBUG
```

This will show detailed information about template selection and email processing.