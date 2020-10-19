package com.badfic.philbot.listeners.antonia;

import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.antonia.AntoniaResponsesConfig;
import com.badfic.philbot.data.antonia.AntoniaResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class AntoniaCommand extends BasicResponsesBot<AntoniaResponsesConfig> implements PhilMarker {

    @Resource(name = "antoniaJda")
    @Lazy
    private JDA antoniaJda;

    @Autowired
    public AntoniaCommand(ObjectMapper objectMapper, AntoniaResponsesConfigRepository antoniaResponsesConfigRepository) throws Exception {
        super(antoniaResponsesConfigRepository, objectMapper, "antonia",
                "antonia-kidFriendlyConfig.json", "antonia-nsfwConfig.json", AntoniaResponsesConfig::new);
    }

    @Override
    protected Optional<String> getResponse(CommandEvent event, AntoniaResponsesConfig responsesConfig) {
        String channelName = event.getChannel().getName();
        Set<String> responses;
        if (responsesConfig.getSfwConfig().getChannels().contains(channelName)) {
            responses = responsesConfig.getSfwConfig().getResponses();
        } else if (responsesConfig.getNsfwConfig().getChannels().contains(channelName)) {
            responses = responsesConfig.getNsfwConfig().getResponses();
        } else {
            return Optional.empty();
        }

        List<Emote> emotes = event.getMessage().getEmotes();
        if (CollectionUtils.isNotEmpty(emotes)) {
            return Optional.of(emotes.get(0).getAsMention());
        }

        return Optional.of(pickRandom(responses));
    }

}
