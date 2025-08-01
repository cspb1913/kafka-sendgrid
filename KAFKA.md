# Kafka Integration Guide

This document provides comprehensive instructions for connecting the kafka-sendgrid application to Apache Kafka, with specific focus on IBM Streams integration.

## Table of Contents

* [Overview](#overview)
* [IBM Streams Integration](#ibm-streams-integration)
* [Configuration Variables](#configuration-variables)
* [Message Format](#message-format)
* [Deployment Scenarios](#deployment-scenarios)
  * [Local Development](#local-development)
  * [Docker Deployment](#docker-deployment)
  * [Kubernetes Deployment](#kubernetes-deployment)
  * [IBM Cloud Deployment](#ibm-cloud-deployment)
* [Security Considerations](#security-considerations)
* [Monitoring and Troubleshooting](#monitoring-and-troubleshooting)
* [Performance Tuning](#performance-tuning)
* [Examples](#examples)

## Overview

The kafka-sendgrid application is designed to consume email messages from Kafka topics and forward them to SendGrid for delivery. It integrates seamlessly with IBM Streams through Kafka's standard protocol, allowing for reliable, scalable email processing in event-driven architectures.

### Architecture Flow

```
IBM Streams → Kafka Topic → kafka-sendgrid → SendGrid → Email Recipients
```

## IBM Streams Integration

### What is IBM Streams?

IBM Streams is a platform for developing and running streaming applications that can ingest, analyze, and correlate information as it arrives from thousands of real-time sources. When integrated with Kafka, it provides:

- Real-time data processing capabilities
- Complex event processing
- Integration with various data sources
- Scalable stream processing

### Integration Points

The kafka-sendgrid application connects to IBM Streams through:

1. **Kafka Topics**: IBM Streams applications produce messages to Kafka topics
2. **Message Format**: Standardized JSON format for email messages
3. **Error Handling**: Robust error handling for failed message processing
4. **Monitoring**: Integration with actuator endpoints for health monitoring

### IBM Streams to Kafka Configuration

To send messages from IBM Streams to Kafka (which kafka-sendgrid will consume):

1. **In your IBM Streams application**, use the Kafka toolkit:
   ```java
   // Example IBM Streams SPL code
   use com.ibm.streamsx.kafka::KafkaProducer;
   
   composite SendEmailsToKafka {
     graph
       stream<EmailMessage> ProcessedEmails = // your processing logic
       
       () as KafkaSink = KafkaProducer(ProcessedEmails) {
         param
           topic: "sendgrid-topic";
           bootstrapServers: "your-kafka-cluster:9092";
           keyAttribute: "to"; // Use recipient email as key
       }
   }
   ```

## Configuration Variables

### Required Environment Variables

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses (comma-separated) | `localhost:9092` | `kafka-broker1:9092,kafka-broker2:9092` |
| `SENDGRID_API_KEY` | SendGrid API key for email sending | `your-sendgrid-api-key` | `SG.xyz123...` |

### Optional Environment Variables

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `KAFKA_CONSUMER_GROUP_ID` | Consumer group ID for Kafka | `kafka-sendgrid-group` | `email-service-prod` |
| `KAFKA_AUTO_OFFSET_RESET` | Offset reset strategy | `earliest` | `latest` |
| `KAFKA_TOPIC_NAME` | Kafka topic to consume from | `sendgrid-topic` | `email-notifications` |
| `SENDGRID_FROM_EMAIL` | Default sender email address | `no-reply@yourdomain.com` | `notifications@company.com` |

### IBM Cloud Specific Variables

For IBM Cloud Kubernetes Service (IKS) and IBM Event Streams:

| Variable | Description | Example |
|----------|-------------|---------|
| `KAFKA_BOOTSTRAP_SERVERS` | IBM Event Streams brokers | `broker-0-xyz.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093` |
| `KAFKA_SECURITY_PROTOCOL` | Security protocol | `SASL_SSL` |
| `KAFKA_SASL_MECHANISM` | SASL mechanism | `PLAIN` |
| `KAFKA_SASL_USERNAME` | Service credentials username | `token` |
| `KAFKA_SASL_PASSWORD` | Service credentials password | `your-api-key` |

## Message Format

### Required JSON Structure

Messages sent to the Kafka topic must follow this JSON format:

```json
{
  "to": "recipient@example.com",
  "subject": "Your Email Subject",
  "body": "Email body content (supports HTML)",
  "from": "sender@yourdomain.com"
}
```

### Field Requirements

- **`to`** (required): Valid email address of the recipient
- **`subject`** (required): Email subject line
- **`body`** (required): Email content (plain text or HTML)
- **`from`** (optional): Sender email address (uses default if not provided)

### Example Messages

**Simple Text Email:**
```json
{
  "to": "user@example.com",
  "subject": "Welcome to Our Service",
  "body": "Thank you for signing up!"
}
```

**HTML Email with Custom Sender:**
```json
{
  "to": "customer@example.com",
  "subject": "Order Confirmation",
  "body": "<h1>Order Confirmed</h1><p>Your order #12345 has been confirmed.</p>",
  "from": "orders@company.com"
}
```

## Deployment Scenarios

### Local Development

#### 1. Prerequisites
- Java 17 or higher
- Docker (for local Kafka)
- SendGrid API key

#### 2. Start Local Kafka
```bash
# Using Docker Compose
docker run -d \
  --name kafka \
  -p 9092:9092 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  confluentinc/cp-kafka:latest
```

#### 3. Set Environment Variables
```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export KAFKA_TOPIC_NAME=sendgrid-topic
export SENDGRID_API_KEY=your-sendgrid-api-key
export SENDGRID_FROM_EMAIL=your-email@domain.com
```

#### 4. Run Application
```bash
./gradlew bootRun
```

### Docker Deployment

#### 1. Build Image
```bash
docker build -t kafka-sendgrid:1.0.0 .
```

#### 2. Run Container
```bash
docker run -d \
  --name kafka-sendgrid \
  -e KAFKA_BOOTSTRAP_SERVERS=your-kafka-cluster:9092 \
  -e KAFKA_TOPIC_NAME=sendgrid-topic \
  -e SENDGRID_API_KEY=your-sendgrid-api-key \
  -e SENDGRID_FROM_EMAIL=notifications@yourdomain.com \
  -p 8080:8080 \
  kafka-sendgrid:1.0.0
```

### Kubernetes Deployment

#### 1. Using Helm (Recommended)

Create a values override file:

```yaml
# values-production.yaml
config:
  kafka:
    bootstrapServers: "kafka-cluster.kafka.svc.cluster.local:9092"
    topicName: "email-notifications"
    consumerGroupId: "kafka-sendgrid-prod"
  
  sendgrid:
    fromEmail: "notifications@company.com"

secrets:
  sendgridApiKey: "your-sendgrid-api-key"

resources:
  limits:
    cpu: 1000m
    memory: 1Gi
  requests:
    cpu: 500m
    memory: 512Mi

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
```

Deploy with Helm:
```bash
helm install kafka-sendgrid ./helm/kafka-sendgrid -f values-production.yaml
```

#### 2. Manual Kubernetes Deployment

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: kafka-sendgrid-secrets
type: Opaque
stringData:
  sendgrid-api-key: "your-sendgrid-api-key"
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: kafka-sendgrid-config
data:
  KAFKA_BOOTSTRAP_SERVERS: "kafka-cluster:9092"
  KAFKA_TOPIC_NAME: "sendgrid-topic"
  SENDGRID_FROM_EMAIL: "notifications@company.com"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka-sendgrid
spec:
  replicas: 2
  selector:
    matchLabels:
      app: kafka-sendgrid
  template:
    metadata:
      labels:
        app: kafka-sendgrid
    spec:
      containers:
      - name: kafka-sendgrid
        image: ghcr.io/cspb1913/kafka-sendgrid:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SENDGRID_API_KEY
          valueFrom:
            secretKeyRef:
              name: kafka-sendgrid-secrets
              key: sendgrid-api-key
        envFrom:
        - configMapRef:
            name: kafka-sendgrid-config
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

### IBM Cloud Deployment

#### 1. IBM Event Streams Setup

First, create an IBM Event Streams instance and get credentials:

1. Create Event Streams service in IBM Cloud
2. Create service credentials
3. Note the broker URLs and API key

#### 2. IBM Cloud Kubernetes Service (IKS)

```yaml
# values-ibm-cloud.yaml
config:
  kafka:
    bootstrapServers: "broker-0-xyz.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093,broker-1-xyz.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093"
    topicName: "sendgrid-topic"
    consumerGroupId: "kafka-sendgrid-ibm-cloud"
    
secrets:
  sendgridApiKey: "your-sendgrid-api-key"
  kafkaUsername: "token"
  kafkaPassword: "your-event-streams-api-key"

# Additional IBM Cloud specific configurations
podSecurityContext:
  fsGroup: 1001

securityContext:
  allowPrivilegeEscalation: false
  runAsNonRoot: true
  runAsUser: 1001
```

#### 3. Deploy to IBM Cloud

```bash
# Login to IBM Cloud
ibmcloud login --apikey your-api-key

# Set Kubernetes context
ibmcloud ks cluster config --cluster your-cluster-name

# Deploy with IBM Cloud values
helm install kafka-sendgrid ./helm/kafka-sendgrid -f values-ibm-cloud.yaml
```

## Security Considerations

### 1. Kafka Security

For production deployments, especially with IBM Event Streams:

```yaml
# Additional environment variables for secure Kafka
KAFKA_SECURITY_PROTOCOL: SASL_SSL
KAFKA_SASL_MECHANISM: PLAIN
KAFKA_SASL_USERNAME: token
KAFKA_SASL_PASSWORD: your-event-streams-api-key
KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM: https
```

### 2. SendGrid API Key Management

- Store API keys in Kubernetes secrets
- Use IBM Cloud Key Protect for additional security
- Rotate API keys regularly
- Monitor API key usage

### 3. Network Security

- Use private endpoints for IBM Event Streams when possible
- Implement network policies in Kubernetes
- Use service mesh for additional security

## Monitoring and Troubleshooting

### Health Checks

The application provides several health check endpoints:

- **Health**: `GET /actuator/health`
- **Metrics**: `GET /actuator/metrics`
- **Info**: `GET /actuator/info`

### Common Issues and Solutions

#### 1. Kafka Connection Issues

**Problem**: Application cannot connect to Kafka brokers
```
Error: Failed to connect to Kafka brokers
```

**Solutions**:
- Verify `KAFKA_BOOTSTRAP_SERVERS` is correct
- Check network connectivity to Kafka brokers
- For IBM Event Streams, ensure credentials are correct
- Verify topic exists and has proper permissions

#### 2. SendGrid API Issues

**Problem**: Email sending fails
```
Error: Failed to send email via SendGrid
```

**Solutions**:
- Verify SendGrid API key is valid
- Check sender email is verified in SendGrid
- Ensure SendGrid account has sufficient quota
- Check SendGrid activity logs

#### 3. Message Processing Issues

**Problem**: Messages are not being processed
```
Error: Invalid email message format
```

**Solutions**:
- Verify message format matches required JSON schema
- Check required fields (to, subject, body) are present
- Validate email addresses are properly formatted
- Check Kafka consumer group is consuming from correct topic

### Logging Configuration

Adjust logging levels for troubleshooting:

```yaml
# application.yml
logging:
  level:
    ph.edu.cspb.kafkasendgrid: DEBUG
    org.springframework.kafka: INFO
    org.apache.kafka: WARN
```

### Monitoring Metrics

Key metrics to monitor:

- **Kafka Consumer Lag**: Monitor how far behind the consumer is
- **Message Processing Rate**: Messages processed per second
- **Email Success Rate**: Percentage of successfully sent emails
- **Error Rate**: Rate of processing errors

## Performance Tuning

### Kafka Consumer Tuning

```yaml
# Additional Kafka consumer properties for high throughput
spring:
  kafka:
    consumer:
      max-poll-records: 500
      max-poll-interval-ms: 300000
      session-timeout-ms: 30000
      heartbeat-interval-ms: 3000
      fetch-min-size: 1
      fetch-max-wait: 500
```

### Application Scaling

For high message volumes:

1. **Horizontal Scaling**: Increase replica count
2. **Resource Allocation**: Increase CPU and memory limits
3. **Consumer Parallelism**: Multiple consumer instances in same group
4. **Topic Partitioning**: Ensure topics have multiple partitions

### SendGrid Rate Limiting

Be aware of SendGrid rate limits:
- Free tier: 100 emails/day
- Paid tiers: Various limits based on plan
- Implement exponential backoff for rate limit errors

## Examples

### IBM Streams SPL Example

Here's an example of how to produce messages from IBM Streams to the kafka-sendgrid topic:

```spl
// IBM Streams SPL application example
namespace com.example.emailnotifications;

use com.ibm.streamsx.kafka::KafkaProducer;
use com.ibm.streamsx.json::*;

type EmailNotification = tuple<rstring to, rstring subject, rstring body, rstring from>;

composite EmailNotificationService {
    graph
        // Your data processing streams here
        stream<EmailNotification> ProcessedNotifications = Custom() {
            logic
                onProcess: {
                    // Example: Create email notification
                    mutable EmailNotification email = {};
                    email.to = "user@example.com";
                    email.subject = "Alert: System Notification";
                    email.body = "This is an automated notification from IBM Streams";
                    email.from = "alerts@company.com";
                    
                    submit(email, ProcessedNotifications);
                }
        }
        
        // Convert to JSON format expected by kafka-sendgrid
        stream<rstring jsonString> JsonMessages = Custom(ProcessedNotifications) {
            logic
                onTuple ProcessedNotifications: {
                    mutable rstring json = "{";
                    json += "\"to\":\"" + to + "\",";
                    json += "\"subject\":\"" + subject + "\",";
                    json += "\"body\":\"" + body + "\",";
                    json += "\"from\":\"" + from + "\"";
                    json += "}";
                    
                    submit({jsonString = json}, JsonMessages);
                }
        }
        
        // Send to Kafka
        () as KafkaSink = KafkaProducer(JsonMessages) {
            param
                topic: "sendgrid-topic";
                bootstrapServers: "your-kafka-cluster:9092";
                messageAttribute: "jsonString";
        }
}
```

### Testing Message Production

You can test the integration by producing messages to Kafka manually:

```bash
# Using kafka-console-producer
echo '{"to":"test@example.com","subject":"Test Email","body":"This is a test message"}' | \
  kafka-console-producer --broker-list localhost:9092 --topic sendgrid-topic

# Using curl with Kafka REST Proxy (if available)
curl -X POST http://kafka-rest-proxy:8082/topics/sendgrid-topic \
  -H "Content-Type: application/vnd.kafka.json.v2+json" \
  -d '{
    "records": [
      {
        "value": {
          "to": "test@example.com",
          "subject": "Test Email via REST",
          "body": "This message was sent via Kafka REST Proxy"
        }
      }
    ]
  }'
```

---

For additional support or questions about integrating with IBM Streams, please refer to the main [README.md](README.md) or open an issue in the repository.