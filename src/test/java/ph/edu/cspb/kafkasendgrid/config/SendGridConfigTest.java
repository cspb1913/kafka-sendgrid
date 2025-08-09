package ph.edu.cspb.kafkasendgrid.config;

import com.sendgrid.SendGrid;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for SendGridConfig configuration class.
 */
public class SendGridConfigTest {

    private SendGridConfig sendGridConfig;

    @BeforeMethod
    public void setUp() {
        sendGridConfig = new SendGridConfig();
    }

    @Test
    public void testSendGridBean() {
        // Arrange
        ReflectionTestUtils.setField(sendGridConfig, "apiKey", "test-api-key");

        // Act
        SendGrid sendGrid = sendGridConfig.sendGrid();

        // Assert
        assertNotNull(sendGrid);
        assertTrue(sendGrid instanceof SendGrid);
    }

    @Test
    public void testSendGridBeanWithDifferentApiKeys() {
        // Test with different API keys
        String[] apiKeys = {"test-key-1", "test-key-2", "SG.test-key-3", ""};

        for (String apiKey : apiKeys) {
            // Arrange
            ReflectionTestUtils.setField(sendGridConfig, "apiKey", apiKey);

            // Act
            SendGrid sendGrid = sendGridConfig.sendGrid();

            // Assert
            assertNotNull(sendGrid);
            assertTrue(sendGrid instanceof SendGrid);
        }
    }

    @Test
    public void testSendGridBeanWithNullApiKey() {
        // Arrange
        ReflectionTestUtils.setField(sendGridConfig, "apiKey", null);

        // Act
        SendGrid sendGrid = sendGridConfig.sendGrid();

        // Assert
        assertNotNull(sendGrid);
        assertTrue(sendGrid instanceof SendGrid);
    }

    @Test
    public void testSendGridBeanReturnsNewInstance() {
        // Arrange
        ReflectionTestUtils.setField(sendGridConfig, "apiKey", "test-api-key");

        // Act
        SendGrid sendGrid1 = sendGridConfig.sendGrid();
        SendGrid sendGrid2 = sendGridConfig.sendGrid();

        // Assert
        assertNotNull(sendGrid1);
        assertNotNull(sendGrid2);
        // Different instances should be returned (not singleton within this config method)
        assertNotSame(sendGrid1, sendGrid2);
    }

    @Test
    public void testSendGridConfigInstantiation() {
        // Act
        SendGridConfig config = new SendGridConfig();

        // Assert
        assertNotNull(config);
    }

    @Test
    public void testSendGridBeanWithValidApiKeyFormat() {
        // Arrange - Test with SendGrid-like API key format
        String validApiKey = "SG.abcdefghijklmnopqrstuvwxyz.1234567890abcdefghijklmnopqrstuvwxyz";
        ReflectionTestUtils.setField(sendGridConfig, "apiKey", validApiKey);

        // Act
        SendGrid sendGrid = sendGridConfig.sendGrid();

        // Assert
        assertNotNull(sendGrid);
        assertTrue(sendGrid instanceof SendGrid);
    }

    @Test
    public void testSendGridBeanWithSpecialCharacters() {
        // Arrange - Test with API key containing special characters
        String apiKeyWithSpecialChars = "SG.test_key-with.special@chars#123";
        ReflectionTestUtils.setField(sendGridConfig, "apiKey", apiKeyWithSpecialChars);

        // Act
        SendGrid sendGrid = sendGridConfig.sendGrid();

        // Assert
        assertNotNull(sendGrid);
        assertTrue(sendGrid instanceof SendGrid);
    }

    @Test
    public void testSendGridBeanWithLongApiKey() {
        // Arrange - Test with very long API key
        StringBuilder longApiKey = new StringBuilder("SG.");
        for (int i = 0; i < 100; i++) {
            longApiKey.append("a");
        }
        ReflectionTestUtils.setField(sendGridConfig, "apiKey", longApiKey.toString());

        // Act
        SendGrid sendGrid = sendGridConfig.sendGrid();

        // Assert
        assertNotNull(sendGrid);
        assertTrue(sendGrid instanceof SendGrid);
    }

    @Test
    public void testSendGridBeanMultipleInstantiations() {
        // Arrange
        ReflectionTestUtils.setField(sendGridConfig, "apiKey", "test-api-key");

        // Act & Assert - Multiple calls should not throw exceptions
        for (int i = 0; i < 5; i++) {
            SendGrid sendGrid = sendGridConfig.sendGrid();
            assertNotNull(sendGrid);
            assertTrue(sendGrid instanceof SendGrid);
        }
    }
}