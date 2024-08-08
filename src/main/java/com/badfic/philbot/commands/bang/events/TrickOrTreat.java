package com.badfic.philbot.commands.bang.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.PointsStat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrickOrTreat extends BaseBangCommand {
    public TrickOrTreat() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "trickortreat";
        help = "!!trickortreat\nManually trigger a Trick Or Treat (aka Checkout or Trampled, aka Naughty or Nice)";
    }

    @Override
    public void execute(final CommandEvent event) {
        trickOrTreat();
    }

    @Scheduled(cron = "${swampy.schedule.events.trickortreat}", zone = "${swampy.schedule.timezone}")
    void trickOrTreat() {
        final var swampyGamesConfig = getSwampyGamesConfig();

        final var guild = philJda.getGuildById(baseConfig.guildId);
        final var allUsers = discordUserRepository.findAll();
        allUsers.sort((u1, u2) -> Long.compare(u2.getXp(), u1.getXp())); // Descending sort

        final var fifteenHoursAgo = LocalDateTime.now().minusHours(15);

        var totalGiven = 0L;
        var totalTaken = 0L;
        final var futures = new ArrayList<CompletableFuture<?>>();
        final var description = new StringBuilder();
        for (final var user : allUsers) {
            if (user.getUpdateTime().isAfter(fifteenHoursAgo)) {
                try {
                    final var memberById = guild.getMemberById(user.getId());
                    if (memberById != null && !isNotParticipating(memberById)) {
                        if (randomNumberService.nextInt() % 2 == 0) {
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
                } catch (final Exception e) {
                    log.error("Failed to trick or treat user [id={}]", user.getId(), e);
                }
            }
        }

        final var footer = "I gave " + NumberFormat.getIntegerInstance().format(totalGiven) + " points and took "
                + NumberFormat.getIntegerInstance().format(totalTaken);

        final var title = swampyGamesConfig.getThisOrThatGiveEmoji() + " "
                + swampyGamesConfig.getThisOrThatName() + " "
                + swampyGamesConfig.getThisOrThatTakeEmoji();

        final var message = Constants.simpleEmbed(title, description.toString(), swampyGamesConfig.getThisOrThatImage(), footer);

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .getFirst()
                    .sendMessageEmbeds(message)
                    .queue();
        });
    }

}
