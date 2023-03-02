package com.badfic.philbot.listeners.john;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.john.JohnResponsesConfig;
import com.badfic.philbot.data.john.JohnResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.CommandEvent;
import io.honeybadger.reporter.HoneybadgerReporter;
import java.util.Optional;
import java.util.Set;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class JohnCommand extends BasicResponsesBot<JohnResponsesConfig> {

    @Autowired
    @Qualifier("johnJda")
    @Lazy
    private JDA johnJda;

    @Autowired
    public JohnCommand(ObjectMapper objectMapper, HoneybadgerReporter honeybadgerReporter, JohnResponsesConfigRepository johnResponsesConfigRepository)
            throws Exception {
        super(johnResponsesConfigRepository, objectMapper, honeybadgerReporter, "john", "john-kidFriendlyConfig.json", "john-nsfwConfig.json",
                JohnResponsesConfig::new);
    }

    @Override
    protected Optional<String> getResponse(CommandEvent event, JohnResponsesConfig responsesConfig) {
        String msgContent = event.getMessage().getContentRaw();
        String channelName = event.getChannel().getName();
        Set<String> responses;
        if (responsesConfig.getSfwConfig().getChannels().contains(channelName)) {
            responses = responsesConfig.getSfwConfig().getResponses();
        } else if (responsesConfig.getNsfwConfig().getChannels().contains(channelName)) {
            responses = responsesConfig.getNsfwConfig().getResponses();
        } else {
            return Optional.empty();
        }

        return Optional.of(Constants.pickRandom(responses));
    }

    public JDA getJohnJda() {
        return johnJda;
    }
}
