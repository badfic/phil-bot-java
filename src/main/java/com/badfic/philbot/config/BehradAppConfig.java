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

    @Bean(name = "behradJda")
    public JDA behradJda() throws Exception {
        return JDABuilder.createLight(behradBotToken, Collections.emptyList())
                .setActivity(Activity.playing(PLAYING_STATUS_LIST[ThreadLocalRandom.current().nextInt(PLAYING_STATUS_LIST.length)]))
                .build();
    }

}
