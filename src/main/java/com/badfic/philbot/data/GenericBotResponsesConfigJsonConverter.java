package com.badfic.philbot.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.PersistenceException;
import org.springframework.stereotype.Component;

@Component
public class GenericBotResponsesConfigJsonConverter implements AttributeConverter<GenericBotResponsesConfigJson, String> {

    private static ObjectMapper OBJECT_MAPPER;

    public GenericBotResponsesConfigJsonConverter(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    public String convertToDatabaseColumn(GenericBotResponsesConfigJson genericBotResponsesConfigJson) {
        try {
            return OBJECT_MAPPER.writeValueAsString(genericBotResponsesConfigJson);
        } catch (JsonProcessingException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public GenericBotResponsesConfigJson convertToEntityAttribute(String s) {
        try {
            return OBJECT_MAPPER.readValue(s, GenericBotResponsesConfigJson.class);
        } catch (JsonProcessingException e) {
            throw new PersistenceException(e);
        }
    }
}
