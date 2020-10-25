package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TrickOrTreat extends BaseSwampy implements PhilMarker {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int TRICK_OR_TREAT_POINTS = 500;
    private static final String TRICK_OR_TREAT = "https://cdn.discordapp.com/attachments/323666308107599872/761499236940251136/trick_or_treat.png";

    public TrickOrTreat() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "trickortreat";
    }

    @Override
    public void execute(CommandEvent event) {
        trickOrTreat();
    }

    @Scheduled(cron = "0 3 19 * * ?", zone = "GMT")
    public void trickOrTreat() {
        List<DiscordUser> allUsers = discordUserRepository.findAll();

        long totalGiven = 0;
        long totalTaken = 0;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder();
        for (DiscordUser user : allUsers) {
            if (user.getXp() > SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD) {
                try {
                    Member memberById = philJda.getGuilds().get(0).getMemberById(user.getId());
                    if (memberById != null && !memberById.getUser().isBot()) {
                        if (ThreadLocalRandom.current().nextInt() % 2 == 0 || hasRole(memberById, Constants.CHAOS_CHILDREN_ROLE)) {
                            futures.add(givePointsToMember(TRICK_OR_TREAT_POINTS, memberById));
                            totalGiven += TRICK_OR_TREAT_POINTS;

                            description
                                    .append("\uD83C\uDF6C gave ")
                                    .append(NumberFormat.getIntegerInstance().format(TRICK_OR_TREAT_POINTS))
                                    .append(" points to <@!")
                                    .append(user.getId())
                                    .append(">\n");
                        } else {
                            futures.add(takePointsFromMember(TRICK_OR_TREAT_POINTS, memberById));
                            totalTaken += TRICK_OR_TREAT_POINTS;

                            description
                                    .append("\uD83D\uDC80 took ")
                                    .append(NumberFormat.getIntegerInstance().format(TRICK_OR_TREAT_POINTS))
                                    .append(" points from <@!")
                                    .append(user.getId())
                                    .append(">\n");
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to trick or treat user [id={}]", user.getId(), e);
                }
            }
        }

        description.append("\n\nI gave ")
                .append(NumberFormat.getIntegerInstance().format(totalGiven))
                .append(" points and took ")
                .append(NumberFormat.getIntegerInstance().format(totalTaken));

        String title = "\uD83C\uDF6C Trick Or Treat! \uD83C\uDF6C";
        MessageEmbed message = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description.toString())
                .setImage(TRICK_OR_TREAT)
                .setColor(Constants.HALOWEEN_ORANGE)
                .build();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
        });
    }

}
