package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.text.NumberFormat;
import java.time.LocalDateTime;
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
public class Shrekoning extends BaseSwampy {

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

    @Scheduled(cron = "0 47 2 * * ?", zone = "GMT")
    public void shrekoning() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        if (swampyGamesConfig == null) {
            return;
        }

        List<DiscordUser> allUsers = discordUserRepository.findAll();

        MutableLong totalPointsGiven = new MutableLong(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder("Shrek has infiltrated the swamp! He's giving \uD83E\uDDC5 \uD83E\uDDC5 \uD83E\uDDC5 " +
                "to all of the not-top-3 chaos children!\n\n");

        allUsers.stream()
                .sorted((u1, u2) -> Long.compare(u2.getXp(), u1.getXp()))
                .filter(u -> u.getXp() > SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD && u.getUpdateTime().isAfter(LocalDateTime.now().minusHours(22)))
                .filter(u -> {
                    Member m = philJda.getGuilds().get(0).getMemberById(u.getId());
                    return m != null && !m.getUser().isBot() && hasRole(m, Constants.CHAOS_CHILDREN_ROLE);
                })
                .skip(3)
                .forEachOrdered(user -> {
                    try {
                        Member memberById = philJda.getGuilds().get(0).getMemberById(user.getId());
                        if (memberById != null) {
                            long points = ThreadLocalRandom.current()
                                    .nextLong(swampyGamesConfig.getShrekoningMinPoints(), swampyGamesConfig.getShrekoningMaxPoints());

                            futures.add(givePointsToMember(points, memberById, PointsStat.SHREKONING));
                            totalPointsGiven.add(points);
                            description
                                    .append(NumberFormat.getIntegerInstance().format(points))
                                    .append(" \uD83E\uDDC5")
                                    .append(" for <@!")
                                    .append(user.getId())
                                    .append(">\n");
                        }
                    } catch (Exception e) {
                        logger.error("Failed to shrekoning user [id={}]", user.getId(), e);
                        honeybadgerReporter.reportError(e, "Failed to shrekoning user: " + user.getId());
                    }
                });

        description.append("\nShrek gave a total of ")
                .append(NumberFormat.getIntegerInstance().format(totalPointsGiven.getValue()))
                .append(" \uD83E\uDDC5 to the chaos children!");

        MessageEmbed message = Constants.simpleEmbed("The Shrekoning", description.toString(), swampyGamesConfig.getShrekoningImg());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
        });
    }

}
