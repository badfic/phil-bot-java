package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.data.phil.Trivia;
import com.badfic.philbot.data.phil.TriviaRepository;
import com.badfic.philbot.service.MinuteTickable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Resource;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Component;

@Component
public class TriviaTickable extends NonCommandSwampy implements MinuteTickable {

    @Resource
    private TriviaRepository triviaRepository;

    @Override
    public void run() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);

        if (Objects.nonNull(swampyGamesConfig.getTriviaMsgId())
                && swampyGamesConfig.getTriviaExpiration().isBefore(LocalDateTime.now())) {
            String triviaMsgId = swampyGamesConfig.getTriviaMsgId();
            UUID triviaGuid = swampyGamesConfig.getTriviaGuid();
            int triviaEventPoints = swampyGamesConfig.getTriviaEventPoints();

            swampyGamesConfig.setTriviaMsgId(null);
            swampyGamesConfig.setTriviaGuid(null);
            swampyGamesConfig.setTriviaExpiration(null);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            Optional<Trivia> optTriviaQuestion = triviaRepository.findById(triviaGuid);

            if (optTriviaQuestion.isEmpty()) {
                swampysChannel.sendMessage("Could not find trivia question anymore. Failed to award points.").queue();
                return;
            }
            Trivia triviaQuestion = optTriviaQuestion.get();

            StringBuilder description = new StringBuilder();

            Message msg;
            try {
                msg = swampysChannel.retrieveMessageById(triviaMsgId).complete();
            } catch (Exception e) {
                swampysChannel.sendMessage("Could not find trivia question anymore. Failed to award points.").queue();
                return;
            }

            long wrongPoints = triviaEventPoints * -1;

            List<User> users = msg.retrieveReactionUsers("\uD83C\uDDE6").complete();
            awardPoints(description, users, triviaQuestion.getCorrectAnswer() == 0 ? (long) triviaEventPoints : wrongPoints);

            users = msg.retrieveReactionUsers("\uD83C\uDDE7").complete();
            awardPoints(description, users, triviaQuestion.getCorrectAnswer() == 1 ? (long) triviaEventPoints : wrongPoints);

            users = msg.retrieveReactionUsers("\uD83C\uDDE8").complete();
            awardPoints(description, users, triviaQuestion.getCorrectAnswer() == 2 ? (long) triviaEventPoints : wrongPoints);

            msg.clearReactions().queue();

            swampysChannel.sendMessage(Constants.simpleEmbed("Trivia Results", description.toString())).queue();
        }
    }

    private void awardPoints(StringBuilder description, List<User> users, long points) {
        for (User user : users) {
            if (user.getId().equals(philJda.getSelfUser().getId())) {
                continue;
            }

            Member memberById = philJda.getGuilds().get(0).getMemberById(user.getId());
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
                    takePointsFromMember(points * -1, memberById, PointsStat.TRIVIA);
                } else {
                    givePointsToMember(points, memberById, PointsStat.TRIVIA);
                }
            }
        }
    }
}
