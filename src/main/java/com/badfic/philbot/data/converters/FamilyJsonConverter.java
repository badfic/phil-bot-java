package com.badfic.philbot.data.converters;

import com.badfic.philbot.data.Family;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.PersistenceException;
import org.springframework.stereotype.Component;

@Component
public class FamilyJsonConverter implements AttributeConverter<Family, String> {

    private static ObjectMapper OBJECT_MAPPER;

    public FamilyJsonConverter(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    public String convertToDatabaseColumn(Family family) {
        if (family == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(family);
        } catch (JsonProcessingException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Family convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(s, Family.class);
        } catch (JsonProcessingException e) {
            throw new PersistenceException(e);
        }
    }
}
