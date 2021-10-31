package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.service.MinuteTickable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Component;

@Component
public class QuoteTriviaTickable extends NonCommandSwampy implements MinuteTickable {

    @Override
    public void run() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        if (Objects.nonNull(swampyGamesConfig.getQuoteTriviaMsgId())
                && swampyGamesConfig.getQuoteTriviaExpiration().isBefore(LocalDateTime.now())) {
            String quoteTriviaMsgId = swampyGamesConfig.getQuoteTriviaMsgId();
            short quoteTriviaCorrectAnswer = swampyGamesConfig.getQuoteTriviaCorrectAnswer();
            int quoteTriviaEventPoints = swampyGamesConfig.getQuoteTriviaEventPoints();

            swampyGamesConfig.setQuoteTriviaCorrectAnswer(null);
            swampyGamesConfig.setQuoteTriviaMsgId(null);
            swampyGamesConfig.setQuoteTriviaExpiration(null);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);
            Guild guild = philJda.getGuilds().get(0);

            StringBuilder description = new StringBuilder();

            Message msg;
            try {
                msg = swampysChannel.retrieveMessageById(quoteTriviaMsgId).complete();
            } catch (Exception e) {
                swampysChannel.sendMessage("Could not find quote trivia question anymore. Failed to award points.").queue();
                return;
            }

            long wrongPoints = quoteTriviaEventPoints * -1;

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            List<User> users = msg.retrieveReactionUsers("\uD83C\uDDE6").complete();
            awardPoints(description, users, quoteTriviaCorrectAnswer == 0 ? (long) quoteTriviaEventPoints : wrongPoints, futures, guild);

            users = msg.retrieveReactionUsers("\uD83C\uDDE7").complete();
            awardPoints(description, users, quoteTriviaCorrectAnswer == 1 ? (long) quoteTriviaEventPoints : wrongPoints, futures, guild);

            users = msg.retrieveReactionUsers("\uD83C\uDDE8").complete();
            awardPoints(description, users, quoteTriviaCorrectAnswer == 2 ? (long) quoteTriviaEventPoints : wrongPoints, futures, guild);

            msg.clearReactions().queue();

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
                swampysChannel.sendMessageEmbeds(Constants.simpleEmbed("Quote Trivia Results", description.toString())).queue();
            });
        }
    }

    private void awardPoints(StringBuilder description, List<User> users, long points, List<CompletableFuture<Void>> futures, Guild guild) {
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