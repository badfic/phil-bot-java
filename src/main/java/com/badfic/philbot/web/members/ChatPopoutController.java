package com.badfic.philbot.web.members;

import com.badfic.philbot.config.UnauthorizedException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ChatPopoutController extends BaseMembersController {

    @GetMapping(value = "/chat-popout", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);

        Member member = getMemberFromSession(httpServletRequest);

        if (member == null) {
            throw new UnauthorizedException("You do not have a valid session. Please refresh and login again");
        }

        String userAvatar = member.getUser().getEffectiveAvatarUrl();

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "The Swamp Live Chat");
        props.put("nickname", member.getEffectiveName());
        props.put("userAvatar", userAvatar);

        return new ModelAndView("chat-popout", props);
    }
}