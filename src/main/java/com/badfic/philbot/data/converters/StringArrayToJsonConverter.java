package com.badfic.philbot.data.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class StringArrayToJsonConverter implements Converter<String[], String> {

    private static ObjectMapper OBJECT_MAPPER;

    public StringArrayToJsonConverter(final ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    @SneakyThrows
    public String convert(final String[] source) {
        if (source == null) {
            return null;
        }
        return OBJECT_MAPPER.writeValueAsString(source);
    }
}
