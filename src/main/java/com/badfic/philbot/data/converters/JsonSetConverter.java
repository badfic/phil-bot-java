package com.badfic.philbot.data.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.PersistenceException;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class JsonSetConverter implements AttributeConverter<Set<String>, String> {

    private static ObjectMapper OBJECT_MAPPER;

    public JsonSetConverter(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    public String convertToDatabaseColumn(Set<String> set) {
        try {
            if (set == null) {
                return null;
            }
            return OBJECT_MAPPER.writeValueAsString(set);
        } catch (JsonProcessingException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Set<String> convertToEntityAttribute(String s) {
        try {
            if (s == null) {
                return null;
            }
            return OBJECT_MAPPER.readValue(s, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new PersistenceException(e);
        }
    }
}
