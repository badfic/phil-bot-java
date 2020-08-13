package com.badfic.philbot.config;

import static net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGE_REACTIONS;
import static net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGE_TYPING;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_EMOJIS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_INVITES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGE_REACTIONS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGE_TYPING;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_PRESENCES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.ACTIVITY;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.CLIENT_STATUS;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.EMOTE;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.VOICE_STATE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import java.util.List;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.EventListener;
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
        return client.build();
    }

    @Bean
    public JDA jda(List<EventListener> eventListeners) throws Exception {
        return JDABuilder.createDefault(botToken)
                .setActivity(Activity.playing("with our feelings"))
                .disableIntents(GUILD_PRESENCES, GUILD_MESSAGE_TYPING, GUILD_MESSAGE_REACTIONS, GUILD_MESSAGE_TYPING, GUILD_EMOJIS, GUILD_INVITES,
                        GUILD_VOICE_STATES, DIRECT_MESSAGES, DIRECT_MESSAGE_REACTIONS, DIRECT_MESSAGE_TYPING)
                .disableCache(VOICE_STATE, ACTIVITY, CLIENT_STATUS, EMOTE)
                .addEventListeners(eventListeners.toArray(new EventListener[0]))
                .build();
    }

}
