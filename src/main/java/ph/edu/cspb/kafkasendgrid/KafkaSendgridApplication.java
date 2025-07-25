package ph.edu.cspb.kafkasendgrid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Main Spring Boot application class for kafka-sendgrid service.
 * This application consumes messages from Kafka and sends emails via SendGrid.
 */
@SpringBootApplication
@EnableKafka
public class KafkaSendgridApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaSendgridApplication.class, args);
    }
}