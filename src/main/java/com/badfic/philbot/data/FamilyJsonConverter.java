package com.badfic.philbot.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.AttributeConverter;
import javax.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FamilyJsonConverter implements AttributeConverter<Family, String> {

    private static ObjectMapper OBJECT_MAPPER;

    @Autowired
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
