package com.badfic.philbot.data.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import lombok.SneakyThrows;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SetToJsonConverter implements Converter<Set<String>, PGobject> {

    private static ObjectMapper OBJECT_MAPPER;

    public SetToJsonConverter(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    @SneakyThrows
    public PGobject convert(Set<String> set) {
        if (set == null) {
            return null;
        }
        String value = OBJECT_MAPPER.writeValueAsString(set);
        PGobject pGobject = new PGobject();
        pGobject.setType("JSON");
        pGobject.setValue(value);
        return pGobject;
    }
}
