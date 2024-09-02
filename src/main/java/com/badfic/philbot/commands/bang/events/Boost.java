package com.badfic.philbot.commands.bang.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.PointsStat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Boost extends BaseBangCommand {

    public Boost() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "boost";
        aliases = new String[] {"blitz"};
        help = "!!boost\nManually trigger a boost, it will end at the top of the hour";
    }

    @Override
    public void execute(final CommandEvent event) {
        doBoost(true);
    }

    @Scheduled(cron = "${swampy.schedule.events.boost}", zone = "${swampy.schedule.timezone}")
    void boost() {
        Thread.startVirtualThread(() -> doBoost(false));
    }

    private void doBoost(final boolean force) {
        final var swampyGamesConfig = getSwampyGamesConfig();

        final var swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).getFirst();
        final var guild = Objects.requireNonNull(philJda.getGuildById(baseConfig.guildId));

        if (swampyGamesConfig.getBoostPhrase() != null) {
            final var startTime = Objects.requireNonNullElseGet(swampyGamesConfig.getBoostStartTime(), () -> LocalDateTime.now().minusMinutes(61));

            swampyGamesConfig.setBoostPhrase(null);
            swampyGamesConfig.setBoostStartTime(null);
            saveSwampyGamesConfig(swampyGamesConfig);

            final var futures = new ArrayList<CompletableFuture<?>>();
            final var description = new StringBuilder();
            discordUserRepository.findAll()
                    .stream()
                    .filter(u -> Objects.nonNull(u.getAcceptedBoost()) && u.getAcceptedBoost().isAfter(startTime))
                    .forEach(discordUser -> {
                        final var userId = discordUser.getId();

                        try {
                            final var memberLookedUp = guild.getMemberById(userId);
                            if (memberLookedUp == null) {
                                throw new RuntimeException("member not found");
                            }

                            if (isNotParticipating(memberLookedUp)) {
                                description.append("<@")
                                        .append(userId)
                                        .append("> Please ask a mod to check your roles to participate in the swampys\n");
                            } else {
                                futures.add(givePointsToMember(swampyGamesConfig.getBoostEventPoints(), memberLookedUp, PointsStat.BOOST));
                                description.append("Gave ")
                                        .append(swampyGamesConfig.getBoostEventPoints())
                                        .append(" points to <@")
                                        .append(userId)
                                        .append(">\n");
                            }
                        } catch (final Exception e) {
                            log.error("Failed to give boost points to user [id={}]", userId, e);
                            description.append("OOPS: Unable to give points to <@")
                                    .append(userId)
                                    .append(">\n");
                        }
                    });

            final var messageEmbed = Constants.simpleEmbed("Boost Blitz Complete", description.toString(), swampyGamesConfig.getBoostEndImg());

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> swampysChannel.sendMessageEmbeds(messageEmbed).queue());
            return;
        }

        if (force || randomNumberService.nextInt(100) < swampyGamesConfig.getPercentChanceBoostHappensOnHour()) {
            final var boostPhrase = Constants.pickRandom(swampyGamesConfig.getBoostWords());
            swampyGamesConfig.setBoostPhrase(boostPhrase);
            swampyGamesConfig.setBoostStartTime(LocalDateTime.now());
            saveSwampyGamesConfig(swampyGamesConfig);

            final var message = Constants.simpleEmbed("BOOST BLITZ",
                    "Type `" + boostPhrase + "` in this channel before the top of the hour* to be boosted by "
                            + swampyGamesConfig.getBoostEventPoints() + " points",
                    swampyGamesConfig.getBoostStartImg(), "* top of the hour unless it's midnight Pacific time, then the boost runs until 3am Pacific time");

            swampysChannel.sendMessageEmbeds(message).queue();
        }
    }

}
