package com.badfic.philbot.commands.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.Trivia;
import com.badfic.philbot.data.TriviaRepository;
import com.badfic.philbot.service.OnJdaReady;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
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
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TriviaCommand extends BaseNormalCommand implements OnJdaReady {

    private final TriviaRepository triviaRepository;

    public TriviaCommand(TriviaRepository triviaRepository) {
        name = "trivia";
        requiredRole = Constants.ADMIN_ROLE;
        help = """
                `!!trivia dump` returns a json file with all the trivia questions and answers.
                `!!trivia delete 44e04b28-7d39-41d8-8009-80ca6de5a04a` deleted trivia question with id 44e04b28-7d39-41d8-8009-80ca6de5a04a""";
        this.triviaRepository = triviaRepository;
    }

    @Override
    public void run() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        if (Objects.nonNull(swampyGamesConfig.getTriviaExpiration())) {
            taskScheduler.schedule(this::triviaComplete, swampyGamesConfig.getTriviaExpiration().toInstant(ZoneOffset.UTC));
        }
    }

    @Override
    public void execute(CommandEvent event) {
        if (event.getArgs().startsWith("dump")) {
            List<Trivia> all = triviaRepository.findAll();
            try {
                event.getChannel().sendFiles(FileUpload.fromData(objectMapper.writeValueAsBytes(all), "trivia.json")).queue();
            } catch (JsonProcessingException e) {
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", fatal error could not send trivia dump to you").queue();
            }
        } else if (event.getArgs().startsWith("delete")) {
            String guidStr = event.getArgs().replace("delete", "").trim();
            try {
                UUID guid = UUID.fromString(guidStr);
                if (triviaRepository.existsById(guid)) {
                    triviaRepository.deleteById(guid);
                    event.replySuccess("Successfully deleted " + guid);
                } else {
                    event.replyError("Trivia with id " + guid + " does not exist.");
                }
            } catch (Exception e) {
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", badly formatted command." +
                        "Example: `!!trivia delete 44e04b28-7d39-41d8-8009-80ca6de5a04a`").queue();
            }
        } else {
            event.replyError("Unrecognized trivia command. Trivia can't be triggered manually.");
        }
    }

    @Scheduled(cron = "${swampy.schedule.events.trivia}", zone = "${swampy.schedule.timezone}")
    public void trivia() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);

        if (swampyGamesConfig.getTriviaGuid() != null) {
            swampysChannel.sendMessage("There is currently a trivia running, you can't trigger another.").queue();
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
        String title = "Trivia time! (for " + swampyGamesConfig.getTriviaEventPoints() + " points)";
        swampysChannel.sendMessageEmbeds(Constants.simpleEmbed(title, description)).queue(success -> {
            swampyGamesConfig.setTriviaGuid(trivia.getId());
            swampyGamesConfig.setTriviaMsgId(success.getId());
            LocalDateTime expiration = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(15);
            swampyGamesConfig.setTriviaExpiration(expiration);
            saveSwampyGamesConfig(swampyGamesConfig);

            taskScheduler.schedule(this::triviaComplete, expiration.toInstant(ZoneOffset.UTC));

            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE6")).queue();
            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE7")).queue();
            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE8")).queue();
        });
    }

    private void triviaComplete() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);

        if (Objects.isNull(swampyGamesConfig.getTriviaMsgId())) {
            swampysChannel.sendMessage("Could not find trivia question anymore. Failed to award points.").queue();
            return;
        }

        String triviaMsgId = swampyGamesConfig.getTriviaMsgId();
        UUID triviaGuid = swampyGamesConfig.getTriviaGuid();
        int triviaEventPoints = swampyGamesConfig.getTriviaEventPoints();

        swampyGamesConfig.setTriviaMsgId(null);
        swampyGamesConfig.setTriviaGuid(null);
        swampyGamesConfig.setTriviaExpiration(null);
        saveSwampyGamesConfig(swampyGamesConfig);


        Guild guild = philJda.getGuildById(baseConfig.guildId);

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

    private void awardPoints(StringBuilder description, List<User> users, long points, List<CompletableFuture<?>> futures, Guild guild) {
        for (User user : users) {
            if (user.getId().equals(philJda.getSelfUser().getId())) {
                continue;
            }

            Member memberById = guild.getMemberById(user.getId());
            if (memberById == null) {
                description.append("Could not award points to <@")
                        .append(user.getId())
                        .append(">\n");
            } else {
                if (isNotParticipating(memberById)) {
                    description.append("<@")
                            .append(user.getId())
                            .append("> Please ask a mod to check your roles to participate in the swampys\n");
                } else {
                    description.append("Awarded ")
                            .append(points)
                            .append(" points to <@")
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
