package com.badfic.philbot.data.converters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import lombok.SneakyThrows;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class JsonToSetConverter implements Converter<PGobject, Set<String>> {

    private static final TypeReference<Set<String>> SET_TYPE_REFERENCE = new TypeReference<>() {};
    private static ObjectMapper OBJECT_MAPPER;

    public JsonToSetConverter(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    @SneakyThrows
    public Set<String> convert(PGobject s) {
        if (s.getValue() == null) {
            return null;
        }
        return OBJECT_MAPPER.readValue(s.getValue(), SET_TYPE_REFERENCE);
    }
}
