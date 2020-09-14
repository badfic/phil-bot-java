package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.PhilResponsesConfig;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.badfic.philbot.repository.PhilResponsesConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PhilCommand extends BasicResponsesBot<PhilResponsesConfig> implements PhilMarker {

    @Autowired
    public PhilCommand(ObjectMapper objectMapper, PhilResponsesConfigRepository philResponsesConfigRepository, BaseConfig baseConfig,
                       CloseableHttpClient gfycatClient) throws Exception {
        super(baseConfig, philResponsesConfigRepository, gfycatClient, objectMapper, "phil",
                "phil-kidFriendlyConfig.json", "phil-nsfwConfig.json", PhilResponsesConfig::new);
    }

    @Override
    protected Optional<String> getResponse(CommandEvent event, PhilResponsesConfig philResponsesConfig) {
        String msgContent = event.getMessage().getContentRaw();
        String channelName = event.getChannel().getName();
        Set<String> responses;
        if (philResponsesConfig.getSfwConfig().getChannels().contains(channelName)) {
            responses = philResponsesConfig.getSfwConfig().getResponses();
        } else if (philResponsesConfig.getNsfwConfig().getChannels().contains(channelName)) {
            responses = philResponsesConfig.getNsfwConfig().getResponses();

            if (StringUtils.containsIgnoreCase(msgContent, "you suck")) {
                return Optional.of("you swallow");
            }
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
