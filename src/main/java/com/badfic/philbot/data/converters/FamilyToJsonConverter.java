package com.badfic.philbot.data.converters;

import com.badfic.philbot.data.Family;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@WritingConverter
public class FamilyToJsonConverter implements Converter<Family, String> {

    private static JsonMapper JSON_MAPPER;

    public FamilyToJsonConverter(final JsonMapper jsonMapper) {
        JSON_MAPPER = jsonMapper;
    }

    @Override
    @SneakyThrows
    public String convert(final Family family) {
        if (family == null) {
            return null;
        }
        return JSON_MAPPER.writeValueAsString(family);
    }
}
