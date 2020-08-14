package com.badfic.philbot.data;

import com.badfic.philbot.model.PhilConfigJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.AttributeConverter;
import javax.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PhilConfigJsonConverter implements AttributeConverter<PhilConfigJson, String> {

    private static ObjectMapper OBJECT_MAPPER;

    @Autowired
    public PhilConfigJsonConverter(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    public String convertToDatabaseColumn(PhilConfigJson philConfigJson) {
        try {
            return OBJECT_MAPPER.writeValueAsString(philConfigJson);
        } catch (JsonProcessingException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public PhilConfigJson convertToEntityAttribute(String s) {
        try {
            return OBJECT_MAPPER.readValue(s, PhilConfigJson.class);
        } catch (JsonProcessingException e) {
            throw new PersistenceException(e);
        }
    }
}
