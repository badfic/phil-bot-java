package com.badfic.philbot.data.converters;

import com.badfic.philbot.data.Family;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FamilyToJsonConverter implements Converter<Family, PGobject> {

    private static ObjectMapper OBJECT_MAPPER;

    public FamilyToJsonConverter(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    @SneakyThrows
    public PGobject convert(Family family) {
        if (family == null) {
            return null;
        }
        String value = OBJECT_MAPPER.writeValueAsString(family);
        PGobject pGobject = new PGobject();
        pGobject.setType("JSON");
        pGobject.setValue(value);
        return pGobject;
    }
}
