package com.badfic.philbot.data.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.SneakyThrows;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ListToJsonConverter implements Converter<List<String>, PGobject> {

    private static ObjectMapper OBJECT_MAPPER;

    public ListToJsonConverter(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    @SneakyThrows
    public PGobject convert(List<String> list) {
        if (list == null) {
            return null;
        }
        String value = OBJECT_MAPPER.writeValueAsString(list);
        PGobject pGobject = new PGobject();
        pGobject.setType("JSON");
        pGobject.setValue(value);
        return pGobject;
    }
}
