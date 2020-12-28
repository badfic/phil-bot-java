package com.badfic.philbot.web;

import com.github.mustachejava.Mustache;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import net.dv8tion.jda.api.entities.User;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LiveController extends BaseController {

    private Mustache mustache;

    @PostConstruct
    public void init() {
        mustache = mustacheFactory.compile("live.mustache");
    }

    @GetMapping(value = "/live", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> get(HttpSession httpSession) throws Exception {
        checkSession(httpSession, false);

        String discordId = (String) httpSession.getAttribute(DISCORD_ID);
        User member = Optional.ofNullable(philJda.getUserById(discordId))
                .orElseGet(() -> philJda.getUserById(philJda.getSelfUser().getIdLong()));
        String userAvatar = member.getEffectiveAvatarUrl();

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "The Swamp Live");
        props.put("username", httpSession.getAttribute(DISCORD_USERNAME));
        props.put("userAvatar", userAvatar);
        props.put("hlsStreamLocation", baseConfig.hlsStreamLocation);

        try (ReusableStringWriter stringWriter = ReusableStringWriter.getCurrent()) {
            mustache.execute(stringWriter, props);
            return ResponseEntity.ok(stringWriter.toString());
        }
    }
}
