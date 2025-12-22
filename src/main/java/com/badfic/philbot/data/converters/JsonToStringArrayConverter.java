package com.badfic.philbot.data.converters;

import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@ReadingConverter
public class JsonToStringArrayConverter implements Converter<String, String[]> {

    private static JsonMapper JSON_MAPPER;

    public JsonToStringArrayConverter(final JsonMapper jsonMapper) {
        JSON_MAPPER = jsonMapper;
    }

    @Override
    @SneakyThrows
    public String[] convert(final String source) {
        if (source == null) {
            return null;
        }
        return JSON_MAPPER.readValue(source, String[].class);
    }
}
