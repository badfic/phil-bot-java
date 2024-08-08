package com.badfic.philbot.data.converters;

import com.badfic.philbot.data.Family;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class FamilyToJsonConverter implements Converter<Family, String> {

    private static ObjectMapper OBJECT_MAPPER;

    public FamilyToJsonConverter(final ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    @SneakyThrows
    public String convert(final Family family) {
        if (family == null) {
            return null;
        }
        return OBJECT_MAPPER.writeValueAsString(family);
    }
}
