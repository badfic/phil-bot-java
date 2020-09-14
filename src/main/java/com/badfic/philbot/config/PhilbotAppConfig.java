package com.badfic.philbot.config;

import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_BANS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGE_REACTIONS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
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
public class PhilbotAppConfig {

    @Value("${PHIL_BOT_TOKEN}")
    public String philBotToken;

    @Resource
    private BaseConfig baseConfig;

    @Bean(name = "philCommandClient")
    public CommandClient philCommandClient(List<Command> commands) {
        return new CommandClientBuilder()
                .setOwnerId(baseConfig.ownerId)
                .setPrefix("!!")
                .useHelpBuilder(false)
                .addCommands(commands.stream().filter(c -> c instanceof PhilMarker).toArray(Command[]::new))
                .setActivity(Activity.playing("with our feelings"))
                .setEmojis("✅", "⚠️", "❌")
                .build();
    }

    @Bean(name = "philJda")
    public JDA philJda(List<EventListener> eventListeners,
                       @Qualifier("philCommandClient") CommandClient philCommandClient) throws Exception {
        return JDABuilder.create(philBotToken, Arrays.asList(GUILD_MEMBERS, GUILD_BANS, GUILD_MESSAGES, GUILD_VOICE_STATES, GUILD_MESSAGE_REACTIONS))
                .disableCache(EnumSet.allOf(CacheFlag.class))
                .enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(eventListeners.stream().filter(e -> e instanceof PhilMarker).toArray(EventListener[]::new))
                .addEventListeners(philCommandClient)
                .setActivity(Activity.playing("with our feelings"))
                .build();
    }

}
