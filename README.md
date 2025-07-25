# kafka-sendgrid

A Spring Boot 3.5.4 application that consumes messages from a Kafka topic and sends emails via the SendGrid API.

## Table of Contents

* [Description](#description)
* [Prerequisites](#prerequisites)
* [Getting Started](#getting-started)

  * [Clone the Repository](#clone-the-repository)
  * [Configuration](#configuration)
  * [Build and Run](#build-and-run)
* [Application Properties](#application-properties)
* [Dependencies](#dependencies)
* [Usage](#usage)
* [Logging](#logging)
* [Contributing](#contributing)
* [License](#license)

## Description

The **kafka-sendgrid** service is a lightweight Spring Boot application (version 3.5.4) that listens for messages on a specified Kafka topic and forwards the content as emails using SendGrid. This decouples email handling from other microservices and provides asynchronous, reliable email delivery.

## Prerequisites

* Java 17 or higher
* Maven 3.8+ or Gradle 7+
* Access to a running Kafka cluster
* A valid SendGrid API key

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/your-org/kafka-sendgrid.git
cd kafka-sendgrid
```

### Configuration

Copy the sample configuration file and update with your values:

```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

Edit `src/main/resources/application.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: kafka-sendgrid-group
      auto-offset-reset: earliest
    topic:
      name: sendgrid-topic

sendgrid:
  api-key: YOUR_SENDGRID_API_KEY
  from-email: no-reply@yourdomain.com
```

### Build and Run

Build the project with Maven:

```bash
mvn clean package -DskipTests
```

Run the application:

```bash
java -jar target/kafka-sendgrid-0.0.1-SNAPSHOT.jar
```

Or using Gradle:

```bash
gradle bootRun
```

## Application Properties

| Property                                  | Description                                |
| ----------------------------------------- | ------------------------------------------ |
| `spring.kafka.bootstrap-servers`          | Comma-separated list of Kafka brokers      |
| `spring.kafka.consumer.group-id`          | Consumer group ID                          |
| `spring.kafka.consumer.auto-offset-reset` | Offset reset strategy (e.g., `earliest`)   |
| `spring.kafka.topic.name`                 | Name of the Kafka topic to consume         |
| `sendgrid.api-key`                        | SendGrid API key                           |
| `sendgrid.from-email`                     | Sender email address for outgoing messages |

## Dependencies

Key dependencies declared in `pom.xml` or `build.gradle`:

* `org.springframework.boot:spring-boot-starter`
* `org.springframework.boot:spring-boot-starter-kafka`
* `com.sendgrid:sendgrid-java`
* `org.springframework.boot:spring-boot-starter-logging`

## Usage

1. Ensure Kafka is running and the configured topic exists.
2. Start the `kafka-sendgrid` application.
3. Produce messages to the `sendgrid-topic` with a JSON payload matching the expected format:

```json
{
  "to": "recipient@example.com",
  "subject": "Test Email",
  "body": "Hello from Kafka-SendGrid!"
}
```

4. The application will consume the message and send the email via SendGrid.

## Logging

Logs are output to the console by default. You can customize logging levels in `application.yml`:

```yaml
logging:
  level:
    com.yourorg.kafkasendgrid: INFO
```

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a pull request

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
