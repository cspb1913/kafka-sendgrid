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
* [Testing](#testing)
* [Docker](#docker)
* [Kubernetes Deployment](#kubernetes-deployment)
* [CI/CD](#cicd)
* [Logging](#logging)
* [Contributing](#contributing)
* [License](#license)

## Description

The **kafka-sendgrid** service is a lightweight Spring Boot application (version 3.5.4) that listens for messages on a specified Kafka topic and forwards the content as emails using SendGrid. This decouples email handling from other microservices and provides asynchronous, reliable email delivery.

## Prerequisites

* Java 17 or higher
* Gradle 8+ (included via wrapper)
* Access to a running Kafka cluster
* A valid SendGrid API key

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/cspb1913/kafka-sendgrid.git
cd kafka-sendgrid
```

### Configuration

The application uses environment variables for configuration. Create your own configuration or use the default values:

#### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | `localhost:9092` |
| `KAFKA_CONSUMER_GROUP_ID` | Consumer group ID | `kafka-sendgrid-group` |
| `KAFKA_AUTO_OFFSET_RESET` | Offset reset strategy | `earliest` |
| `KAFKA_TOPIC_NAME` | Kafka topic to consume from | `sendgrid-topic` |
| `SENDGRID_API_KEY` | SendGrid API key | `your-sendgrid-api-key` |
| `SENDGRID_FROM_EMAIL` | Default sender email | `no-reply@yourdomain.com` |

### Build and Run

Build the project:

```bash
./gradlew build
```

Run the application:

```bash
./gradlew bootRun
```

Or run the JAR directly:

```bash
java -jar build/libs/kafka-sendgrid-1.0.0.jar
```

## Application Properties

All configuration is handled via environment variables. See the `application.yml` file for the complete configuration structure.

## Dependencies

Key dependencies:

* `org.springframework.boot:spring-boot-starter` - Core Spring Boot
* `org.springframework.boot:spring-boot-starter-web` - Web framework
* `org.springframework.boot:spring-boot-starter-actuator` - Health checks
* `org.springframework.kafka:spring-kafka` - Kafka integration
* `com.sendgrid:sendgrid-java` - SendGrid client
* `org.projectlombok:lombok` - Code generation
* `org.testng:testng` - Testing framework

## Usage

1. Ensure Kafka is running and the configured topic exists.
2. Start the `kafka-sendgrid` application.
3. Produce messages to the configured topic with a JSON payload:

```json
{
  "to": "recipient@example.com",
  "subject": "Test Email",
  "body": "Hello from Kafka-SendGrid!",
  "from": "custom@example.com"
}
```

The `from` field is optional and will use the default if not provided.

## Testing

Run tests with coverage:

```bash
./gradlew test jacocoTestReport
```

Check coverage verification:

```bash
./gradlew testCoverage
```

**Note**: Current test coverage is basic. To achieve 80% coverage as required, additional unit tests need to be added for:
- EmailService with proper SendGrid mocking
- KafkaConsumerService with comprehensive scenario testing
- Configuration classes
- Error handling scenarios

## Docker

### Build Docker Image

```bash
docker build -t kafka-sendgrid:1.0.0 .
```

### Run with Docker

```bash
docker run -e KAFKA_BOOTSTRAP_SERVERS=your-kafka:9092 \
           -e SENDGRID_API_KEY=your-api-key \
           -e SENDGRID_FROM_EMAIL=your-email@domain.com \
           kafka-sendgrid:1.0.0
```

## Kubernetes Deployment

Deploy using Helm:

```bash
# Install with default values
helm install kafka-sendgrid ./helm/kafka-sendgrid

# Install with custom values
helm install kafka-sendgrid ./helm/kafka-sendgrid \
  --set secrets.sendgridApiKey=your-api-key \
  --set config.kafka.bootstrapServers=your-kafka:9092 \
  --set config.sendgrid.fromEmail=your-email@domain.com
```

### Configuration for IBM Cloud Kubernetes Service (IKS)

The Helm chart is configured for IKS deployment with:
- Pod security contexts
- Resource limits and requests
- Health checks
- Horizontal Pod Autoscaling
- Pod Disruption Budgets

## CI/CD

The project includes a complete GitHub Actions workflow (`.github/workflows/ci-cd.yml`) that:

1. **Tests** - Runs TestNG tests and coverage verification
2. **Build** - Builds and pushes Docker images to GitHub Container Registry
3. **Deploy** - Deploys to Kubernetes using Helm
4. **Security** - Scans images for vulnerabilities

### Required Secrets

Configure these secrets in your GitHub repository:

- `KUBECONFIG` - Base64 encoded kubeconfig for your Kubernetes cluster
- `SENDGRID_API_KEY` - Your SendGrid API key
- `KAFKA_BOOTSTRAP_SERVERS` - Your Kafka cluster endpoints

### Versioning

The project uses semantic versioning with the pattern `1.0.x` where `x` is incremental:
- Push to `main` branch deploys with `latest` tag
- Tags matching `v*` (e.g., `v1.0.1`) deploy with the specified version

## Logging

Logs are configured with structured output including:
- Kafka consumer activity
- Email sending status
- Application health information

Logging levels can be adjusted via Spring Boot configuration.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Add comprehensive tests (aiming for 80%+ coverage)
4. Commit your changes (`git commit -m 'Add some feature'`)
5. Push to the branch (`git push origin feature/your-feature`)
6. Open a pull request

### Development Notes

- Use TestNG for testing (not JUnit)
- Ensure all external dependencies are mocked
- Follow Spring Boot best practices
- Add proper error handling and logging

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
