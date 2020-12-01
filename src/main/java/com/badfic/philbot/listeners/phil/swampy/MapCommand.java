package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.MapQuestionJson;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
public class MapCommand extends BaseSwampy implements PhilMarker {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private List<MapQuestionJson> questions;

    public MapCommand() throws Exception {
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
        Optional<SwampyGamesConfig> optionalConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID);
        if (!optionalConfig.isPresent()) {
            return;
        }
        SwampyGamesConfig swampyGamesConfig = optionalConfig.get();

        TextChannel[] channels = new TextChannel[] {
                philJda.getTextChannelsByName(Constants.DRY_BASTARDS_CHANNEL, false).get(0),
                philJda.getTextChannelsByName(Constants.DRY_CINNAMON_CHANNEL, false).get(0),
                philJda.getTextChannelsByName(Constants.SWAMPY_BASTARD_CHANNEL, false).get(0),
                philJda.getTextChannelsByName(Constants.SWAMPY_CINNAMON_CHANNEL, false).get(0)
        };

        if (swampyGamesConfig.getMapPhrase() != null) {
            event.replyError("There is currently a map trivia running, you can't trigger another.");
            return;
        }

        MapQuestionJson chosenQuestion = Constants.pickRandom(questions);
        swampyGamesConfig.setMapPhrase(chosenQuestion.getAnswer());
        swampyGamesConfig.setMapTriviaExpiration(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(15));
        swampyGamesConfigRepository.save(swampyGamesConfig);

        String description = null;
        String image = null;
        switch (ThreadLocalRandom.current().nextInt(1, 7)) {
            case 1:
                description = '"' + chosenQuestion.getNickname() + "\" is the nickname of what " + chosenQuestion.getIdentifier() + "?";
                break;
            case 2:
                description = chosenQuestion.getCapital() + " is the capital of what " + chosenQuestion.getIdentifier() + "?";
                break;
            case 3:
                description = "This flag represents what " + chosenQuestion.getIdentifier() + "?";
                image = chosenQuestion.getFlagUrl();
                break;
            case 4:
                description = "What " + chosenQuestion.getIdentifier() + " is highlighted on this map?";
                image = chosenQuestion.getMapUrl();
                break;
            case 5:
                description = "This image is a landscape from what " + chosenQuestion.getIdentifier() + "?";
                image = chosenQuestion.getLandscapeUrl();
                break;
            case 6:
                description = "This image is the skyline from what " + chosenQuestion.getIdentifier() + "?";
                image = chosenQuestion.getSkylineUrl();
                break;
        }

        description += "\n\nType your answer here in this channel within the next 15 minutes for " + swampyGamesConfig.getMapEventPoints() + " points.";

        String imageExtension = StringUtils.substring(image, -3);

        if (image != null) {
            try {
                LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
                headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);
                ResponseEntity<byte[]> imageResponse = restTemplate.exchange(image, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);

                for (TextChannel channel : channels) {
                    channel.sendMessage(Constants.simpleEmbed("Map Trivia", description))
                            .addFile(imageResponse.getBody(), "image." + imageExtension)
                            .queue();
                }
            } catch (Exception e) {
                logger.error("Failed to load [image={}] for map trivia", image, e);
                honeybadgerReporter.reportError(e, null, "Failed to load image for map trivia: " + image);
                event.replyError("Failed to load image for map trivia");
            }
        } else {
            for (TextChannel channel : channels) {
                channel.sendMessage(Constants.simpleEmbed("Map Trivia", description)).queue();
            }
        }
    }
}
