package com.badfic.philbot.config;

import static net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_BANS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGE_REACTIONS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class BaseConfig {

    private static final String[] KEANU_STATUS_LIST = {
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

    private static final String[] BEHRAD_STATUS_LIST = {
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

    private static final String[] ANTONIA_STATUS_LIST = {
            "Back in Black - AC/DC",
            "Highway to Hell - AC/DC",
            "You Shook Me All Night Long AC/DC",
            "Shoot to Thrill - AC/DC",
            "Driving With the Top Down - Ryeland Allison",
            "Iron Man - Black Sabbath",
            "For Whom the Bell Tolls - Metallica",
            "Amerika - Rammstein",
            "Back in the Saddle - Aerosmith",
            "Irresistible - Fall Out Boy",
            "Mad Sounds - Arctic Monkeys",
            "I Want it All - Queen",
            "Rock and Roll - Led Zeppelin",
            "Mr. Brightside - The Killers",
            "I Write Sins not Tragedies - Panic! At the Disco",
            "Sugar We're Goin Down - Fall Out Boy"
    };

    @Value("${PHIL_BOT_TOKEN}")
    public String philBotToken;

    @Value("${KEANU_BOT_TOKEN}")
    public String keanuBotToken;

    @Value("${BEHRAD_BOT_TOKEN}")
    public String behradBotToken;

    @Value("${ANTONIA_BOT_TOKEN}")
    public String antoniaBotToken;

    @Value("${git.commit.id}")
    public String commitSha;

    @Value("${git.commit.message.short}")
    public String commitMessage;

    @Value("${OWNER_ID}")
    public String ownerId;

    @Bean
    public ScheduledExecutorService childBotExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Bean
    public OkHttpClient childBotOkHttpClient() {
        return IOUtil.newHttpClientBuilder().build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propsConfig
                = new PropertySourcesPlaceholderConfigurer();
        propsConfig.setLocation(new ClassPathResource("git.properties"));
        propsConfig.setIgnoreResourceNotFound(true);
        propsConfig.setIgnoreUnresolvablePlaceholders(true);
        return propsConfig;
    }

    @Bean(name = "antoniaJda")
    public JDA antoniaJda() throws Exception {
        return JDABuilder.createLight(antoniaBotToken, Collections.emptyList())
                .setRateLimitPool(childBotExecutor(), false)
                .setCallbackPool(childBotExecutor(), false)
                .setEventPool(childBotExecutor(), false)
                .setGatewayPool(childBotExecutor(), false)
                .setHttpClient(childBotOkHttpClient())
                .setActivity(Activity.listening(ANTONIA_STATUS_LIST[ThreadLocalRandom.current().nextInt(ANTONIA_STATUS_LIST.length)]))
                .build();
    }

    @Bean(name = "behradJda")
    public JDA behradJda() throws Exception {
        return JDABuilder.createLight(behradBotToken, Collections.emptyList())
                .setRateLimitPool(childBotExecutor(), false)
                .setCallbackPool(childBotExecutor(), false)
                .setEventPool(childBotExecutor(), false)
                .setGatewayPool(childBotExecutor(), false)
                .setHttpClient(childBotOkHttpClient())
                .setActivity(Activity.playing(BEHRAD_STATUS_LIST[ThreadLocalRandom.current().nextInt(BEHRAD_STATUS_LIST.length)]))
                .build();
    }

    @Bean(name = "keanuJda")
    public JDA keanuJda() throws Exception {
        return JDABuilder.createLight(keanuBotToken, Collections.emptyList())
                .setRateLimitPool(childBotExecutor(), false)
                .setCallbackPool(childBotExecutor(), false)
                .setEventPool(childBotExecutor(), false)
                .setGatewayPool(childBotExecutor(), false)
                .setHttpClient(childBotOkHttpClient())
                .setActivity(Activity.watching(KEANU_STATUS_LIST[ThreadLocalRandom.current().nextInt(KEANU_STATUS_LIST.length)]))
                .build();
    }

    @Bean(name = "philCommandClient")
    public CommandClient philCommandClient(List<Command> commands) {
        return new CommandClientBuilder()
                .setOwnerId(ownerId)
                .setPrefix("!!")
                .useHelpBuilder(false)
                .addCommands(commands.stream().filter(c -> c instanceof PhilMarker).toArray(Command[]::new))
                .setActivity(Activity.playing("with our feelings"))
                .setEmojis("\uD83C\uDF83", "⚠️", "\uD83D\uDC80")
                .build();
    }

    @Bean(name = "philJda")
    public JDA philJda(List<EventListener> eventListeners,
                       @Qualifier("philCommandClient") CommandClient philCommandClient) throws Exception {
        return JDABuilder.create(philBotToken, Arrays.asList(GUILD_MEMBERS, GUILD_BANS, GUILD_MESSAGES, GUILD_VOICE_STATES, GUILD_MESSAGE_REACTIONS, DIRECT_MESSAGES))
                .addEventListeners(eventListeners.stream().filter(e -> e instanceof PhilMarker).toArray(EventListener[]::new))
                .addEventListeners(philCommandClient)
                .setActivity(Activity.playing("with our feelings"))
                .build();
    }

}
