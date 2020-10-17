package com.badfic.philbot.config;

import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
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

    @Bean(name = "keanuJda")
    public JDA keanuJda() throws Exception {
        return JDABuilder.createLight(keanuBotToken, Collections.emptyList())
                .setActivity(Activity.watching(WATCHING_STATUS_LIST[ThreadLocalRandom.current().nextInt(WATCHING_STATUS_LIST.length)]))
                .build();
    }

}
