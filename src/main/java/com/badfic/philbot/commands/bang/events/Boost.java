package com.badfic.philbot.commands.bang.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
    public void execute(CommandEvent event) {
        doBoost(true);
    }

    @Scheduled(cron = "${swampy.schedule.events.boost}", zone = "${swampy.schedule.timezone}")
    void boost() {
        doBoost(false);
    }

    private void doBoost(boolean force) {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).getFirst();
        Guild guild = Objects.requireNonNull(philJda.getGuildById(baseConfig.guildId));

        if (swampyGamesConfig.getBoostPhrase() != null) {
            LocalDateTime startTime = Objects.requireNonNullElseGet(swampyGamesConfig.getBoostStartTime(), () -> LocalDateTime.now().minusMinutes(61));

            swampyGamesConfig.setBoostPhrase(null);
            swampyGamesConfig.setBoostStartTime(null);
            saveSwampyGamesConfig(swampyGamesConfig);

            List<CompletableFuture<?>> futures = new ArrayList<>();
            StringBuilder description = new StringBuilder();
            discordUserRepository.findAll()
                    .stream()
                    .filter(u -> Objects.nonNull(u.getAcceptedBoost()) && u.getAcceptedBoost().isAfter(startTime))
                    .forEach(discordUser -> {
                        String userId = discordUser.getId();

                        try {
                            Member memberLookedUp = guild.getMemberById(userId);
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
                        } catch (Exception e) {
                            log.error("Failed to give boost points to user [id={}]", userId, e);
                            description.append("OOPS: Unable to give points to <@")
                                    .append(userId)
                                    .append(">\n");
                        }
                    });

            MessageEmbed messageEmbed = Constants.simpleEmbed("Boost Blitz Complete", description.toString(), swampyGamesConfig.getBoostEndImg());

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> swampysChannel.sendMessageEmbeds(messageEmbed).queue());
            return;
        }

        if (force || randomNumberService.nextInt(100) < swampyGamesConfig.getPercentChanceBoostHappensOnHour()) {
            String boostPhrase = Constants.pickRandom(swampyGamesConfig.getBoostWords());
            swampyGamesConfig.setBoostPhrase(boostPhrase);
            swampyGamesConfig.setBoostStartTime(LocalDateTime.now());
            saveSwampyGamesConfig(swampyGamesConfig);

            MessageEmbed message = Constants.simpleEmbed("BOOST BLITZ",
                    "Type `" + boostPhrase + "` in this channel before the top of the hour* to be boosted by "
                            + swampyGamesConfig.getBoostEventPoints() + " points",
                    swampyGamesConfig.getBoostStartImg(), "* top of the hour unless it's midnight Pacific time, then the boost runs until 3am Pacific time");

            swampysChannel.sendMessageEmbeds(message).queue();
        }
    }

}
