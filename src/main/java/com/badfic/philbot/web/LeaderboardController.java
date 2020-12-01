package com.badfic.philbot.web;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.github.mustachejava.Mustache;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LeaderboardController extends BaseController {

    private Mustache mustache;

    @PostConstruct
    public void init() {
        mustache = mustacheFactory.compile("leaderboard.mustache");
    }

    @GetMapping(value = "/leaderboard", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> get(HttpSession httpSession) throws Exception {
        checkSession(httpSession, false);

        return ResponseEntity.ok(leaderboard(httpSession));
    }

    private String leaderboard(HttpSession httpSession) {
        List<DiscordUser> swampyUsers = discordUserRepository.findAll();

        long dryBastards = 0;
        long dryCinnamons = 0;
        long swampyBastards = 0;
        long swampyCinnamons = 0;

        for (DiscordUser swampyUser : swampyUsers) {
            Member memberById = philJda.getGuilds().get(0).getMemberById(swampyUser.getId());

            if (memberById != null) {
                if (hasRole(memberById, Constants.DRY_BASTARDS_ROLE)) {
                    dryBastards += swampyUser.getXp();
                } else if (hasRole(memberById, Constants.DRY_CINNAMON_ROLE)) {
                    dryCinnamons += swampyUser.getXp();
                } else if (hasRole(memberById, Constants.SWAMPY_BASTARDS_ROLE)) {
                    swampyBastards += swampyUser.getXp();
                } else if (hasRole(memberById, Constants.SWAMPY_CINNAMON_ROLE)) {
                    swampyCinnamons += swampyUser.getXp();
                }
            }
        }

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Leaderboard");
        props.put("username", httpSession.getAttribute(DISCORD_USERNAME));
        props.put("dry-bastards", NumberFormat.getIntegerInstance().format(dryBastards));
        props.put("dry-cinnamon-rolls", NumberFormat.getIntegerInstance().format(dryCinnamons));
        props.put("swampy-bastards", NumberFormat.getIntegerInstance().format(swampyBastards));
        props.put("swampy-cinnamon-rolls", NumberFormat.getIntegerInstance().format(swampyCinnamons));
        try (ReusableStringWriter stringWriter = ReusableStringWriter.getCurrent()) {
            mustache.execute(stringWriter, props);
            return stringWriter.toString();
        }
    }

}
