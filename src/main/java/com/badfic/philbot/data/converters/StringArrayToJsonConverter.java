package com.badfic.philbot.data.converters;

import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@WritingConverter
public class StringArrayToJsonConverter implements Converter<String[], String> {

    private static JsonMapper JSON_MAPPER;

    public StringArrayToJsonConverter(final JsonMapper jsonMapper) {
        JSON_MAPPER = jsonMapper;
    }

    @Override
    @SneakyThrows
    public String convert(final String[] source) {
        if (source == null) {
            return null;
        }
        return JSON_MAPPER.writeValueAsString(source);
    }
}
