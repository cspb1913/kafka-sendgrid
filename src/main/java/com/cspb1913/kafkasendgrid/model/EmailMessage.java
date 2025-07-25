package com.cspb1913.kafkasendgrid.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Model class representing an email message to be sent via SendGrid.
 * This class is used to deserialize JSON messages from Kafka.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {

    @JsonProperty("to")
    @NotBlank(message = "Recipient email address is required")
    @Email(message = "Invalid recipient email address format")
    private String to;

    @JsonProperty("subject")
    @NotBlank(message = "Email subject is required")
    private String subject;

    @JsonProperty("body")
    @NotBlank(message = "Email body is required")
    private String body;

    @JsonProperty("from")
    private String from; // Optional, will use default if not provided
}