package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Shrekoning extends BaseSwampy implements PhilMarker {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public Shrekoning() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "shrekoning";
        help = "!!shrekoning\nTrigger a shrekoning for non-top-3 chaos children";
    }

    @Override
    public void execute(CommandEvent event) {
        shrekoning();
    }

    @Scheduled(cron = "0 47 4 * * ?", zone = "GMT")
    public void shrekoning() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        if (swampyGamesConfig == null) {
            return;
        }

        List<DiscordUser> swampyUsers = discordUserRepository.findAll();
        long dryBastards = 0;
        long dryCinnamons = 0;
        long swampyBastards = 0;
        long swampyCinnamons = 0;

        for (DiscordUser swampyUser : swampyUsers) {
            Member memberById = philJda.getGuilds().get(0).getMemberById(swampyUser.getId());

            if (memberById != null && !isNotParticipating(memberById)) {
                if (hasRole(memberById, Constants.DRY_BASTARDS_ROLE)) {
                    dryBastards += swampyUser.getXp();
                }
                if (hasRole(memberById, Constants.DRY_CINNAMON_ROLE)) {
                    dryCinnamons += swampyUser.getXp();
                }
                if (hasRole(memberById, Constants.SWAMPY_BASTARDS_ROLE)) {
                    swampyBastards += swampyUser.getXp();
                }
                if (hasRole(memberById, Constants.SWAMPY_CINNAMON_ROLE)) {
                    swampyCinnamons += swampyUser.getXp();
                }
            }
        }

        String losingRole;
        if (dryBastards <= dryCinnamons && dryBastards <= swampyBastards && dryBastards <= swampyCinnamons) {
            losingRole = Constants.DRY_BASTARDS_ROLE;
        } else if (dryCinnamons <= dryBastards && dryCinnamons <= swampyBastards && dryCinnamons <= swampyCinnamons) {
            losingRole = Constants.DRY_CINNAMON_ROLE;
        } else if (swampyBastards <= dryBastards && swampyBastards <= dryCinnamons && swampyBastards <= swampyCinnamons) {
            losingRole = Constants.SWAMPY_BASTARDS_ROLE;
        } else {
            losingRole = Constants.SWAMPY_CINNAMON_ROLE;
        }

        MutableLong totalPointsGiven = new MutableLong(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder("Shrek has infiltrated the swamp! He's giving \uD83E\uDDC5 \uD83E\uDDC5 \uD83E\uDDC5 " +
                "to the chaos children of the house with the least points!\n\n");

        for (DiscordUser user : swampyUsers) {
            Member memberById = philJda.getGuilds().get(0).getMemberById(user.getId());

            if (memberById != null && !isNotParticipating(memberById) && hasRole(memberById, losingRole) && hasRole(memberById, Constants.CHAOS_CHILDREN_ROLE)) {
                try {
                    long points = ThreadLocalRandom.current()
                            .nextLong(swampyGamesConfig.getShrekoningMinPoints(), swampyGamesConfig.getShrekoningMaxPoints());

                    futures.add(givePointsToMember(points, memberById));
                    totalPointsGiven.add(points);
                    description
                            .append(NumberFormat.getIntegerInstance().format(points))
                            .append(" \uD83E\uDDC5")
                            .append(" for <@!")
                            .append(user.getId())
                            .append(">\n");
                } catch (Exception e) {
                    logger.error("Failed to shrekoning user [id={}]", user.getId(), e);
                    honeybadgerReporter.reportError(e, "Failed to shrekoning user: " + user.getId());
                }
            }
        }

        description.append("\nShrek gave a total of ")
                .append(NumberFormat.getIntegerInstance().format(totalPointsGiven.getValue()))
                .append(" \uD83E\uDDC5 to the ")
                .append(losingRole)
                .append('!');

        MessageEmbed message = Constants.simpleEmbed("The Shrekoning", description.toString(), swampyGamesConfig.getShrekoningImg());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
        });
    }

}
