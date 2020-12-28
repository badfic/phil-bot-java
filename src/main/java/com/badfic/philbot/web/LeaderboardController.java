package com.badfic.philbot.web;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.text.NumberFormat;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LeaderboardController extends BaseController {

    @Resource
    private MustacheFactory mustacheFactory;

    private Mustache mustache;

    @PostConstruct
    public void init() {
        mustache = mustacheFactory.compile("leaderboard.mustache");
    }

    @GetMapping(value = "/leaderboard", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getRanks(HttpSession httpSession) throws Exception {
        checkSession(httpSession, false);

        return ResponseEntity.ok(leaderboard(httpSession));
    }

    private String leaderboard(HttpSession httpSession) {
        List<DiscordUser> swampyUsers = discordUserRepository.findAll();

        List<SimpleMember> bastards = swampyUsers.stream()
                .sorted((u1, u2) -> Long.compare(u2.getXp(), u1.getXp()))
                .filter(user -> {
                    Member member = philJda.getGuilds().get(0).getMemberById(user.getId());
                    return member != null && hasRole(member, Constants.EIGHTEEN_PLUS_ROLE);
                })
                .map(user -> {
                    Member member = philJda.getGuilds().get(0).getMemberById(user.getId());
                    return new SimpleMember(member.getUser().getEffectiveAvatarUrl(), member.getEffectiveName(), member.getId(),
                            NumberFormat.getIntegerInstance().format(user.getXp()));
                })
                .collect(Collectors.toList());

        List<SimpleMember> children = swampyUsers.stream()
                .sorted((u1, u2) -> Long.compare(u2.getXp(), u1.getXp()))
                .filter(user -> {
                    Member member = philJda.getGuilds().get(0).getMemberById(user.getId());
                    return member != null && hasRole(member, Constants.CHAOS_CHILDREN_ROLE);
                })
                .map(user -> {
                    Member member = philJda.getGuilds().get(0).getMemberById(user.getId());
                    return new SimpleMember(member.getUser().getEffectiveAvatarUrl(), member.getEffectiveName(), member.getId(),
                            NumberFormat.getIntegerInstance().format(user.getXp()));
                })
                .collect(Collectors.toList());

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Leaderboard");
        props.put("username", httpSession.getAttribute(DISCORD_USERNAME));
        props.put("bastards", bastards);
        props.put("children", children);
        try (ReusableStringWriter stringWriter = ReusableStringWriter.getCurrent()) {
            mustache.execute(stringWriter, props);
            return stringWriter.toString();
        }
    }

    public static class SimpleMember {
        private final String image;
        private final String name;
        private final String id;
        private final String xp;

        public SimpleMember(String image, String name, String id, String xp) {
            this.image = image;
            this.name = name;
            this.id = id;
            this.xp = xp;
        }

        public String getImage() {
            return image;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public String getXp() {
            return xp;
        }
    }
}
