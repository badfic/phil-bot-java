package com.badfic.philbot.config;

import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BehradAppConfig {

    private static final String[] PLAYING_STATUS_LIST = {
            "Tekken",
            "in Heyworld",
            "Street Fighter",
            "Overwatch",
            "Super Smash Bros",
            "Dragon Age Origins",
            "Ocarina of Time",
            "Mass Effect",
            "Mario & Sonic at the Olympic Games",
            "Sonic the Hedgehog",
            "Super Mario Bros",
            "Resident Evil",
            "Wii Sports"
    };

    @Value("${BEHRAD_BOT_TOKEN}")
    public String behradBotToken;

    @Resource
    private BaseConfig baseConfig;

    @Bean(name = "behradCommandClient")
    public CommandClient behradCommandClient(List<Command> commands) {
        return new CommandClientBuilder()
                .setOwnerId(baseConfig.ownerId)
                .setPrefix("!!")
                .useHelpBuilder(false)
                .addCommands(commands.stream().filter(c -> c instanceof BehradMarker).toArray(Command[]::new))
                .setActivity(Activity.playing(PLAYING_STATUS_LIST[ThreadLocalRandom.current().nextInt(PLAYING_STATUS_LIST.length)]))
                .setEmojis("✅", "⚠️", "❌")
                .build();
    }

    @Bean(name = "behradJda")
    public JDA behradJda(List<EventListener> eventListeners,
                         @Qualifier("behradCommandClient") CommandClient behradCommandClient) throws Exception {
        return JDABuilder.create(behradBotToken, Collections.singletonList(GUILD_MESSAGES))
                .disableCache(EnumSet.allOf(CacheFlag.class))
                .addEventListeners(eventListeners.stream().filter(e -> e instanceof BehradMarker).toArray(EventListener[]::new))
                .addEventListeners(behradCommandClient)
                .setActivity(Activity.playing(PLAYING_STATUS_LIST[ThreadLocalRandom.current().nextInt(PLAYING_STATUS_LIST.length)]))
                .build();
    }

}
