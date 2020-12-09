package com.badfic.philbot.web;

import com.badfic.philbot.data.phil.QuoteRepository;
import com.github.mustachejava.Mustache;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuoteController extends BaseController {

    @Resource
    private QuoteRepository quoteRepository;

    private Mustache mustache;

    @PostConstruct
    public void init() {
        mustache = mustacheFactory.compile("quotes.mustache");
    }

    @GetMapping(value = "/quotes", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> get(HttpSession httpSession) throws Exception {
        checkSession(httpSession, false);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Quotes");
        props.put("username", httpSession.getAttribute(DISCORD_USERNAME));
        props.put("quotes", quoteRepository.findAll().stream().map(q -> {
            Member memberById = philJda.getGuilds().get(0).getMemberById(q.getUserId());
            return new SimpleQuote(memberById != null ? memberById.getEffectiveName() : Long.toString(q.getUserId()),
                    q.getId(),
                    q.getQuote(),
                    q.getImage(),
                    q.getChannelId(),
                    q.getMessageId(),
                    q.getCreated().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }).collect(Collectors.toList()));

        try (ReusableStringWriter stringWriter = ReusableStringWriter.getCurrent()) {
            mustache.execute(stringWriter, props);
            return ResponseEntity.ok(stringWriter.toString());
        }
    }

    public static class SimpleQuote {
        private final String name;
        private final long id;
        private final String quote;
        private final String image;
        private final long channelId;
        private final long messageId;
        private final long timestamp;

        public SimpleQuote(String name, long id, String quote, String image, long channelId, long messageId, long timestamp) {
            this.name = name;
            this.id = id;
            this.quote = quote;
            this.image = image;
            this.channelId = channelId;
            this.messageId = messageId;
            this.timestamp = timestamp;
        }

        public String getName() {
            return name;
        }

        public long getId() {
            return id;
        }

        public String getQuote() {
            return quote;
        }

        public String getImage() {
            return image;
        }

        public long getChannelId() {
            return channelId;
        }

        public long getMessageId() {
            return messageId;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

}
