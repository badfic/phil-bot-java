package com.badfic.philbot.listeners.antonia;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.antonia.AntoniaResponsesConfig;
import com.badfic.philbot.data.antonia.AntoniaResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class AntoniaCommand extends BasicResponsesBot<AntoniaResponsesConfig> implements PhilMarker {

    @Resource(name = "antoniaJda")
    @Lazy
    private JDA antoniaJda;

    @Autowired
    public AntoniaCommand(ObjectMapper objectMapper, BaseConfig baseConfig, AntoniaResponsesConfigRepository antoniaResponsesConfigRepository) throws Exception {
        super(baseConfig, antoniaResponsesConfigRepository, objectMapper, "antonia",
                "antonia-kidFriendlyConfig.json", "antonia-nsfwConfig.json", AntoniaResponsesConfig::new);
    }

    @Override
    protected Optional<String> getResponse(CommandEvent event, AntoniaResponsesConfig responsesConfig) {
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

        if (EmojiManager.containsEmoji(msgContent)) {
            Collection<Emoji> allEmoji = EmojiManager.getAll();
            Emoji emoji = pickRandom(allEmoji);
            return Optional.of(emoji.getUnicode());
        }

        return Optional.of(pickRandom(responses));
    }

}
