package com.badfic.philbot.data.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.PersistenceException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JsonListConverter implements AttributeConverter<List<String>, String> {

    private static ObjectMapper OBJECT_MAPPER;

    public JsonListConverter(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        try {
            if (list == null) {
                return null;
            }
            return OBJECT_MAPPER.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String s) {
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
