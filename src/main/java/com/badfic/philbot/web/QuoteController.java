package com.badfic.philbot.web;

import com.badfic.philbot.data.phil.QuoteRepository;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class QuoteController extends BaseController {

    @Resource
    private QuoteRepository quoteRepository;

    @GetMapping(value = "/quotes", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Quotes");
        props.put("username", httpServletRequest.getSession().getAttribute(DISCORD_USERNAME));
        props.put("isMod", httpServletRequest.getSession().getAttribute(DISCORD_IS_MOD));
        props.put("quotes", quoteRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream().map(q -> {
            Member memberById = philJda.getGuilds().get(0).getMemberById(q.getUserId());
            return new SimpleQuote(memberById != null ? memberById.getEffectiveName() : Long.toString(q.getUserId()),
                    q.getId(),
                    q.getQuote(),
                    q.getImage(),
                    q.getChannelId(),
                    q.getMessageId(),
                    q.getCreated().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }).collect(Collectors.toList()));

        return new ModelAndView("quotes", props);
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
