package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Boost extends BaseSwampy implements PhilMarker {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final long BOOST_POINTS_TO_GIVE = 1_000;
    private static final long PERCENTAGE_CHANCE_BOOST_HAPPENS_ON_THE_HOUR = 15;
    private static final String BOOST_START = "https://cdn.discordapp.com/attachments/587078427400732682/772345771885985842/booawoost_tg.png";
    private static final String BOOST_END = "https://cdn.discordapp.com/attachments/587078427400732682/772345775556132864/boostend_tg.png";

    public Boost() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "boost";
    }

    @Override
    public void execute(CommandEvent event) {
        doBoost(true);
    }

    @Scheduled(cron = "0 0 * * * ?", zone = "GMT")
    public void boost() {
        doBoost(false);
    }

    private void doBoost(boolean force) {
        Optional<SwampyGamesConfig> optionalConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID);
        if (!optionalConfig.isPresent()) {
            return;
        }
        SwampyGamesConfig swampyGamesConfig = optionalConfig.get();

        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                .get(0);

        if (swampyGamesConfig.getBoostPhrase() != null) {
            swampyGamesConfig.setBoostPhrase(null);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            StringBuilder description = new StringBuilder();
            discordUserRepository.findAll()
                    .stream()
                    .filter(u -> u.getAcceptedBoost().isAfter(oneHourAgo))
                    .forEach(u -> {
                        try {
                            Member memberLookedUp = philJda.getGuilds().get(0).getMemberById(u.getId());
                            if (memberLookedUp == null) {
                                throw new RuntimeException("member not found");
                            }

                            futures.add(givePointsToMember(1000, memberLookedUp));
                            description.append("Gave " + BOOST_POINTS_TO_GIVE + " points to <@!")
                                    .append(u.getId())
                                    .append(">\n");
                        } catch (Exception e) {
                            logger.error("Failed to give boost points to user [id={}]", u.getId(), e);
                            description.append("OOPS: Unable to give points to <@!")
                                    .append(u.getId())
                                    .append(">\n");
                        }
                    });

            MessageEmbed messageEmbed = new EmbedBuilder()
                    .setImage(BOOST_END)
                    .setTitle("Boost Blitz Complete")
                    .setDescription(description.toString())
                    .setColor(Constants.COLOR_OF_THE_MONTH)
                    .build();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> swampysChannel.sendMessage(messageEmbed).queue());
            return;
        }

        if (force || ThreadLocalRandom.current().nextInt(100) < PERCENTAGE_CHANCE_BOOST_HAPPENS_ON_THE_HOUR) {
            String boostPhrase = pickRandom(Arrays.asList("butter", "shortening", "lard", "ghee", "oleo", "spread", "thanks", "giving", "family", "turkey",
                    "feet", "foot", "stuffing", "cranberry", "cornbread", "bird", "greenbean", "stringbean", "rice", "pumpkin", "pie", "apple", "gravy",
                    "potato", "corn", "maze", "maize", "stonks", "stimulus", "baster", "turducken", "scarecrow", "butters"));
            swampyGamesConfig.setBoostPhrase(boostPhrase);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            MessageEmbed message = new EmbedBuilder()
                    .setTitle("BOOST BLITZ")
                    .setDescription("Type `" + boostPhrase + "` in this channel before the top of the hour to be boosted by "
                            + BOOST_POINTS_TO_GIVE + " points")
                    .setImage(BOOST_START)
                    .setColor(Constants.COLOR_OF_THE_MONTH)
                    .build();

            swampysChannel.sendMessage(message).queue();
        }
    }

}
