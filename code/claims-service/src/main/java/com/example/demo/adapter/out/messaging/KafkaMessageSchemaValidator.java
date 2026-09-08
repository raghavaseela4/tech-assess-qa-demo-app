package com.example.demo.adapter.out.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class KafkaMessageSchemaValidator {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaMessageSchemaValidator.class);

    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final Map<String, JsonSchema> schemaCache = new ConcurrentHashMap<>();

    public KafkaMessageSchemaValidator(
            ObjectMapper objectMapper,
            @Value("${kafka.producer.schema-validation.enabled:true}") boolean enabled) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
    }

    public void validate(String jsonPayload, String schemaName) {
        if (!enabled) {
            LOG.info("Kafka schema validation DISABLED — skipping for {}", schemaName);
            return;
        }
        LOG.info("Kafka schema validation ENABLED — validating {}", schemaName);

        JsonSchema schema = schemaCache.computeIfAbsent(schemaName, this::loadSchema);
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonPayload);
            Set<ValidationMessage> errors = schema.validate(jsonNode);
            if (!errors.isEmpty()) {
                String errorDetails = errors.stream()
                        .map(ValidationMessage::getMessage)
                        .collect(Collectors.joining("; "));
                throw new KafkaMessageValidationException(
                        "Kafka message failed schema validation for " + schemaName + ": " + errorDetails);
            }
        } catch (IOException exception) {
            throw new KafkaMessageValidationException(
                    "Failed to parse JSON for schema validation: " + schemaName, exception);
        }
    }

    private JsonSchema loadSchema(String schemaName) {
        String schemaPath = "asyncapi/schemas/" + schemaName + ".json";
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(schemaPath)) {
            if (inputStream == null) {
                throw new KafkaMessageValidationException(
                        "Schema file not found on classpath: " + schemaPath);
            }
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
            return factory.getSchema(inputStream, config);
        } catch (IOException exception) {
            throw new KafkaMessageValidationException(
                    "Failed to load schema: " + schemaPath, exception);
        }
    }
}
