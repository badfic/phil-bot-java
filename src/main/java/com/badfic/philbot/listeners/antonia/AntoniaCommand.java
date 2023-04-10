package com.badfic.philbot.listeners.antonia;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.antonia.AntoniaResponsesConfig;
import com.badfic.philbot.data.antonia.AntoniaResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Optional;
import java.util.Set;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AntoniaCommand extends BasicResponsesBot<AntoniaResponsesConfig> {

    private final JDA antoniaJda;

    public AntoniaCommand(ObjectMapper objectMapper, AntoniaResponsesConfigRepository antoniaResponsesConfigRepository,
                          @Qualifier("antoniaJda") JDA antoniaJda) {
        super(antoniaResponsesConfigRepository, objectMapper, "antonia", AntoniaResponsesConfig::new);
        this.antoniaJda = antoniaJda;
    }

    @Override
    protected Optional<String> getResponse(CommandEvent event, AntoniaResponsesConfig responsesConfig) {
        String channelName = event.getChannel().getName();
        Set<String> responses;
        if (responsesConfig.getSfwConfig().channels().contains(channelName)) {
            responses = responsesConfig.getSfwConfig().responses();
        } else if (responsesConfig.getNsfwConfig().channels().contains(channelName)) {
            responses = responsesConfig.getNsfwConfig().responses();
        } else {
            return Optional.empty();
        }

        return Optional.of(Constants.pickRandom(responses));
    }

    public JDA getAntoniaJda() {
        return antoniaJda;
    }
}
