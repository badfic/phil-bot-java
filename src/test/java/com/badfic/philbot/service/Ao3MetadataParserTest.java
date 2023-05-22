package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.SwampyGamesConfigDal;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class Ao3MetadataParserTest {

    @Mock
    private JDA philJda;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SwampyGamesConfigDal swampyGamesConfigDal;

    @InjectMocks
    private Ao3MetadataParser ao3MetadataParser;

    private String work;

    @BeforeEach
    public void setup() throws Exception {
        Mockito.doReturn(new SwampyGamesConfig()).when(swampyGamesConfigDal).get();
        new Constants(swampyGamesConfigDal, null).init();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("ao3-test.html")) {
            work = new String(Objects.requireNonNull(stream).readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    public void testParser() {
        TextChannel mockChannel = Mockito.mock(TextChannel.class);
        Mockito.doReturn(Collections.singletonList(mockChannel)).when(philJda).getTextChannelsByName(Mockito.eq(Constants.TEST_CHANNEL), Mockito.eq(false));

        ao3MetadataParser.parseWork("https://example.com", work, Constants.TEST_CHANNEL);
        Mockito.verify(mockChannel).sendMessageEmbeds(Mockito.any(MessageEmbed.class));
    }
}
