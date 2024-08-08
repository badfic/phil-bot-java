package com.badfic.philbot.data.converters;

import com.badfic.philbot.data.Family;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@Component
@ReadingConverter
public class JsonToFamilyConverter implements Converter<String, Family> {

    private static ObjectMapper OBJECT_MAPPER;

    public JsonToFamilyConverter(final ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    @SneakyThrows
    public Family convert(final String s) {
        if (s == null) {
            return null;
        }
        return OBJECT_MAPPER.readValue(s, Family.class);
    }
}
