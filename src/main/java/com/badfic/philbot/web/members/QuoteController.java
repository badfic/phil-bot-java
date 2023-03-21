package com.badfic.philbot.web.members;

import com.badfic.philbot.data.phil.QuoteRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class QuoteController extends BaseMembersController {

    private final QuoteRepository quoteRepository;

    public QuoteController(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    @GetMapping(value = "/quotes", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);
        Guild guild = philJda.getGuildById(baseConfig.guildId);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "\uD83D\uDCAC Quotes");
        addCommonProps(httpServletRequest, props);
        props.put("quotes", quoteRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream().map(q -> {
            Member memberById = guild.getMemberById(q.getUserId());
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

    public record SimpleQuote(String name, long id, String quote, String image, long channelId, long messageId, long timestamp) {}

}
