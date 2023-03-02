package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.data.phil.Trivia;
import com.badfic.philbot.data.phil.TriviaRepository;
import com.badfic.philbot.service.MinuteTickable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TriviaTickable extends NonCommandSwampy implements MinuteTickable {

    @Autowired
    private TriviaRepository triviaRepository;

    @Override
    public void runMinutelyTask() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        if (Objects.nonNull(swampyGamesConfig.getTriviaMsgId())
                && swampyGamesConfig.getTriviaExpiration().isBefore(LocalDateTime.now())) {
            String triviaMsgId = swampyGamesConfig.getTriviaMsgId();
            UUID triviaGuid = swampyGamesConfig.getTriviaGuid();
            int triviaEventPoints = swampyGamesConfig.getTriviaEventPoints();

            swampyGamesConfig.setTriviaMsgId(null);
            swampyGamesConfig.setTriviaGuid(null);
            swampyGamesConfig.setTriviaExpiration(null);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);
            Guild guild = philJda.getGuilds().get(0);

            Optional<Trivia> optTriviaQuestion = triviaRepository.findById(triviaGuid);

            if (optTriviaQuestion.isEmpty()) {
                swampysChannel.sendMessage("Could not find trivia question anymore. Failed to award points.").queue();
                return;
            }
            Trivia triviaQuestion = optTriviaQuestion.get();

            StringBuilder description = new StringBuilder();

            Message msg;
            try {
                msg = swampysChannel.retrieveMessageById(triviaMsgId).timeout(30, TimeUnit.SECONDS).complete();
            } catch (Exception e) {
                swampysChannel.sendMessage("Could not find trivia question anymore. Failed to award points.").queue();
                return;
            }

            long wrongPoints = triviaEventPoints * -1;

            List<CompletableFuture<?>> futures = new ArrayList<>();

            List<User> users = msg.retrieveReactionUsers(Emoji.fromUnicode("\uD83C\uDDE6")).timeout(30, TimeUnit.SECONDS).complete();
            awardPoints(description, users, triviaQuestion.getCorrectAnswer() == 0 ? (long) triviaEventPoints : wrongPoints, futures, guild);

            users = msg.retrieveReactionUsers(Emoji.fromUnicode("\uD83C\uDDE7")).timeout(30, TimeUnit.SECONDS).complete();
            awardPoints(description, users, triviaQuestion.getCorrectAnswer() == 1 ? (long) triviaEventPoints : wrongPoints, futures, guild);

            users = msg.retrieveReactionUsers(Emoji.fromUnicode("\uD83C\uDDE8")).timeout(30, TimeUnit.SECONDS).complete();
            awardPoints(description, users, triviaQuestion.getCorrectAnswer() == 2 ? (long) triviaEventPoints : wrongPoints, futures, guild);

            msg.clearReactions().queue();

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
                swampysChannel.sendMessageEmbeds(Constants.simpleEmbed("Trivia Results", description.toString())).queue();
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
                    futures.add(takePointsFromMember(points * -1, memberById, PointsStat.TRIVIA));
                } else {
                    futures.add(givePointsToMember(points, memberById, PointsStat.TRIVIA));
                }
            }
        }
    }
}
