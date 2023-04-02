package com.badfic.philbot.commands.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MapCommandTest {

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private MapCommand mapCommand;

    @Test
    public void testInit() throws Exception {
        mapCommand.init();
        List<MapCommand.MapTriviaObject> countries = mapCommand.getCountries();

        Assertions.assertTrue(countries.stream()
                .noneMatch(country -> StringUtils.isBlank(country.code()) || StringUtils.isBlank(country.capital()) || StringUtils.isBlank(country.regex())));

        Set<String> flagzipCountryCodes = new HashSet<>();

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(MapCommand.MAP_ZIP_FILENAME);
             ZipInputStream zipFile = new ZipInputStream(stream)) {
            LocalFileHeader fileHeader;
            while ((fileHeader = zipFile.getNextEntry()) != null) {
                String countryCode = fileHeader.getFileName().substring(0, fileHeader.getFileName().length() - 4);
                flagzipCountryCodes.add(countryCode);
            }
        }

        List<String> jsonCountryCodes = countries.stream().map(MapCommand.MapTriviaObject::code).collect(Collectors.toList());
        Collection<String> disjunction = CollectionUtils.disjunction(flagzipCountryCodes, jsonCountryCodes);

        Assertions.assertTrue(CollectionUtils.isEmpty(disjunction));
    }

}
