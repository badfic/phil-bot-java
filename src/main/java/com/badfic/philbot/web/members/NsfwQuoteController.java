package com.badfic.philbot.web.members;

import com.badfic.philbot.data.NsfwQuoteRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class NsfwQuoteController extends BaseMembersController {

    private final NsfwQuoteRepository nsfwQuoteRepository;

    @GetMapping(value = "/nsfw-quotes", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);

        final var guild = philJda.getGuildById(baseConfig.guildId);

        final var props = new HashMap<String, Object>();
        addCommonProps(httpServletRequest.getSession(), props);
        props.put("pageTitle", "\uD83C\uDF46 Quotes");

        props.put("quotes", nsfwQuoteRepository.findAll().stream().sorted((a, b) -> (int) (a.getId() - b.getId())).map(q -> {
            final var memberById = guild.getMemberById(q.getUserId());
            return new SimpleQuote(memberById != null ? memberById.getEffectiveName() : Long.toString(q.getUserId()),
                    q.getId(),
                    q.getQuote(),
                    q.getImage(),
                    q.getChannelId(),
                    q.getMessageId(),
                    q.getCreated().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }).toList());

        return new ModelAndView("nsfw-quotes", props);
    }

    public record SimpleQuote(String name, long id, String quote, String image, long channelId, long messageId, long timestamp) {}

}
