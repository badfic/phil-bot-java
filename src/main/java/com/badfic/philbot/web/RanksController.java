package com.badfic.philbot.web;

import com.badfic.philbot.data.phil.Rank;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RanksController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Resource
    private MustacheFactory mustacheFactory;

    private Mustache mustache;

    @PostConstruct
    public void init() {
        mustache = mustacheFactory.compile("ranks.mustache");
    }

    @GetMapping(value = "/ranks", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getRanks(HttpSession httpSession) {
        try {
            if (httpSession.isNew()) {
                return redirectDiscordLogin();
            }
            if (httpSession.getAttribute(DISCORD_TOKEN) == null) {
                httpSession.invalidate();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You do not have a valid session. Please refresh and try again");
            }
            refreshTokenIfNeeded(httpSession);

            Map<String, Object> props = new HashMap<>();
            props.put("pageTitle", "Ranks");
            props.put("ranks", Rank.getAllRanks());

            try (ReusableStringWriter stringWriter = ReusableStringWriter.getCurrent()) {
                mustache.execute(stringWriter, props);
                return ResponseEntity.ok(stringWriter.toString());
            }
        } catch (Exception e) {
            logger.error("Exception in get ranks controller", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("You are either unauthorized or something broke, ask Santiago");
        }
    }
}
