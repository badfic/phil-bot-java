package com.badfic.philbot.commands.bang.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.zip.ZipInputStream;
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
        final var countries = mapCommand.getCountries();

        Assertions.assertTrue(Arrays.stream(countries)
                .noneMatch(country -> StringUtils.isBlank(country.code()) || StringUtils.isBlank(country.regex())));

        final var flagzipCountryCodes = new HashSet<String>();

        try (final var stream = getClass().getClassLoader().getResourceAsStream(MapCommand.MAP_ZIP_FILENAME);
             final var zipFile = new ZipInputStream(stream)) {
            var zipEntry = zipFile.getNextEntry();
            while (zipEntry != null) {
                final var countryCode = zipEntry.getName().substring(0, zipEntry.getName().length() - 4);
                flagzipCountryCodes.add(countryCode);
                zipEntry = zipFile.getNextEntry();
            }
        }

        final var jsonCountryCodes = Arrays.stream(countries).map(MapCommand.MapTriviaObject::code).toList();
        final var disjunction = CollectionUtils.disjunction(flagzipCountryCodes, jsonCountryCodes);

        Assertions.assertTrue(CollectionUtils.isEmpty(disjunction));
    }

}
