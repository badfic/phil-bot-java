package com.badfic.philbot.data.converters;

import com.badfic.philbot.data.Family;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@ReadingConverter
public class JsonToFamilyConverter implements Converter<String, Family> {

    private static JsonMapper JSON_MAPPER;

    public JsonToFamilyConverter(final JsonMapper jsonMapper) {
        JSON_MAPPER = jsonMapper;
    }

    @Override
    @SneakyThrows
    public Family convert(final String s) {
        if (s == null) {
            return null;
        }
        return JSON_MAPPER.readValue(s, Family.class);
    }
}
