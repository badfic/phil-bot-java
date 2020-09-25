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
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PhilCommand extends BasicResponsesBot<PhilResponsesConfig> implements PhilMarker {

    @Resource(name = "philJda")
    @Lazy
    private JDA philJda;

    @Autowired
    public PhilCommand(ObjectMapper objectMapper, PhilResponsesConfigRepository philResponsesConfigRepository, BaseConfig baseConfig,
                       CloseableHttpClient gfycatClient) throws Exception {
        super(baseConfig, philResponsesConfigRepository, gfycatClient, objectMapper, "phil",
                "phil-kidFriendlyConfig.json", "phil-nsfwConfig.json", PhilResponsesConfig::new);
    }

    @Scheduled(cron = "0 1 17 * * WED", zone = "GMT")
    public void goodMorning() {
        TextChannel general = philJda.getTextChannelsByName("general", false).get(0);
        general.sendMessage("https://cdn.discordapp.com/attachments/741792267609702461/755988086399434793/unknown.png").queue();
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

        if (StringUtils.isAllUpperCase(event.getMessage().getContentRaw())) {
            return Optional.of(StringUtils.upperCase(pickRandom(responses)));
        }

        return Optional.of(pickRandom(responses));
    }

}
