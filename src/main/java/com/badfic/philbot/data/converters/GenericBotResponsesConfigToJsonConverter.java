package com.badfic.philbot.data.converters;

import com.badfic.philbot.data.GenericBotResponsesConfigJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class GenericBotResponsesConfigToJsonConverter implements Converter<GenericBotResponsesConfigJson, PGobject> {

    private static ObjectMapper OBJECT_MAPPER;

    public GenericBotResponsesConfigToJsonConverter(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    @SneakyThrows
    public PGobject convert(GenericBotResponsesConfigJson genericBotResponsesConfigJson) {
        String value = OBJECT_MAPPER.writeValueAsString(genericBotResponsesConfigJson);
        PGobject pGobject = new PGobject();
        pGobject.setType("JSON");
        pGobject.setValue(value);
        return pGobject;
    }
}
