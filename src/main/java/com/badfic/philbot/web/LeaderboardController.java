package com.badfic.philbot.web;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController extends BaseController {

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRanks(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Leaderboard");
        addCommonProps(httpServletRequest, props);
        props.put("tables", Arrays.asList("bastards", "children"));

        return new ModelAndView("leaderboard", props);
    }

    @GetMapping(value = "/bastards", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DiscordUser> getBastards(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);

        List<DiscordUser> swampyUsers = discordUserRepository.findAll();
        Guild guild = philJda.getGuilds().get(0);

        return swampyUsers.stream()
                .filter(user -> {
                    Member member = guild.getMemberById(user.getId());
                    return member != null && hasRole(member, Constants.EIGHTEEN_PLUS_ROLE);
                })
                .peek(user -> {
                    Member member = guild.getMemberById(user.getId());
                    user.setNickname(member.getEffectiveName());
                    user.setProfileUrl(member.getUser().getEffectiveAvatarUrl());
                })
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/children", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DiscordUser> getChildren(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);

        List<DiscordUser> swampyUsers = discordUserRepository.findAll();
        Guild guild = philJda.getGuilds().get(0);

        return swampyUsers.stream()
                .filter(user -> {
                    Member member = guild.getMemberById(user.getId());
                    return member != null && hasRole(member, Constants.CHAOS_CHILDREN_ROLE);
                })
                .peek(user -> {
                    Member member = guild.getMemberById(user.getId());
                    user.setNickname(member.getEffectiveName());
                    user.setProfileUrl(member.getUser().getEffectiveAvatarUrl());
                })
                .collect(Collectors.toList());
    }

}
