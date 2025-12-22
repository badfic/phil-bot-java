package com.badfic.philbot.data.converters;

import com.badfic.philbot.data.GenericBotResponsesConfigJson;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@WritingConverter
public class GenericBotResponsesConfigToJsonConverter implements Converter<GenericBotResponsesConfigJson, String> {

    private static JsonMapper JSON_MAPPER;

    public GenericBotResponsesConfigToJsonConverter(final JsonMapper jsonMapper) {
        JSON_MAPPER = jsonMapper;
    }

    @Override
    @SneakyThrows
    public String convert(final GenericBotResponsesConfigJson genericBotResponsesConfigJson) {
        if (genericBotResponsesConfigJson == null) {
            return null;
        }
        return JSON_MAPPER.writeValueAsString(genericBotResponsesConfigJson);
    }
}
