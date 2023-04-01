package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.PhilResponsesConfig;
import com.badfic.philbot.data.phil.PhilResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.CommandEvent;
import io.honeybadger.reporter.HoneybadgerReporter;
import java.util.Optional;
import java.util.Set;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PhilCommand extends BasicResponsesBot<PhilResponsesConfig> {

    @Setter(onMethod_ = {@Autowired, @Qualifier("philJda"), @Lazy})
    private JDA philJda;

    public PhilCommand(ObjectMapper objectMapper, HoneybadgerReporter honeybadgerReporter, PhilResponsesConfigRepository philResponsesConfigRepository) {
        super(philResponsesConfigRepository, objectMapper, honeybadgerReporter, "phil", PhilResponsesConfig::new);
    }

    @Scheduled(cron = "${swampy.schedule.phil.humpday}", zone = "${swampy.schedule.timezone}")
    public void goodMorning() {
        TextChannel general = philJda.getTextChannelsByName("general", false).get(0);
        general.sendMessage("https://cdn.discordapp.com/attachments/323666308107599872/834310518478471218/mullethumpygrinch.png").queue();
    }

    @Override
    protected Optional<String> getResponse(CommandEvent event, PhilResponsesConfig philResponsesConfig) {
        String msgContent = event.getMessage().getContentRaw();
        String channelName = event.getChannel().getName();
        Set<String> responses;
        if (philResponsesConfig.getSfwConfig().channels().contains(channelName)) {
            responses = philResponsesConfig.getSfwConfig().responses();
        } else if (philResponsesConfig.getNsfwConfig().channels().contains(channelName)) {
            responses = philResponsesConfig.getNsfwConfig().responses();

            if (StringUtils.containsIgnoreCase(msgContent, "you suck")) {
                return Optional.of("you swallow");
            }
        } else {
            return Optional.empty();
        }

        boolean isAllUppercase = true;
        for (String s : msgContent.split("\\s+")) {
            isAllUppercase &= StringUtils.isAllUpperCase(s);
        }
        if (isAllUppercase) {
            return Optional.of(StringUtils.upperCase(Constants.pickRandom(responses)));
        }

        return Optional.of(Constants.pickRandom(responses));
    }

}
