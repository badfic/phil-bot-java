package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TrickOrTreat extends BaseSwampy implements PhilMarker {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public TrickOrTreat() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "trickortreat";
        help = "!!trickortreat\nManually trigger a Trick Or Treat (aka Checkout or Trampled, aka Naughty or Nice)";
    }

    @Override
    public void execute(CommandEvent event) {
        trickOrTreat();
    }

    @Scheduled(cron = "0 3 19 * * ?", zone = "GMT")
    public void trickOrTreat() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        if (swampyGamesConfig == null) {
            return;
        }

        List<DiscordUser> allUsers = discordUserRepository.findAll();
        allUsers.sort((u1, u2) -> Long.compare(u2.getXp(), u1.getXp())); // Descending sort

        long totalGiven = 0;
        long totalTaken = 0;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder();
        for (DiscordUser user : allUsers) {
            if (user.getXp() > SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD && user.getUpdateTime().isAfter(LocalDateTime.now().minusHours(22))) {
                try {
                    Member memberById = philJda.getGuilds().get(0).getMemberById(user.getId());
                    if (memberById != null && !isNotParticipating(memberById)) {
                        if (ThreadLocalRandom.current().nextInt() % 2 == 0) {
                            futures.add(givePointsToMember(swampyGamesConfig.getTrickOrTreatPoints(), memberById));
                            totalGiven += swampyGamesConfig.getTrickOrTreatPoints();

                            description
                                    .append(NumberFormat.getIntegerInstance().format(swampyGamesConfig.getTrickOrTreatPoints()))
                                    .append(' ')
                                    .append(swampyGamesConfig.getTrickOrTreatTreatEmoji())
                                    .append(" given to <@!")
                                    .append(user.getId())
                                    .append(">\n");
                        } else {
                            futures.add(takePointsFromMember(swampyGamesConfig.getTrickOrTreatPoints(), memberById));
                            totalTaken += swampyGamesConfig.getTrickOrTreatPoints();

                            description
                                    .append(NumberFormat.getIntegerInstance().format(swampyGamesConfig.getTrickOrTreatPoints()))
                                    .append(' ')
                                    .append(swampyGamesConfig.getTrickOrTreatTrickEmoji())
                                    .append(" taken from <@!")
                                    .append(user.getId())
                                    .append(">\n");
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to trick or treat user [id={}]", user.getId(), e);
                    honeybadgerReporter.reportError(e, "Failed to trick or treat user: " + user.getId());
                }
            }
        }

        String footer = "I gave " + NumberFormat.getIntegerInstance().format(totalGiven) + " points and took "
                + NumberFormat.getIntegerInstance().format(totalTaken);

        String title = swampyGamesConfig.getTrickOrTreatTreatEmoji() + " "
                + swampyGamesConfig.getTrickOrTreatName() + " "
                + swampyGamesConfig.getTrickOrTreatTrickEmoji();

        MessageEmbed message = Constants.simpleEmbed(title, description.toString(), swampyGamesConfig.getTrickOrTreatImg(), footer);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
        });
    }

}
