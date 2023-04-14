package com.badfic.philbot.data.converters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.SneakyThrows;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class JsonToListConverter implements Converter<PGobject, List<String>> {

    private static final TypeReference<List<String>> LIST_TYPE_REFERENCE = new TypeReference<>() {};
    private static ObjectMapper OBJECT_MAPPER;

    public JsonToListConverter(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    @SneakyThrows
    public List<String> convert(PGobject s) {
        if (s.getValue() == null) {
            return null;
        }
        return OBJECT_MAPPER.readValue(s.getValue(), LIST_TYPE_REFERENCE);
    }
}
