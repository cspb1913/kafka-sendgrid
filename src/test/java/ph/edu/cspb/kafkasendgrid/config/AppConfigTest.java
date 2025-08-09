package ph.edu.cspb.kafkasendgrid.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.validation.Validator;

import static org.testng.Assert.*;

/**
 * Unit tests for AppConfig configuration class.
 */
public class AppConfigTest {

    private AppConfig appConfig;

    @BeforeMethod
    public void setUp() {
        appConfig = new AppConfig();
    }

    @Test
    public void testObjectMapperBean() {
        // Act
        ObjectMapper objectMapper = appConfig.objectMapper();

        // Assert
        assertNotNull(objectMapper);
        assertTrue(objectMapper instanceof ObjectMapper);
    }

    @Test
    public void testObjectMapperBeanIsFunctional() throws Exception {
        // Arrange
        ObjectMapper objectMapper = appConfig.objectMapper();

        // Act
        String json = objectMapper.writeValueAsString("test");
        String result = objectMapper.readValue(json, String.class);

        // Assert
        assertEquals(result, "test");
    }

    @Test
    public void testObjectMapperBeanReturnsNewInstance() {
        // Act
        ObjectMapper objectMapper1 = appConfig.objectMapper();
        ObjectMapper objectMapper2 = appConfig.objectMapper();

        // Assert
        assertNotNull(objectMapper1);
        assertNotNull(objectMapper2);
        // Different instances should be returned (not singleton within this config method)
        assertNotSame(objectMapper1, objectMapper2);
    }

    @Test
    public void testValidatorBean() {
        // Act
        Validator validator = appConfig.validator();

        // Assert
        assertNotNull(validator);
        assertTrue(validator instanceof Validator);
    }

    @Test
    public void testValidatorBeanIsFunctional() {
        // Arrange
        Validator validator = appConfig.validator();

        // Act & Assert
        assertNotNull(validator.getConstraintsForClass(Object.class));
    }

    @Test
    public void testValidatorBeanReturnsNewInstance() {
        // Act
        Validator validator1 = appConfig.validator();
        Validator validator2 = appConfig.validator();

        // Assert
        assertNotNull(validator1);
        assertNotNull(validator2);
        // Different instances should be returned (not singleton within this config method)
        assertNotSame(validator1, validator2);
    }

    @Test
    public void testAppConfigInstantiation() {
        // Act
        AppConfig config = new AppConfig();

        // Assert
        assertNotNull(config);
        
        // Verify all bean methods work
        assertNotNull(config.objectMapper());
        assertNotNull(config.validator());
    }

    @Test
    public void testObjectMapperConfiguration() throws Exception {
        // Arrange
        ObjectMapper objectMapper = appConfig.objectMapper();

        // Test with complex object
        TestObject testObj = new TestObject("test", 123);

        // Act
        String json = objectMapper.writeValueAsString(testObj);
        TestObject deserializedObj = objectMapper.readValue(json, TestObject.class);

        // Assert
        assertEquals(deserializedObj.getName(), testObj.getName());
        assertEquals(deserializedObj.getValue(), testObj.getValue());
    }

    @Test
    public void testObjectMapperHandlesNull() throws Exception {
        // Arrange
        ObjectMapper objectMapper = appConfig.objectMapper();

        // Act
        String json = objectMapper.writeValueAsString(null);
        Object result = objectMapper.readValue(json, Object.class);

        // Assert
        assertEquals(json, "null");
        assertNull(result);
    }

    // Helper class for testing ObjectMapper
    public static class TestObject {
        private String name;
        private int value;

        public TestObject() {}

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}