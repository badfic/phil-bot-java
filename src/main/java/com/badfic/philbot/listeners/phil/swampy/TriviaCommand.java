package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.data.phil.Trivia;
import com.badfic.philbot.data.phil.TriviaRepository;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Resource;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
public class TriviaCommand extends BaseSwampy implements PhilMarker {

    @Resource
    private BaseConfig baseConfig;

    @Resource
    private TriviaRepository triviaRepository;

    public TriviaCommand() {
        name = "trivia";
        guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        ChannelType channelType = event.getChannelType();
        if (channelType == ChannelType.PRIVATE) {
            String userId = event.getAuthor().getId();

            Optional<DiscordUser> optionalDiscordUser = discordUserRepository.findById(userId);
            if (!optionalDiscordUser.isPresent()) {
                event.replyError("You are not present in the swampy database. Do you not have an 18+ or chaos children role? " +
                        "Maybe ask santiago, you shouldn't ever get this error");
                return;
            }

            DiscordUser discordUser = optionalDiscordUser.get();

            Member memberById = philJda.getGuilds().get(0).getMemberById(discordUser.getId());
            if (memberById == null || !hasRole(memberById, Constants.ADMIN_ROLE)) {
                event.replyError("Failed, either you're not an admin or you left the server or the member cache is broken");
                return;
            }

            UUID triviaGuid = UUID.randomUUID();
            discordUser.setTriviaGuid(triviaGuid);
            discordUser.setTriviaGuidTime(LocalDateTime.now());

            discordUserRepository.save(discordUser);

            event.reply("Please fill out this form in the next 15 minutes. " + baseConfig.hostname + "/trivia/" + triviaGuid.toString());
            return;
        }

        trivia();
    }

    public void trivia() {
        Optional<SwampyGamesConfig> optionalSwampyGamesConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID);

        if (!optionalSwampyGamesConfig.isPresent()) {
            return;
        }

        SwampyGamesConfig swampyGamesConfig = optionalSwampyGamesConfig.get();
        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);

        if (swampyGamesConfig.getTriviaMsgId() != null) {
            String triviaMsgId = swampyGamesConfig.getTriviaMsgId();
            Optional<Trivia> optTriviaQuestion = triviaRepository.findById(swampyGamesConfig.getTriviaGuid());

            if (!optTriviaQuestion.isPresent()) {
                swampysChannel.sendMessage("Could not find trivia question anymore. Failed to award points.").queue();

                swampyGamesConfig.setTriviaMsgId(null);
                swampyGamesConfig.setTriviaGuid(null);
                swampyGamesConfigRepository.save(swampyGamesConfig);
                return;
            }
            Trivia triviaQuestion = optTriviaQuestion.get();

            StringBuilder description = new StringBuilder();

            Message msg;
            try {
                msg = swampysChannel.retrieveMessageById(triviaMsgId).complete();
            } catch (Exception e) {
                swampysChannel.sendMessage("Could not find trivia question anymore. Failed to award points.").queue();

                swampyGamesConfig.setTriviaMsgId(null);
                swampyGamesConfig.setTriviaGuid(null);
                swampyGamesConfigRepository.save(swampyGamesConfig);
                return;
            }

            List<User> users = msg.retrieveReactionUsers("\uD83C\uDDE6").complete();
            awardPoints(description, users, triviaQuestion.getCorrectAnswer() == 0 ? 100 : -100);

            users = msg.retrieveReactionUsers("\uD83C\uDDE7").complete();
            awardPoints(description, users, triviaQuestion.getCorrectAnswer() == 1 ? 100 : -100);

            users = msg.retrieveReactionUsers("\uD83C\uDDE8").complete();
            awardPoints(description, users, triviaQuestion.getCorrectAnswer() == 2 ? 100 : -100);

            swampyGamesConfig.setTriviaMsgId(null);
            swampyGamesConfig.setTriviaGuid(null);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            swampysChannel.sendMessage(simpleEmbed("Trivia Results", description.toString())).queue();
            return;
        }

        List<Trivia> allTrivias = triviaRepository.findAll();

        if (CollectionUtils.isEmpty(allTrivias)) {
            swampysChannel.sendMessage("There are no trivia questions").queue();
            return;
        }

        Trivia trivia = pickRandom(allTrivias);

        String description = trivia.getQuestion() +
                "\nA: " + trivia.getAnswerA() +
                "\nB: " + trivia.getAnswerB() +
                "\nC: " + trivia.getAnswerC();
        swampysChannel.sendMessage(simpleEmbed("Trivia time!", description)).queue(success -> {
            swampyGamesConfig.setTriviaGuid(trivia.getId());
            swampyGamesConfig.setTriviaMsgId(success.getId());
            swampyGamesConfigRepository.save(swampyGamesConfig);

            success.addReaction("\uD83C\uDDE6").queue();
            success.addReaction("\uD83C\uDDE7").queue();
            success.addReaction("\uD83C\uDDE8").queue();
        });
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
                description.append("Awarded ")
                        .append(points)
                        .append(" points to <@!")
                        .append(user.getId())
                        .append(">\n");
                if (points < 1) {
                    takePointsFromMember(points * -1, memberById);
                } else {
                    givePointsToMember(points, memberById);
                }
            }
        }
    }
}
