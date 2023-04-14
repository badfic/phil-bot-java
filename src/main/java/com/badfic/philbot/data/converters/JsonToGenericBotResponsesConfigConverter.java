package com.badfic.philbot.data.converters;

import com.badfic.philbot.data.GenericBotResponsesConfigJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@Component
@ReadingConverter
public class JsonToGenericBotResponsesConfigConverter implements Converter<String, GenericBotResponsesConfigJson> {

    private static ObjectMapper OBJECT_MAPPER;

    public JsonToGenericBotResponsesConfigConverter(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    @SneakyThrows
    public GenericBotResponsesConfigJson convert(String s) {
        if (s == null) {
            return null;
        }
        return OBJECT_MAPPER.readValue(s, GenericBotResponsesConfigJson.class);
    }
}
