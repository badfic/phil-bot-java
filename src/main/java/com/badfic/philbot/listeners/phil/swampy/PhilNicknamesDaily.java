package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.service.DailyTickable;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PhilNicknamesDaily extends BaseSwampy implements DailyTickable {
    public PhilNicknamesDaily() {
        name = "philNicknames";
        requiredRole = Constants.ADMIN_ROLE;
    }

    @Override
    public void runDailyTask() {
        for (Member member : philJda.getGuilds().get(0).getMembers()) {
            if (member.getUser().isBot() || hasRole(member, Constants.ADMIN_ROLE)) {
                continue;
            }

            String effectiveName = member.getEffectiveName();

            if (!StringUtils.containsIgnoreCase(effectiveName, "phil")) {
                member.modifyNickname(effectiveName + " ♥ Phil").queue();
            }
        }

        Constants.debugToTestChannel(philJda, "Successfully updated all nicknames to include Phil \uD83D\uDE08");
    }

    @Override
    protected void execute(CommandEvent event) {
        runDailyTask();
    }
}
