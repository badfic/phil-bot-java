package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.MapQuestionJson;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.PostConstruct;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
public class MapCommand extends BaseSwampy {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private List<MapQuestionJson> questions;

    public MapCommand() {
        name = "map";
        help = "!!map\nTo trigger a Map trivia question that will last 15 minutes";
        requiredRole = Constants.ADMIN_ROLE;
    }

    @PostConstruct
    public void init() throws Exception {
        questions = Arrays.asList(objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("map-trivia.json"), MapQuestionJson[].class));
    }

    @Override
    protected void execute(CommandEvent event) {
        doMap();
    }

    @Scheduled(cron = "0 37 1,5,9,13,17,21 * * ?", zone = "America/Los_Angeles")
    public void doMap() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                .get(0);

        if (swampyGamesConfig.getMapPhrase() != null) {
            swampysChannel.sendMessage("There is currently a map trivia running, you can't trigger another.").queue();
            return;
        }

        MapQuestionJson chosenQuestion = Constants.pickRandom(questions);
        swampyGamesConfig.setMapPhrase(chosenQuestion.getAnswer());
        swampyGamesConfig.setMapTriviaExpiration(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(15));
        swampyGamesConfigRepository.save(swampyGamesConfig);

        String description = null;
        String image = null;
        switch (ThreadLocalRandom.current().nextInt(1, 5)) {
            case 1 -> description = '"' + chosenQuestion.getNickname() + "\" is the nickname of what " + chosenQuestion.getIdentifier() + "?";
            case 2 -> description = chosenQuestion.getCapital() + " is the capital of what " + chosenQuestion.getIdentifier() + "?";
            case 3 -> {
                description = "This flag represents what " + chosenQuestion.getIdentifier() + "?";
                image = chosenQuestion.getFlagUrl();
            }
            case 4 -> {
                description = "What " + chosenQuestion.getIdentifier() + " is highlighted on this map?";
                image = chosenQuestion.getMapUrl();
            }
        }

        description += "\n\nType your answer here in this channel within the next 15 minutes for " + swampyGamesConfig.getMapEventPoints() + " points.";

        String imageExtension = StringUtils.substring(image, -3);

        if (image != null) {
            try {
                LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
                headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);
                ResponseEntity<byte[]> imageResponse = restTemplate.exchange(image, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);

                swampysChannel.sendMessageEmbeds(Constants.simpleEmbed("Map Trivia", description))
                        .addFile(imageResponse.getBody(), "image." + imageExtension)
                        .queue();
            } catch (Exception e) {
                logger.error("Failed to load [image={}] for map trivia", image, e);
                honeybadgerReporter.reportError(e, null, "Failed to load image for map trivia: " + image);
                swampysChannel.sendMessage("Failed to load image for map trivia. The answer is " + chosenQuestion.getAnswer()).queue();
            }
        } else {
            swampysChannel.sendMessageEmbeds(Constants.simpleEmbed("Map Trivia", description)).queue();
        }
    }
}
