package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.google.common.collect.ImmutableSet;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Boost extends BaseSwampy {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Set<String> BOOST_WORDS = ImmutableSet.of(
            "butter", "oleo", "olio", "foot", "stonks", "birthday", "grinch", "riverdale", "shrimp", "snoop", "boost", "word", "kitten", "naughty", "nice",
            "nut", "feet", "yeet", "missouri", "shrek", "swamp", "drench", "florida", "moist", "void", "bird", "legends", "plants", "simp", "nuggets", "pride",
            "kachow", "daddy", "murder", "hunger", "tiger", "cougar", "leopard", "purr", "purrfect", "purrugly", "glammeow", "shamwow", "skitty", "purrgatory",
            "delcatty", "tabby", "ragdoll", "abyssinian", "havana", "mainecoon", "supercatgalistic", "litten", "litter");

    public Boost() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "boost";
        help = "!!boost\nManually trigger a boost, it will end at the top of the hour";
    }

    @Override
    public void execute(CommandEvent event) {
        doBoost(true);
    }

    @Scheduled(cron = "${swampy.schedule.hourly}", zone = "${swampy.schedule.timezone}")
    public void boost() {
        doBoost(false);
    }

    private void doBoost(boolean force) {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);
        Guild guild = philJda.getGuilds().get(0);

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
                            Member memberLookedUp = guild.getMemberById(u.getId());
                            if (memberLookedUp == null) {
                                throw new RuntimeException("member not found");
                            }

                            if (isNotParticipating(memberLookedUp)) {
                                description.append("<@!")
                                        .append(u.getId())
                                        .append("> Please ask a mod to check your roles to participate in the swampys\n");
                            } else {
                                futures.add(givePointsToMember(swampyGamesConfig.getBoostEventPoints(), memberLookedUp, PointsStat.BOOST));
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

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> swampysChannel.sendMessageEmbeds(messageEmbed).queue());
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

            swampysChannel.sendMessageEmbeds(message).queue();
        }
    }

}
