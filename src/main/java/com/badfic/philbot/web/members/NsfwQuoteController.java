package com.badfic.philbot.web.members;

import com.badfic.philbot.data.phil.NsfwQuoteRepository;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class NsfwQuoteController extends BaseMembersController {

    @Resource
    private NsfwQuoteRepository nsfwQuoteRepository;

    @GetMapping(value = "/nsfw-quotes", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);

        Guild guild = philJda.getGuilds().get(0);

        Map<String, Object> props = new HashMap<>();
        addCommonProps(httpServletRequest, props);
        props.put("pageTitle", "\uD83C\uDF46 Quotes");

        props.put("quotes", nsfwQuoteRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream().map(q -> {
            Member memberById = guild.getMemberById(q.getUserId());
            return new SimpleQuote(memberById != null ? memberById.getEffectiveName() : Long.toString(q.getUserId()),
                    q.getId(),
                    q.getQuote(),
                    q.getImage(),
                    q.getChannelId(),
                    q.getMessageId(),
                    q.getCreated().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }).collect(Collectors.toList()));

        return new ModelAndView("nsfw-quotes", props);
    }

    public record SimpleQuote(String name, long id, String quote, String image, long channelId, long messageId, long timestamp) {}

}
