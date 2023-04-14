package com.badfic.philbot.data.converters;

import com.badfic.philbot.data.GenericBotResponsesConfigJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class GenericBotResponsesConfigToJsonConverter implements Converter<GenericBotResponsesConfigJson, String> {

    private static ObjectMapper OBJECT_MAPPER;

    public GenericBotResponsesConfigToJsonConverter(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    @SneakyThrows
    public String convert(GenericBotResponsesConfigJson genericBotResponsesConfigJson) {
        if (genericBotResponsesConfigJson == null) {
            return null;
        }
        return OBJECT_MAPPER.writeValueAsString(genericBotResponsesConfigJson);
    }
}
