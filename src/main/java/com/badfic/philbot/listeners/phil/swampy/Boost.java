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

    public static final long BOOST_POINTS_TO_GIVE = 1_000;
    public static final long PERCENTAGE_CHANCE_BOOST_HAPPENS_ON_THE_HOUR = 15;
    public static final String BOOST_START = "https://cdn.discordapp.com/attachments/323666308107599872/761492230379798538/BOOST.png";
    public static final String BOOST_END = "https://cdn.discordapp.com/attachments/323666308107599872/761494374445219850/stan_loona_goddess.png";

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
                    .setColor(Constants.HALOWEEN_ORANGE)
                    .build();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> swampysChannel.sendMessage(messageEmbed).queue());
            return;
        }

        if (force || ThreadLocalRandom.current().nextInt(100) < PERCENTAGE_CHANCE_BOOST_HAPPENS_ON_THE_HOUR) {
            String boostPhrase = pickRandom(Arrays.asList("boost", "yeet", "simp", "swamp", "spooner", "grapefruit", "thicc", "ranch", "bounce", "hard", "firm",
                    "loose", "rub", "moist", "wet", "damp", "wap", "spooky", "season", "pumpkin", "skeleton", "vampire", "bat", "coffin", "trick", "treat",
                    "harvest", "ghost", "skull", "spirit", "halloween", "costume", "afraid", "cat", "witch", "magic", "boo", "broomstick", "boogeyman",
                    "corpse", "casket", "cloak", "creepy", "devil", "dark", "demon", "prank", "darkness", "evil", "death", "goblin", "zombie", "spider",
                    "fog", "karl", "gruesome", "grave", "goodies", "haunted", "howl", "horror", "spoopy", "horrifying", "macabre", "masquerade", "mist",
                    "moon", "morbid", "midnight", "mummy", "nightmare", "night", "ogre", "october", "petrify", "phantom", "rip", "scarecrow", "scare",
                    "fright", "scary", "scream", "shadow", "startled", "web", "sweets", "supernatural", "tomb", "wand", "wicked", "witch", "wizard", "wraith",
                    "warlock", "sorcerer"));
            swampyGamesConfig.setBoostPhrase(boostPhrase);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            MessageEmbed message = new EmbedBuilder()
                    .setTitle("BOOST BLITZ")
                    .setDescription("Type `" + boostPhrase + "` in this channel within the next hour to be boosted by "
                            + BOOST_POINTS_TO_GIVE + " points")
                    .setImage(BOOST_START)
                    .setColor(Constants.HALOWEEN_ORANGE)
                    .build();

            swampysChannel.sendMessage(message).queue();
        }
    }

}
