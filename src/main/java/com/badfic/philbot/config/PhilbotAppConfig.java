package com.badfic.philbot.config;

import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_BANS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PhilbotAppConfig {

    @Value("${BOT_TOKEN}")
    private String botToken;

    @Value("${NODE_ENV:test}")
    public String nodeEnvironment;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public CommandClient commandClient(List<Command> commands) {
        CommandClientBuilder client = new CommandClientBuilder();
        client.setOwnerId("741504913422417992");
        client.setPrefix("!!");
        client.addCommands(commands.toArray(new Command[0]));
        client.setActivity(Activity.playing("with our feelings"));
        return client.build();
    }

    @Bean
    public JDA jda(List<EventListener> eventListeners) throws Exception {
        return JDABuilder.create(botToken, Arrays.asList(GUILD_MEMBERS, GUILD_BANS, GUILD_MESSAGES))
                .disableCache(EnumSet.allOf(CacheFlag.class))
                .addEventListeners(eventListeners.toArray(new EventListener[0]))
                .setActivity(Activity.playing("with our feelings"))
                .build();
    }

}
