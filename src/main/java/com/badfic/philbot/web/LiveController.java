package com.badfic.philbot.web;

import com.badfic.philbot.config.UnauthorizedException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class LiveController extends BaseController {

    @GetMapping(value = "/live", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpSession httpSession) throws Exception {
        checkSession(httpSession, false);

        String discordId = (String) httpSession.getAttribute(DISCORD_ID);
        Member member = philJda.getGuilds().get(0).getMemberById(discordId);

        if (member == null) {
            throw new UnauthorizedException("You do not have a valid session. Please refresh and login again");
        }

        String userAvatar = member.getUser().getEffectiveAvatarUrl();

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "The Swamp Live");
        props.put("username", httpSession.getAttribute(DISCORD_USERNAME));
        props.put("nickname", member.getEffectiveName());
        props.put("userAvatar", userAvatar);

        return new ModelAndView("live", props);
    }
}
