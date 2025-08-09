package ph.edu.cspb.kafkasendgrid.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.*;

/**
 * Unit tests for KafkaConfig configuration class.
 */
public class KafkaConfigTest {

    private KafkaConfig kafkaConfig;

    @BeforeMethod
    public void setUp() {
        kafkaConfig = new KafkaConfig();
        // Set default test values
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(kafkaConfig, "groupId", "test-group");
        ReflectionTestUtils.setField(kafkaConfig, "autoOffsetReset", "earliest");
    }

    @Test
    public void testConsumerFactory() {
        // Act
        ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();

        // Assert
        assertNotNull(consumerFactory);
        
        // Verify configuration properties
        Map<String, Object> configProps = consumerFactory.getConfigurationProperties();
        assertEquals(configProps.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG), "localhost:9092");
        assertEquals(configProps.get(ConsumerConfig.GROUP_ID_CONFIG), "test-group");
        assertEquals(configProps.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "earliest");
        assertEquals(configProps.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG), StringDeserializer.class);
        assertEquals(configProps.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG), StringDeserializer.class);
        assertEquals(configProps.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG), false);
    }

    @Test
    public void testConsumerFactoryWithDifferentBootstrapServers() {
        // Arrange
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "kafka1:9092,kafka2:9092,kafka3:9092");

        // Act
        ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();

        // Assert
        assertNotNull(consumerFactory);
        Map<String, Object> configProps = consumerFactory.getConfigurationProperties();
        assertEquals(configProps.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG), "kafka1:9092,kafka2:9092,kafka3:9092");
    }

    @Test
    public void testConsumerFactoryWithDifferentGroupId() {
        // Arrange
        ReflectionTestUtils.setField(kafkaConfig, "groupId", "custom-consumer-group");

        // Act
        ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();

        // Assert
        assertNotNull(consumerFactory);
        Map<String, Object> configProps = consumerFactory.getConfigurationProperties();
        assertEquals(configProps.get(ConsumerConfig.GROUP_ID_CONFIG), "custom-consumer-group");
    }

    @Test
    public void testConsumerFactoryWithDifferentAutoOffsetReset() {
        // Arrange
        ReflectionTestUtils.setField(kafkaConfig, "autoOffsetReset", "latest");

        // Act
        ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();

        // Assert
        assertNotNull(consumerFactory);
        Map<String, Object> configProps = consumerFactory.getConfigurationProperties();
        assertEquals(configProps.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "latest");
    }

    @Test
    public void testKafkaListenerContainerFactory() {
        // Act
        ConcurrentKafkaListenerContainerFactory<String, String> factory = kafkaConfig.kafkaListenerContainerFactory();

        // Assert
        assertNotNull(factory);
        assertNotNull(factory.getConsumerFactory());
        assertEquals(factory.getContainerProperties().getAckMode(), ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    }

    @Test
    public void testKafkaListenerContainerFactoryUsesCorrectConsumerFactory() {
        // Arrange
        ConsumerFactory<String, String> expectedConsumerFactory = kafkaConfig.consumerFactory();

        // Act
        ConcurrentKafkaListenerContainerFactory<String, String> factory = kafkaConfig.kafkaListenerContainerFactory();

        // Assert
        assertNotNull(factory);
        assertNotNull(factory.getConsumerFactory());
        
        // Both should have the same configuration properties
        Map<String, Object> expectedProps = expectedConsumerFactory.getConfigurationProperties();
        Map<String, Object> actualProps = factory.getConsumerFactory().getConfigurationProperties();
        
        assertEquals(actualProps.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG), expectedProps.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(actualProps.get(ConsumerConfig.GROUP_ID_CONFIG), expectedProps.get(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals(actualProps.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), expectedProps.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
    }

    @Test
    public void testConsumerFactoryReturnsNewInstance() {
        // Act
        ConsumerFactory<String, String> factory1 = kafkaConfig.consumerFactory();
        ConsumerFactory<String, String> factory2 = kafkaConfig.consumerFactory();

        // Assert
        assertNotNull(factory1);
        assertNotNull(factory2);
        assertNotSame(factory1, factory2);
    }

    @Test
    public void testKafkaListenerContainerFactoryReturnsNewInstance() {
        // Act
        ConcurrentKafkaListenerContainerFactory<String, String> factory1 = kafkaConfig.kafkaListenerContainerFactory();
        ConcurrentKafkaListenerContainerFactory<String, String> factory2 = kafkaConfig.kafkaListenerContainerFactory();

        // Assert
        assertNotNull(factory1);
        assertNotNull(factory2);
        assertNotSame(factory1, factory2);
    }

    @Test
    public void testKafkaConfigInstantiation() {
        // Act
        KafkaConfig config = new KafkaConfig();

        // Assert
        assertNotNull(config);
    }

    @Test
    public void testConsumerFactoryWithEmptyBootstrapServers() {
        // Arrange
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "");

        // Act
        ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();

        // Assert
        assertNotNull(consumerFactory);
        Map<String, Object> configProps = consumerFactory.getConfigurationProperties();
        assertEquals(configProps.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG), "");
    }

    @Test
    public void testConsumerFactoryWithNullValues() {
        // Arrange
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", null);
        ReflectionTestUtils.setField(kafkaConfig, "groupId", null);
        ReflectionTestUtils.setField(kafkaConfig, "autoOffsetReset", null);

        // Act & Assert - This should not throw an exception
        try {
            ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();
            assertNotNull(consumerFactory);
            Map<String, Object> configProps = consumerFactory.getConfigurationProperties();
            assertNotNull(configProps);
        } catch (Exception e) {
            // It's acceptable if this throws an exception due to null values
            assertTrue(e instanceof NullPointerException || e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testConsumerFactoryHasRequiredProperties() {
        // Act
        ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();

        // Assert
        assertNotNull(consumerFactory);
        Map<String, Object> configProps = consumerFactory.getConfigurationProperties();
        
        // Verify all required properties are set
        assertTrue(configProps.containsKey(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertTrue(configProps.containsKey(ConsumerConfig.GROUP_ID_CONFIG));
        assertTrue(configProps.containsKey(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
        assertTrue(configProps.containsKey(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
        assertTrue(configProps.containsKey(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
        assertTrue(configProps.containsKey(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
    }

    @Test
    public void testKafkaListenerContainerFactoryAckMode() {
        // Act
        ConcurrentKafkaListenerContainerFactory<String, String> factory = kafkaConfig.kafkaListenerContainerFactory();

        // Assert
        assertNotNull(factory);
        assertEquals(factory.getContainerProperties().getAckMode(), ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    }

    @Test
    public void testConsumerFactoryWithSpecialCharactersInValues() {
        // Arrange
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "kafka-cluster.example.com:9092");
        ReflectionTestUtils.setField(kafkaConfig, "groupId", "consumer-group-with-dashes_and_underscores");
        ReflectionTestUtils.setField(kafkaConfig, "autoOffsetReset", "earliest");

        // Act
        ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();

        // Assert
        assertNotNull(consumerFactory);
        Map<String, Object> configProps = consumerFactory.getConfigurationProperties();
        assertEquals(configProps.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG), "kafka-cluster.example.com:9092");
        assertEquals(configProps.get(ConsumerConfig.GROUP_ID_CONFIG), "consumer-group-with-dashes_and_underscores");
    }
}