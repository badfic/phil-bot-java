package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.SwampyGamesConfigDal;
import java.awt.Color;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
        new Constants(swampyGamesConfigDal, null, null).init();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("ao3-test.html")) {
            work = new String(Objects.requireNonNull(stream).readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    public void testParser() {
        MessageEmbed messageEmbed = ao3MetadataParser.parseWork("https://example.com", work);
        Assertions.assertNotNull(messageEmbed);
        Assertions.assertEquals("AO3 Summary Report", messageEmbed.getTitle());
        Assertions.assertEquals("Words: 4123 | Chapters: 1/1 | Language: English", messageEmbed.getFooter().getText());
        Assertions.assertEquals("https://cdn.discordapp.com/attachments/707453916882665552/780261925212520508/wITXDY67Xw1sAAAAABJRU5ErkJggg.png", messageEmbed.getThumbnail().getUrl());
        Assertions.assertEquals(new Color(144, 7, 7), messageEmbed.getColor());
        Assertions.assertEquals("""
[**such a fool for sacrifice**](https://example.com)
by [esperanzacruz](https://archiveofourown.org/users/esperanzacruz/pseuds/esperanzacruz)

**Rating**: Explicit

**Warnings**: No Archive Warnings Apply

**Categories**: F/M

**Summary**: John sees her across the bar, a vision in a room full of nobodies he drinks with all the time. Zari notices him noticing her and decides to do something about it.

**Fandoms**: DC's Legends of Tomorrow (TV), Constantine (TV)

**Relationships**: John Constantine/Zari Tarazi

**Characters**: Zari Tomaz | Zari Tarazi, John Constantine, Book Club - Character

**Tags**: Death technique, Deep Throating, One Night Stand, Liverpool, john still does magic, i don't care that this is an au, very explicit, reupload with new edits, Choking, Rough Sex, Teasing, Edging""", messageEmbed.getDescription());
    }
}
