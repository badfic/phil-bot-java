package com.badfic.philbot.commands.bang.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.TriviaRepository;
import com.badfic.philbot.service.OnJdaReady;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TriviaCommand extends BaseBangCommand implements OnJdaReady {

    private final TriviaRepository triviaRepository;

    public TriviaCommand(final TriviaRepository triviaRepository) {
        name = "trivia";
        requiredRole = Constants.ADMIN_ROLE;
        help = """
                `!!trivia dump` returns a json file with all the trivia questions and answers.
                `!!trivia delete 44e04b28-7d39-41d8-8009-80ca6de5a04a` deleted trivia question with id 44e04b28-7d39-41d8-8009-80ca6de5a04a""";
        this.triviaRepository = triviaRepository;
    }

    @Override
    public void run() {
        final var swampyGamesConfig = getSwampyGamesConfig();

        if (Objects.nonNull(swampyGamesConfig.getTriviaExpiration())) {
            scheduleTask(this::triviaComplete, swampyGamesConfig.getTriviaExpiration());
        }
    }

    @Override
    public void execute(final CommandEvent event) {
        if (event.getArgs().startsWith("dump")) {
            final var all = triviaRepository.findAll();
            try {
                event.getChannel().sendFiles(FileUpload.fromData(objectMapper.writeValueAsBytes(all), "trivia.json")).queue();
            } catch (final JsonProcessingException e) {
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", fatal error could not send trivia dump to you").queue();
            }
        } else if (event.getArgs().startsWith("delete")) {
            final var guidStr = event.getArgs().replace("delete", "").trim();
            try {
                final var guid = UUID.fromString(guidStr);
                if (triviaRepository.existsById(guid)) {
                    triviaRepository.deleteById(guid);
                    event.replySuccess("Successfully deleted " + guid);
                } else {
                    event.replyError("Trivia with id " + guid + " does not exist.");
                }
            } catch (final Exception e) {
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", badly formatted command." +
                        "Example: `!!trivia delete 44e04b28-7d39-41d8-8009-80ca6de5a04a`").queue();
            }
        } else {
            event.replyError("Unrecognized trivia command. Trivia can't be triggered manually.");
        }
    }

    @Scheduled(cron = "${swampy.schedule.events.trivia}", zone = "${swampy.schedule.timezone}")
    private void onSchedule() {
        Thread.startVirtualThread(this::trivia);
    }

    private void trivia() {
        final var swampyGamesConfig = getSwampyGamesConfig();
        final var swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).getFirst();

        if (swampyGamesConfig.getTriviaGuid() != null) {
            swampysChannel.sendMessage("There is currently a trivia running, you can't trigger another.").queue();
            return;
        }

        final var allTrivias = triviaRepository.findAll();

        if (CollectionUtils.isEmpty(allTrivias)) {
            swampysChannel.sendMessage("There are no trivia questions").queue();
            return;
        }

        final var trivia = Constants.pickRandom(allTrivias);

        final var description = trivia.getQuestion() +
                "\nA: " + trivia.getAnswerA() +
                "\nB: " + trivia.getAnswerB() +
                "\nC: " + trivia.getAnswerC();
        final var title = "Trivia time! (for " + swampyGamesConfig.getTriviaEventPoints() + " points)";
        swampysChannel.sendMessageEmbeds(Constants.simpleEmbed(title, description)).queue(success -> {
            swampyGamesConfig.setTriviaGuid(trivia.getId());
            swampyGamesConfig.setTriviaMsgId(success.getId());
            final var expiration = LocalDateTime.now().plusMinutes(15);
            swampyGamesConfig.setTriviaExpiration(expiration);
            saveSwampyGamesConfig(swampyGamesConfig);

            scheduleTask(this::triviaComplete, expiration);

            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE6")).queue();
            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE7")).queue();
            success.addReaction(Emoji.fromUnicode("\uD83C\uDDE8")).queue();
        });
    }

    private void triviaComplete() {
        final var swampyGamesConfig = getSwampyGamesConfig();
        final var swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).getFirst();

        if (Objects.isNull(swampyGamesConfig.getTriviaMsgId())) {
            swampysChannel.sendMessage("Could not find trivia question anymore. Failed to award points.").queue();
            return;
        }

        final var triviaMsgId = swampyGamesConfig.getTriviaMsgId();
        final var triviaGuid = swampyGamesConfig.getTriviaGuid();
        final var triviaEventPoints = swampyGamesConfig.getTriviaEventPoints();

        swampyGamesConfig.setTriviaMsgId(null);
        swampyGamesConfig.setTriviaGuid(null);
        swampyGamesConfig.setTriviaExpiration(null);
        saveSwampyGamesConfig(swampyGamesConfig);

        final var guild = philJda.getGuildById(baseConfig.guildId);

        final var optTriviaQuestion = triviaRepository.findById(triviaGuid);

        if (optTriviaQuestion.isEmpty()) {
            swampysChannel.sendMessage("Could not find trivia question anymore. Failed to award points.").queue();
            return;
        }
        final var triviaQuestion = optTriviaQuestion.get();

        final var description = new StringBuilder();

        final Message msg;
        try {
            msg = swampysChannel.retrieveMessageById(triviaMsgId).timeout(30, TimeUnit.SECONDS).complete();
        } catch (final Exception e) {
            swampysChannel.sendMessage("Could not find trivia question anymore. Failed to award points.").queue();
            return;
        }

        final var wrongPoints = triviaEventPoints * -1;

        final var futures = new ArrayList<CompletableFuture<?>>();

        var users = msg.retrieveReactionUsers(Emoji.fromUnicode("\uD83C\uDDE6")).timeout(30, TimeUnit.SECONDS).complete();
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

    private void awardPoints(final StringBuilder description, final List<User> users, final long points, final List<CompletableFuture<?>> futures, final Guild guild) {
        for (final var user : users) {
            if (user.getId().equals(philJda.getSelfUser().getId())) {
                continue;
            }

            final var memberById = guild.getMemberById(user.getId());
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
