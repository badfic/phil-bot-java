package com.badfic.philbot.commands.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrickOrTreat extends BaseNormalCommand {
    public TrickOrTreat() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "trickortreat";
        help = "!!trickortreat\nManually trigger a Trick Or Treat (aka Checkout or Trampled, aka Naughty or Nice)";
    }

    @Override
    public void execute(CommandEvent event) {
        trickOrTreat();
    }

    @Scheduled(cron = "${swampy.schedule.events.trickortreat}", zone = "${swampy.schedule.timezone}")
    public void trickOrTreat() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        Guild guild = philJda.getGuildById(baseConfig.guildId);
        List<DiscordUser> allUsers = discordUserRepository.findAll();
        allUsers.sort((u1, u2) -> Long.compare(u2.getXp(), u1.getXp())); // Descending sort

        LocalDateTime fifteenHoursAgo = LocalDateTime.now().minusHours(15);

        long totalGiven = 0;
        long totalTaken = 0;
        List<CompletableFuture<?>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder();
        for (DiscordUser user : allUsers) {
            if (user.getXp() > SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD && user.getUpdateTime().isAfter(fifteenHoursAgo)) {
                try {
                    Member memberById = guild.getMemberById(user.getId());
                    if (memberById != null && !isNotParticipating(memberById)) {
                        if (ThreadLocalRandom.current().nextInt() % 2 == 0) {
                            futures.add(givePointsToMember(swampyGamesConfig.getThisOrThatPoints(), memberById, PointsStat.TRICK_OR_TREAT));
                            totalGiven += swampyGamesConfig.getThisOrThatPoints();

                            description
                                    .append(NumberFormat.getIntegerInstance().format(swampyGamesConfig.getThisOrThatPoints()))
                                    .append(' ')
                                    .append(swampyGamesConfig.getThisOrThatGiveEmoji())
                                    .append(" given to <@")
                                    .append(user.getId())
                                    .append(">\n");
                        } else {
                            futures.add(takePointsFromMember(swampyGamesConfig.getThisOrThatPoints(), memberById, PointsStat.TRICK_OR_TREAT));
                            totalTaken += swampyGamesConfig.getThisOrThatPoints();

                            description
                                    .append(NumberFormat.getIntegerInstance().format(swampyGamesConfig.getThisOrThatPoints()))
                                    .append(' ')
                                    .append(swampyGamesConfig.getThisOrThatTakeEmoji())
                                    .append(" taken from <@")
                                    .append(user.getId())
                                    .append(">\n");
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to trick or treat user [id={}]", user.getId(), e);
                }
            }
        }

        String footer = "I gave " + NumberFormat.getIntegerInstance().format(totalGiven) + " points and took "
                + NumberFormat.getIntegerInstance().format(totalTaken);

        String title = swampyGamesConfig.getThisOrThatGiveEmoji() + " "
                + swampyGamesConfig.getThisOrThatName() + " "
                + swampyGamesConfig.getThisOrThatTakeEmoji();

        MessageEmbed message = Constants.simpleEmbed(title, description.toString(), swampyGamesConfig.getThisOrThatImage(), footer);

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessageEmbeds(message)
                    .queue();
        });
    }

}
