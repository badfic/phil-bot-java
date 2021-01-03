package com.badfic.philbot.web;

import com.badfic.philbot.config.UnauthorizedException;
import com.github.mustachejava.Mustache;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatPopoutController extends BaseController {

    private Mustache mustache;

    @PostConstruct
    public void init() {
        mustache = mustacheFactory.compile("chat-popout.mustache");
    }

    @GetMapping(value = "/chat-popout", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> get(HttpSession httpSession) throws Exception {
        checkSession(httpSession, false);

        String discordId = (String) httpSession.getAttribute(DISCORD_ID);
        Member member = philJda.getGuilds().get(0).getMemberById(discordId);

        if (member == null) {
            throw new UnauthorizedException("You do not have a valid session. Please refresh and login again");
        }

        String userAvatar = member.getUser().getEffectiveAvatarUrl();

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "The Swamp Live Chat");
        props.put("nickname", member.getEffectiveName());
        props.put("userAvatar", userAvatar);

        try (ReusableStringWriter stringWriter = ReusableStringWriter.getCurrent()) {
            mustache.execute(stringWriter, props);
            return ResponseEntity.ok(stringWriter.toString());
        }
    }
}
