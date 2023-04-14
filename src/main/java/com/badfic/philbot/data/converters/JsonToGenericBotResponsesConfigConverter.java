package com.badfic.philbot.data.converters;

import com.badfic.philbot.data.GenericBotResponsesConfigJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class JsonToGenericBotResponsesConfigConverter implements Converter<PGobject, GenericBotResponsesConfigJson> {

    private static ObjectMapper OBJECT_MAPPER;

    public JsonToGenericBotResponsesConfigConverter(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    @SneakyThrows
    public GenericBotResponsesConfigJson convert(PGobject s) {
        if (s.getValue() == null) {
            return null;
        }
        return OBJECT_MAPPER.readValue(s.getValue(), GenericBotResponsesConfigJson.class);
    }
}
