package com.badfic.philbot.config;

import static net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import java.util.Arrays;
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
public class KeanuAppConfig {

    private static final String[] WATCHING_STATUS_LIST = {
            "Bill & Ted Face the Music",
            "The SpongeBob Movie: Sponge on the Run",
            "Toy Story 4",
            "Always Be My Maybe",
            "John Wick: Chapter 3 - Parabellum",
            "Replicas",
            "Destination Wedding",
            "Siberia",
            "John Wick: Chapter 2",
            "Keanu",
            "John Wick",
            "Constantine",
            "Something's Gotta Give",
            "The Matrix Revolutions",
            "The Matrix Reloaded",
            "The Matrix",
            "Speed",
            "Bill & Ted's Bogus Journey",
            "Bill & Ted's Excellent Adventure"
    };

    @Value("${KEANU_BOT_TOKEN}")
    public String keanuBotToken;

    @Resource
    private BaseConfig baseConfig;

    @Bean(name = "keanuCommandClient")
    public CommandClient keanuCommandClient(List<Command> commands) {
        return new CommandClientBuilder()
                .setOwnerId(baseConfig.ownerId)
                .setPrefix("!!")
                .useHelpBuilder(false)
                .addCommands(commands.stream().filter(c -> c instanceof KeanuMarker).toArray(Command[]::new))
                .setActivity(Activity.watching(WATCHING_STATUS_LIST[ThreadLocalRandom.current().nextInt(WATCHING_STATUS_LIST.length)]))
                .setEmojis("✅", "⚠️", "❌")
                .build();
    }

    @Bean(name = "keanuJda")
    public JDA keanuJda(List<EventListener> eventListeners,
                        @Qualifier("keanuCommandClient") CommandClient keanuCommandClient) throws Exception {
        return JDABuilder.create(keanuBotToken, Arrays.asList(GUILD_MESSAGES, DIRECT_MESSAGES))
                .disableCache(EnumSet.allOf(CacheFlag.class))
                .addEventListeners(eventListeners.stream().filter(e -> e instanceof KeanuMarker).toArray(EventListener[]::new))
                .addEventListeners(keanuCommandClient)
                .setActivity(Activity.watching(WATCHING_STATUS_LIST[ThreadLocalRandom.current().nextInt(WATCHING_STATUS_LIST.length)]))
                .build();
    }

}
