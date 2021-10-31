package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.google.common.collect.ImmutableMap;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CursedHalloweenBoost extends BaseSwampy {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Map<String, String> IMAGES = ImmutableMap.<String, String>builder()
            .put("I love Timothee Chimothee", "https://cdn.discordapp.com/attachments/707453916882665552/904494573903040533/unknown.png")
            .put("Matthew Morrison is my favorite grinch", "https://cdn.discordapp.com/attachments/707453916882665552/904494688176861184/unknown.png")
            .put("Jack Scalfani has great taste", "https://cdn.discordapp.com/attachments/323666308107599872/904324512009572382/unknown.png")
            .put("Lord Farquaad is bae", "https://cdn.discordapp.com/attachments/707453916882665552/904495873722056734/unknown.png")
            .put("Phil Klemmer is the best producer", "https://cdn.discordapp.com/attachments/707453916882665552/904495996346728518/unknown.png")
            .put("Justin Bieber, oh yeah baby", "https://cdn.discordapp.com/attachments/707453916882665552/904496585717719071/unknown.png")
            .put("Chris Pratt is the best actor on Parks and Rec", "https://cdn.discordapp.com/attachments/707453916882665552/904496790504624228/unknown.png")
            .build();

    public CursedHalloweenBoost() {
        ownerCommand = true;
        name = "cursedBoost";
        help = "!!cursedBoost cannot be manually triggered";
    }

    @Override
    public void execute(CommandEvent event) {
        doBoost();
    }

    @Scheduled(cron = "0 0,15,30,45 * * * ?", zone = "America/Los_Angeles")
    public void boost() {
        doBoost();
    }

    private void doBoost() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);
        Guild guild = philJda.getGuilds().get(0);

        if (swampyGamesConfig.getBoostPhrase() != null) {
            String boostPhrase = swampyGamesConfig.getBoostPhrase();

            swampyGamesConfig.setBoostPhrase(null);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
            StringBuilder description = new StringBuilder();
            discordUserRepository.findAll()
                    .stream()
                    .filter(u -> u.getAcceptedBoost().isAfter(fifteenMinutesAgo))
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

            MessageEmbed messageEmbed = Constants.simpleEmbed("Cursed Boost Complete", description.toString(), IMAGES.get(boostPhrase));

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> swampysChannel.sendMessageEmbeds(messageEmbed).queue());
            return;
        }

        String boostPhrase = Constants.pickRandom(IMAGES.keySet());
        swampyGamesConfig.setBoostPhrase(boostPhrase);
        swampyGamesConfigRepository.save(swampyGamesConfig);

        MessageEmbed message = Constants.simpleEmbed("CURSED BOOST",
                "Type `" + boostPhrase + "` in this channel in the next 15 minutes to be boosted by "
                        + swampyGamesConfig.getBoostEventPoints() + " points",
                IMAGES.get(boostPhrase));

        swampysChannel.sendMessageEmbeds(message).queue();
    }

}
