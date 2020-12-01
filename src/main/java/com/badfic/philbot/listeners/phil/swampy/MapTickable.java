package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUserRepository;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.data.phil.SwampyGamesConfigRepository;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class MapTickable extends NonCommandSwampy implements MinuteTickable {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Resource
    private DiscordUserRepository discordUserRepository;

    @Resource
    private SwampyGamesConfigRepository swampyGamesConfigRepository;

    @Resource(name = "philJda")
    @Lazy
    private JDA philJda;

    @Override
    public void tick() throws Exception {
        Optional<SwampyGamesConfig> optionalConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID);
        if (!optionalConfig.isPresent()) {
            return;
        }
        SwampyGamesConfig swampyGamesConfig = optionalConfig.get();

        TextChannel[] channels = new TextChannel[] {
                philJda.getTextChannelsByName(Constants.DRY_BASTARDS_CHANNEL, false).get(0),
                philJda.getTextChannelsByName(Constants.DRY_CINNAMON_CHANNEL, false).get(0),
                philJda.getTextChannelsByName(Constants.SWAMPY_BASTARD_CHANNEL, false).get(0),
                philJda.getTextChannelsByName(Constants.SWAMPY_CINNAMON_CHANNEL, false).get(0)
        };

        if (Objects.nonNull(swampyGamesConfig.getMapPhrase())
                && Objects.nonNull(swampyGamesConfig.getMapTriviaExpiration())
                && swampyGamesConfig.getMapTriviaExpiration().isBefore(LocalDateTime.now())) {
            swampyGamesConfig.setMapPhrase(null);
            swampyGamesConfig.setMapTriviaExpiration(null);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            LocalDateTime startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusMinutes(15);
            StringBuilder description = new StringBuilder();
            discordUserRepository.findAll()
                    .stream()
                    .filter(u -> Objects.nonNull(u.getAcceptedMapTrivia()) && u.getAcceptedMapTrivia().isAfter(startTime))
                    .forEach(u -> {
                        try {
                            Member memberLookedUp = philJda.getGuilds().get(0).getMemberById(u.getId());
                            if (memberLookedUp == null) {
                                throw new RuntimeException("member not found");
                            }

                            futures.add(givePointsToMember(swampyGamesConfig.getMapEventPoints(), memberLookedUp));
                            description.append("Gave " + swampyGamesConfig.getMapEventPoints() + " points to <@!")
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

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        for (TextChannel channel : channels) {
                            channel.sendMessage(messageEmbed).queue();
                        }
                    });
        }
    }
}
