package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.PhilResponsesConfig;
import com.badfic.philbot.data.phil.PhilResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
    public PhilCommand(ObjectMapper objectMapper, PhilResponsesConfigRepository philResponsesConfigRepository) throws Exception {
        super(philResponsesConfigRepository, objectMapper, "phil",
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

        List<Emote> emotes = event.getMessage().getEmotes();
        if (CollectionUtils.isNotEmpty(emotes)) {
            return Optional.of(emotes.get(0).getAsMention());
        }

        boolean isAllUppercase = true;
        for (String s : msgContent.split("\\s+")) {
            isAllUppercase &= StringUtils.isAllUpperCase(s);
        }
        if (isAllUppercase) {
            return Optional.of(StringUtils.upperCase(pickRandom(responses)));
        }

        return Optional.of(pickRandom(responses));
    }

}
