package com.badfic.philbot.data.converters;

import com.badfic.philbot.data.GenericBotResponsesConfigJson;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@ReadingConverter
public class JsonToGenericBotResponsesConfigConverter implements Converter<String, GenericBotResponsesConfigJson> {

    private static JsonMapper JSON_MAPPER;

    public JsonToGenericBotResponsesConfigConverter(final JsonMapper jsonMapper) {
        JSON_MAPPER = jsonMapper;
    }

    @Override
    @SneakyThrows
    public GenericBotResponsesConfigJson convert(final String s) {
        if (s == null) {
            return null;
        }
        return JSON_MAPPER.readValue(s, GenericBotResponsesConfigJson.class);
    }
}
