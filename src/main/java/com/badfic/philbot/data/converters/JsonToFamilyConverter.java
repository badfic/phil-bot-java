package com.badfic.philbot.data.converters;

import com.badfic.philbot.data.Family;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class JsonToFamilyConverter implements Converter<PGobject, Family> {

    private static ObjectMapper OBJECT_MAPPER;

    public JsonToFamilyConverter(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    @SneakyThrows
    public Family convert(PGobject s) {
        if (s.getValue() == null) {
            return null;
        }
        return OBJECT_MAPPER.readValue(s.getValue(), Family.class);
    }
}
