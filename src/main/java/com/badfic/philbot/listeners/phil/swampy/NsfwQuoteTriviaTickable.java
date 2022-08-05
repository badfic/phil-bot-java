package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.service.MinuteTickable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Component;

@Component
public class NsfwQuoteTriviaTickable extends NonCommandSwampy implements MinuteTickable {

    @Override
    public void runMinutelyTask() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        if (Objects.nonNull(swampyGamesConfig.getNsfwQuoteTriviaMsgId())
                && swampyGamesConfig.getNsfwQuoteTriviaExpiration().isBefore(LocalDateTime.now())) {
            String quoteTriviaMsgId = swampyGamesConfig.getNsfwQuoteTriviaMsgId();
            short quoteTriviaCorrectAnswer = swampyGamesConfig.getNsfwQuoteTriviaCorrectAnswer();
            int quoteTriviaEventPoints = swampyGamesConfig.getNsfwQuoteTriviaEventPoints();

            swampyGamesConfig.setNsfwQuoteTriviaCorrectAnswer(null);
            swampyGamesConfig.setNsfwQuoteTriviaMsgId(null);
            swampyGamesConfig.setNsfwQuoteTriviaExpiration(null);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.CURSED_SWAMP_CHANNEL, false).get(0);
            Guild guild = philJda.getGuilds().get(0);

            StringBuilder description = new StringBuilder();

            Message msg;
            try {
                msg = swampysChannel.retrieveMessageById(quoteTriviaMsgId).timeout(30, TimeUnit.SECONDS).complete();
            } catch (Exception e) {
                swampysChannel.sendMessage("Could not find nsfw quote trivia question anymore. Failed to award points.").queue();
                return;
            }

            long wrongPoints = quoteTriviaEventPoints * -1;

            List<CompletableFuture<?>> futures = new ArrayList<>();

            List<User> users = msg.retrieveReactionUsers("\uD83C\uDDE6").timeout(30, TimeUnit.SECONDS).complete();
            awardPoints(description, users, quoteTriviaCorrectAnswer == 0 ? (long) quoteTriviaEventPoints : wrongPoints, futures, guild);

            users = msg.retrieveReactionUsers("\uD83C\uDDE7").timeout(30, TimeUnit.SECONDS).complete();
            awardPoints(description, users, quoteTriviaCorrectAnswer == 1 ? (long) quoteTriviaEventPoints : wrongPoints, futures, guild);

            users = msg.retrieveReactionUsers("\uD83C\uDDE8").timeout(30, TimeUnit.SECONDS).complete();
            awardPoints(description, users, quoteTriviaCorrectAnswer == 2 ? (long) quoteTriviaEventPoints : wrongPoints, futures, guild);

            msg.clearReactions().queue();

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
                swampysChannel.sendMessageEmbeds(Constants.simpleEmbed("\uD83C\uDF46 Quote Trivia Results", description.toString())).queue();
            });
        }
    }

    private void awardPoints(StringBuilder description, List<User> users, long points, List<CompletableFuture<?>> futures, Guild guild) {
        for (User user : users) {
            if (user.getId().equals(philJda.getSelfUser().getId())) {
                continue;
            }

            Member memberById = guild.getMemberById(user.getId());
            if (memberById == null) {
                description.append("Could not award points to <@!")
                        .append(user.getId())
                        .append(">\n");
            } else {
                if (isNotParticipating(memberById)) {
                    description.append("<@!")
                            .append(user.getId())
                            .append("> Please ask a mod to check your roles to participate in the swampys\n");
                } else {
                    description.append("Awarded ")
                            .append(points)
                            .append(" points to <@!")
                            .append(user.getId())
                            .append(">\n");
                }
                if (points < 1) {
                    futures.add(takePointsFromMember(points * -1, memberById, PointsStat.QUOTE_TRIVIA));
                } else {
                    futures.add(givePointsToMember(points, memberById, PointsStat.QUOTE_TRIVIA));
                }
            }
        }
    }
}
