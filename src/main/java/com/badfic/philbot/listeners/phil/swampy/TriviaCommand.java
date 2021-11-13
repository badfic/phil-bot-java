package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.data.phil.Trivia;
import com.badfic.philbot.data.phil.TriviaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import javax.annotation.Resource;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TriviaCommand extends BaseSwampy {

    @Resource
    private TriviaRepository triviaRepository;

    public TriviaCommand() {
        name = "trivia";
        requiredRole = Constants.ADMIN_ROLE;
        help = """
                `!!trivia dump` returns a json file with all the trivia questions and answers.
                `!!trivia delete 44e04b28-7d39-41d8-8009-80ca6de5a04a` deleted trivia question with id 44e04b28-7d39-41d8-8009-80ca6de5a04a""";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().startsWith("dump")) {
            List<Trivia> all = triviaRepository.findAll();
            try {
                event.getChannel().sendFile(objectMapper.writeValueAsBytes(all), "trivia.json").queue();
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
            swampyGamesConfig.setTriviaExpiration(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(15));
            swampyGamesConfigRepository.save(swampyGamesConfig);

            success.addReaction("\uD83C\uDDE6").queue();
            success.addReaction("\uD83C\uDDE7").queue();
            success.addReaction("\uD83C\uDDE8").queue();
        });
    }
}
