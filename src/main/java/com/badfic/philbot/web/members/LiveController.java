package com.badfic.philbot.web.members;

import com.badfic.philbot.config.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LiveController extends BaseMembersController {

    @GetMapping(value = "/live", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);

        HttpSession session = httpServletRequest.getSession();
        Member member = getMemberFromSession(session);

        if (member == null) {
            throw new UnauthorizedException("You do not have a valid session. Please refresh and login again");
        }

        String userAvatar = member.getEffectiveAvatarUrl();

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "The Swamp Live");
        addCommonProps(session, props);
        props.put("nickname", member.getEffectiveName());
        props.put("userAvatar", userAvatar);

        return new ModelAndView("live", props);
    }
}
