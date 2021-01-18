package com.badfic.philbot.web;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController extends BaseController {

    @Resource
    private MustacheFactory mustacheFactory;

    private Mustache mustache;

    @PostConstruct
    public void init() {
        mustache = mustacheFactory.compile("leaderboard.mustache");
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getRanks(HttpSession httpSession) throws Exception {
        checkSession(httpSession, false);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Leaderboard");
        props.put("username", httpSession.getAttribute(DISCORD_USERNAME));
        props.put("tables", Arrays.asList("bastards", "children"));
        try (ReusableStringWriter stringWriter = ReusableStringWriter.getCurrent()) {
            mustache.execute(stringWriter, props);
            return ResponseEntity.ok(stringWriter.toString());
        }
    }

    @GetMapping(value = "/bastards", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DiscordUser> getBastards(HttpSession httpSession) throws Exception {
        checkSession(httpSession, false);

        List<DiscordUser> swampyUsers = discordUserRepository.findAll();

        return swampyUsers.stream()
                .filter(user -> {
                    Member member = philJda.getGuilds().get(0).getMemberById(user.getId());
                    return member != null && hasRole(member, Constants.EIGHTEEN_PLUS_ROLE);
                })
                .peek(user -> {
                    Member member = philJda.getGuilds().get(0).getMemberById(user.getId());
                    user.setNickname(member.getEffectiveName());
                    user.setProfileUrl(member.getUser().getEffectiveAvatarUrl());
                })
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/children", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DiscordUser> getChildren(HttpSession httpSession) throws Exception {
        checkSession(httpSession, false);

        List<DiscordUser> swampyUsers = discordUserRepository.findAll();

        return swampyUsers.stream()
                .filter(user -> {
                    Member member = philJda.getGuilds().get(0).getMemberById(user.getId());
                    return member != null && hasRole(member, Constants.CHAOS_CHILDREN_ROLE);
                })
                .peek(user -> {
                    Member member = philJda.getGuilds().get(0).getMemberById(user.getId());
                    user.setNickname(member.getEffectiveName());
                    user.setProfileUrl(member.getUser().getEffectiveAvatarUrl());
                })
                .collect(Collectors.toList());
    }

}
