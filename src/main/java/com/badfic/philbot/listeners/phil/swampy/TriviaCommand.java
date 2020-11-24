package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.data.phil.Trivia;
import com.badfic.philbot.data.phil.TriviaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jagrosh.jdautilities.command.CommandEvent;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TriviaCommand extends BaseSwampy implements PhilMarker {

    @Resource
    private TriviaRepository triviaRepository;

    public TriviaCommand() {
        name = "trivia";
        guildOnly = false;
        help = "!!trivia\n" +
                "DM Phil `!!trivia` to get back a link to this admin site where you can add new trivia questions\n" +
                "Manually triggering a trivia question has been disabled";
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
            if (memberById == null || (!hasRole(memberById, Constants.ADMIN_ROLE) && !hasRole(memberById, Constants.MOD_ROLE))) {
                event.replyError("Failed, either you're not an admin or you left the server or the member cache is broken");
                return;
            }

            event.reply("Please login here and click the 'Trivia' link on the navigation bar: " + baseConfig.hostname);
            return;
        }

        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE) && !hasRole(event.getMember(), Constants.MOD_ROLE)) {
            event.replyError("You must be a mod to use this command");
            return;
        }

        if (event.getArgs().startsWith("dump")) {
            List<Trivia> all = triviaRepository.findAll();
            try {
                event.getChannel().sendFile(objectMapper.writeValueAsBytes(all), "trivia.json").queue();
            } catch (JsonProcessingException e) {
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", fatal error could not send sfw config to you").queue();
            }
            return;
        } else if (event.getArgs().startsWith("delete")) {
            String guidStr = event.getArgs().replace("delete", "").trim();
            try {
                UUID guid = UUID.fromString(guidStr);
                if (triviaRepository.existsById(guid)) {
                    triviaRepository.deleteById(guid);
                    event.replySuccess("Successfully deleted " + guid.toString());
                } else {
                    event.replyError("Trivia with id " + guid.toString() + " does not exist.");
                }
            } catch (Exception e) {
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", badly formatted command." +
                        "Example: `!!trivia delete 44e04b28-7d39-41d8-8009-80ca6de5a04a`").queue();
            }

            return;
        }

        if (baseConfig.ownerId.equalsIgnoreCase(event.getAuthor().getId())) {
            trivia();
        } else {
            event.replyError("Unrecognized command. You can no longer trigger a trivia manually.");
        }
    }

    @Scheduled(cron = "0 37,57 1,3,5,7,9,11,13,15,17,19,21,23 * * ?", zone = "GMT")
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

            msg.clearReactions().queue();

            swampyGamesConfig.setTriviaMsgId(null);
            swampyGamesConfig.setTriviaGuid(null);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            swampysChannel.sendMessage(Constants.simpleEmbed("Trivia Results", description.toString())).queue();
            return;
        }

        List<Trivia> allTrivias = triviaRepository.findAll();

        if (CollectionUtils.isEmpty(allTrivias)) {
            swampysChannel.sendMessage("There are no trivia questions").queue();
            return;
        }

        Trivia trivia = Constants.pickRandom(allTrivias);

        String description = trivia.getQuestion() +
                "\nA: " + trivia.getAnswerA() +
                "\nB: " + trivia.getAnswerB() +
                "\nC: " + trivia.getAnswerC();
        swampysChannel.sendMessage(Constants.simpleEmbed("Trivia time!", description)).queue(success -> {
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
