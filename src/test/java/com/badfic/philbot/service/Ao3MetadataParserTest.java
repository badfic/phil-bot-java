package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import io.honeybadger.reporter.HoneybadgerReporter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class Ao3MetadataParserTest {

    @Mock
    private JDA philJda;

    @Mock
    private HoneybadgerReporter honeybadgerReporter;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private Ao3MetadataParser ao3MetadataParser;

    private String work;

    @BeforeEach
    public void setup() throws Exception {
        work = FileUtils.readFileToString(ResourceUtils.getFile("classpath:ao3-test.html"), StandardCharsets.UTF_8);
    }

    @Test
    public void testParser() {
        TextChannel mockChannel = Mockito.mock(TextChannel.class);
        Mockito.doReturn(Collections.singletonList(mockChannel)).when(philJda).getTextChannelsByName(Mockito.eq(Constants.TEST_CHANNEL), Mockito.eq(false));

        ao3MetadataParser.parseWork("https://example.com", work, Constants.TEST_CHANNEL);
        Mockito.verify(mockChannel).sendMessageEmbeds(Mockito.any(MessageEmbed.class));
    }
}
