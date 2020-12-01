package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.google.common.collect.ImmutableSet;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
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

    private static final Set<String> BOOST_WORDS = ImmutableSet.of(
            "butter", "shortening", "lard", "ghee", "oleo", "olio", "spread", "family", "feet", "foot", "cranberry", "bird", "pie", "stonks", "butters",
            "jingle", "jolly", "festivus", "santa", "grinch", "scrooge", "hanukkah", "latkes", "dreidel", "festival", "presents", "menorah", "pajamas",
            "pyjamas", "jimjams", "crackers", "bauble", "buble", "advent", "druid", "shrekmas", "swampmas", "snoopmas", "holly", "krampus", "mittens",
            "yuletide", "naughty", "nice", "nutcracker", "sleigh", "solstice", "yule", "yeet", "merry", "chrysler");

    public Boost() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "boost";
        help = "!!boost\nManually trigger a boost, if you don't end it manually it will end at the top of the hour";
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

        TextChannel[] channels = new TextChannel[] {
                philJda.getTextChannelsByName(Constants.DRY_BASTARDS_CHANNEL, false).get(0),
                philJda.getTextChannelsByName(Constants.DRY_CINNAMON_CHANNEL, false).get(0),
                philJda.getTextChannelsByName(Constants.SWAMPY_BASTARD_CHANNEL, false).get(0),
                philJda.getTextChannelsByName(Constants.SWAMPY_CINNAMON_CHANNEL, false).get(0)
        };

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
                            u.setBoostParticipations(u.getBoostParticipations() + 1);
                            discordUserRepository.save(u);

                            Member memberLookedUp = philJda.getGuilds().get(0).getMemberById(u.getId());
                            if (memberLookedUp == null) {
                                throw new RuntimeException("member not found");
                            }

                            if (isNotParticipating(memberLookedUp)) {
                                description.append("<@!")
                                        .append(u.getId())
                                        .append("> Please get sorted into a house to participate\n");
                            } else {
                                futures.add(givePointsToMember(swampyGamesConfig.getBoostEventPoints(), memberLookedUp));
                                description.append("Gave ")
                                        .append(swampyGamesConfig.getBoostEventPoints())
                                        .append(" points to <@!")
                                        .append(u.getId())
                                        .append(">\n");
                            }
                        } catch (Exception e) {
                            logger.error("Failed to give boost points to user [id={}]", u.getId(), e);
                            description.append("OOPS: Unable to give points to <@!")
                                    .append(u.getId())
                                    .append(">\n");
                        }
                    });

            MessageEmbed messageEmbed = Constants.simpleEmbed("Boost Blitz Complete", description.toString(), swampyGamesConfig.getBoostEndImg());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        for (TextChannel channel : channels) {
                            channel.sendMessage(messageEmbed).queue();
                        }
                    });
            return;
        }

        if (force || ThreadLocalRandom.current().nextInt(100) < swampyGamesConfig.getPercentChanceBoostHappensOnHour()) {
            String boostPhrase = Constants.pickRandom(BOOST_WORDS);
            swampyGamesConfig.setBoostPhrase(boostPhrase);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            MessageEmbed message = Constants.simpleEmbed("BOOST BLITZ",
                    "Type `" + boostPhrase + "` in this channel before the top of the hour to be boosted by "
                            + swampyGamesConfig.getBoostEventPoints() + " points",
                    swampyGamesConfig.getBoostStartImg());

            for (TextChannel channel : channels) {
                channel.sendMessage(message).queue();
            }
        }
    }

}
