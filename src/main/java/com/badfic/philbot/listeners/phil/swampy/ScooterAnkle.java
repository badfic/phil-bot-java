package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ScooterAnkle extends BaseSwampy {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public ScooterAnkle() {
        name = "scooterankle";
        help = "`!!scooterankle`\n" +
                "Scooter Ankle sacrifices 25,000 of your own points to all swamplings (bastard and child) below you.\n" +
                "In hopes that one of them may overtake the leader of their respective scoreboards.";
    }

    @Override
    protected void execute(CommandEvent event) {
        scooterAnkle(event);
    }

    private void scooterAnkle(CommandEvent event) {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        if (swampyGamesConfig == null) {
            return;
        }

        TextChannel swampysChannel = event.getJDA().getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);
        DiscordUser scooterUser = getDiscordUserByMember(event.getMember());

        if (scooterUser.getScooterParticipant() != 0) {
            event.replyError("You have already participated in scooter ankle, you can only do it once per swampy cycle.");
            return;
        }

        if (scooterUser.getXp() < swampyGamesConfig.getScooterAnklePoints()) {
            event.replyError("You can only scooter ankle if you have " + swampyGamesConfig.getScooterAnklePoints() + " points or more.");
            return;
        }

        List<DiscordUser> allUsers = discordUserRepository.findAll();
        List<DiscordUser> filteredUsers = allUsers.stream()
                .filter(u -> u.getXp() > SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD && u.getXp() < scooterUser.getXp())
                .collect(Collectors.toList());

        if (filteredUsers.size() < 1) {
            event.replyError("There were not enough people eligible to receive your scooter ankle points so you can't scooter ankle right now.");
            return;
        }

        long pointsToGive = swampyGamesConfig.getScooterAnklePoints() / filteredUsers.size();

        List<CompletableFuture<?>> futures = new ArrayList<>();
        futures.add(takePointsFromMember(swampyGamesConfig.getScooterAnklePoints(), event.getMember(), PointsStat.SCOOTER));
        futures.add(givePointsToMember(1, event.getMember(), PointsStat.SCOOTER_PARTICIPANT));

        StringBuilder description = new StringBuilder()
                .append(event.getMember().getAsMention())
                .append(" has \uD83D\uDEF4 scooter ankle'd \uD83D\uDEF4 and given ")
                .append(NumberFormat.getIntegerInstance().format(swampyGamesConfig.getScooterAnklePoints()))
                .append(" of their points to everyone below them on the leaderboard.\n\n");

        for (DiscordUser user : filteredUsers) {
            try {
                Member memberById = event.getGuild().getMemberById(user.getId());

                if (memberById != null) {
                    futures.add(givePointsToMember(pointsToGive, memberById, PointsStat.SCOOTER));
                    description.append("Gave ")
                            .append(NumberFormat.getIntegerInstance().format(pointsToGive))
                            .append(" \uD83D\uDEF4 to ")
                            .append(memberById.getAsMention())
                            .append("\n");
                } else {
                    throw new RuntimeException("Unable to find member " + user.getId());
                }
            } catch (Exception e) {
                description.append("Oop, failed to give points to user <@!")
                        .append(user.getId())
                        .append(">\n");
                logger.error("Exception giving points to [user={}] for [scooterUser={}]'s scooter ankle", user.getId(), scooterUser.getId(), e);
                honeybadgerReporter.reportError(e, "Failed to give points to user: " + user.getId() + " for scooter user: " + scooterUser.getId());
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> swampysChannel.sendMessage(Constants.simpleEmbed(
                        "\uD83D\uDEF4 Scooter Ankle \uD83D\uDEF4", description.toString(), swampyGamesConfig.getScooterAnkleImg())).queue());
    }

}
