package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.data.phil.UsStateJson;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.PostConstruct;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class MapCommand extends BaseSwampy implements PhilMarker {
    private List<UsStateJson> states;

    public MapCommand() throws Exception {
        name = "map";
        help = "!!map\nTo trigger a Map trivia question that will last 15 minutes";
        requiredRole = Constants.ADMIN_ROLE;
    }

    @PostConstruct
    public void init() throws Exception {
        states = Arrays.asList(objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("us-states-trivia.json"), UsStateJson[].class));
    }

    @Override
    protected void execute(CommandEvent event) {
        Optional<SwampyGamesConfig> optionalConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID);
        if (!optionalConfig.isPresent()) {
            return;
        }
        SwampyGamesConfig swampyGamesConfig = optionalConfig.get();

        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                .get(0);

        if (swampyGamesConfig.getMapPhrase() != null) {
            event.replyError("There is currently a map trivia running, you can't trigger another.");
            return;
        }

        UsStateJson chosenState = Constants.pickRandom(states);
        swampyGamesConfig.setMapPhrase(chosenState.getState());
        // TODO Change from 1 to 15
        swampyGamesConfig.setMapTriviaExpiration(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(1));
        swampyGamesConfigRepository.save(swampyGamesConfig);

        String description = null;
        String image = null;
        switch (ThreadLocalRandom.current().nextInt(1, 7)) {
            case 1:
                description = '"' + chosenState.getNickname() + "\" is the nickname of what US state?";
                break;
            case 2:
                description = chosenState.getCapital() + " is the capital of what US state?";
                break;
            case 3:
                description = "This flag represents what US state?";
                image = chosenState.getStateFlagUrl();
                break;
            case 4:
                description = "What US state is highlighted on this map?";
                image = chosenState.getMapUrl();
                break;
            case 5:
                description = "This image is a landscape from what US state?";
                image = chosenState.getLandscapeUrl();
                break;
            case 6:
                description = "This image is the skyline from what US state?";
                image = chosenState.getSkylineUrl();
                break;
        }

        description += "\n\nType your answer here in this channel within the next 15 minutes.";

        String imageExtension = StringUtils.substring(image, -3);

        if (image != null) {
            try {
                swampysChannel.sendMessage(simpleEmbed("US State Trivia", description))
                        .addFile(new URL(image).openStream(), "image." + imageExtension)
                        .queue();
            } catch (IOException e) {
                event.replyError("Failed to load image for map trivia");
            }
        } else {
            swampysChannel.sendMessage(simpleEmbed("US State Trivia", description)).queue();
        }
    }
}
