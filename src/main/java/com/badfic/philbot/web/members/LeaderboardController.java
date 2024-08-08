package com.badfic.philbot.web.members;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LeaderboardController extends BaseMembersController {

    @GetMapping(value = "/leaderboard", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRanks(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);

        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Leaderboard");
        addCommonProps(httpServletRequest.getSession(), props);
        props.put("tables", Arrays.asList("bastards", "children"));

        return new ModelAndView("leaderboard", props);
    }

    @GetMapping(value = "/leaderboard/bastards", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DiscordUser> getBastards(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);

        final var swampyUsers = discordUserRepository.findAll();
        final var guild = philJda.getGuildById(baseConfig.guildId);

        return swampyUsers.stream()
                .filter(user -> {
                    final var member = guild.getMemberById(user.getId());
                    return member != null && hasRole(member, Constants.EIGHTEEN_PLUS_ROLE);
                })
                .peek(user -> {
                    final var member = guild.getMemberById(user.getId());
                    user.setNickname(member.getEffectiveName());
                    user.setProfileUrl(member.getEffectiveAvatarUrl());
                })
                .toList();
    }

    @GetMapping(value = "/leaderboard/children", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DiscordUser> getChildren(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);

        final var swampyUsers = discordUserRepository.findAll();
        final var guild = philJda.getGuildById(baseConfig.guildId);

        return swampyUsers.stream()
                .filter(user -> {
                    final var member = guild.getMemberById(user.getId());
                    return member != null && hasRole(member, Constants.CHAOS_CHILDREN_ROLE);
                })
                .peek(user -> {
                    final var member = guild.getMemberById(user.getId());
                    user.setNickname(member.getEffectiveName());
                    user.setProfileUrl(member.getEffectiveAvatarUrl());
                })
                .toList();
    }

}
