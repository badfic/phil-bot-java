package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.service.MinuteTickable;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MapTickable extends NonCommandSwampy implements MinuteTickable {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void runMinutelyTask() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        if (Objects.nonNull(swampyGamesConfig.getMapPhrase())
                && Objects.nonNull(swampyGamesConfig.getMapTriviaExpiration())
                && swampyGamesConfig.getMapTriviaExpiration().isBefore(LocalDateTime.now())) {
            swampyGamesConfig.setMapPhrase(null);
            swampyGamesConfig.setMapTriviaExpiration(null);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);
            Guild guild = philJda.getGuildById(baseConfig.guildId);

            List<CompletableFuture<?>> futures = new ArrayList<>();
            LocalDateTime startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusMinutes(15);
            StringBuilder description = new StringBuilder();
            discordUserRepository.findAll()
                    .stream()
                    .filter(u -> Objects.nonNull(u.getAcceptedMapTrivia()) && u.getAcceptedMapTrivia().isAfter(startTime))
                    .forEach(u -> {
                        try {
                            Member memberLookedUp = guild.getMemberById(u.getId());
                            if (memberLookedUp == null) {
                                throw new RuntimeException("member not found");
                            }

                            futures.add(givePointsToMember(swampyGamesConfig.getMapEventPoints(), memberLookedUp, PointsStat.MAP));
                            description.append("Gave ")
                                    .append(swampyGamesConfig.getMapEventPoints())
                                    .append(" points to <@!")
                                    .append(u.getId())
                                    .append(">\n");
                        } catch (Exception e) {
                            logger.error("Failed to give map trivia points to user [id={}]", u.getId(), e);
                            description.append("OOPS: Unable to give points to <@!")
                                    .append(u.getId())
                                    .append(">\n");
                        }
                    });

            MessageEmbed messageEmbed = Constants.simpleEmbed("Map Trivia Complete", description.toString());

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                    .thenRun(() -> swampysChannel.sendMessageEmbeds(messageEmbed).queue());
        }
    }
}
