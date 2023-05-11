package com.badfic.philbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
public class BaseConfig {

    @Value("${PHIL_BOT_TOKEN}")
    public String philBotToken;

    @Value("${git.commit.id}")
    public String commitSha;

    @Value("${git.commit.message.short}")
    public String commitMessage;

    @Value("${OWNER_ID}")
    public String ownerId;

    @Value("${GUILD_ID}")
    public String guildId;

    @Value("${HOSTNAME}")
    public String hostname;

    @Value("${DISCORD_CLIENT_ID}")
    public String discordClientId;

    @Value("${DISCORD_CLIENT_SECRET}")
    public String discordClientSecret;

    @Value("${TUMBLR_CONSUMER_KEY}")
    public String tumblrConsumerKey;

    @Value("${OWNCAST_INSTANCE}")
    public String owncastInstance;

    @Value("${AIRTABLE_API_TOKEN}")
    public String airtableApiToken;

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
        threadPoolTaskExecutor.setRejectedExecutionHandler((runnable, executor) -> {
            log.error("Rejected task in threadPoolTaskExecutor. [runnable={}]", runnable);
        });
        return threadPoolTaskExecutor;
    }

    @Bean(name = "taskScheduler")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(2);
        threadPoolTaskScheduler.setRejectedExecutionHandler((runnable, executor) -> {
            log.error("Rejected task in taskScheduler. [runnable={}]", runnable);
        });
        threadPoolTaskScheduler.setErrorHandler(t -> {
            log.error("Error in scheduled task", t);
        });
        return threadPoolTaskScheduler;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propsConfig = new PropertySourcesPlaceholderConfigurer();
        propsConfig.setLocation(new ClassPathResource("git.properties"));
        propsConfig.setIgnoreResourceNotFound(true);
        propsConfig.setIgnoreUnresolvablePlaceholders(true);
        return propsConfig;
    }

    @Bean
    public GatewayDiscordClient gatewayDiscordClient() {
        return DiscordClient.create(philBotToken).login().block();
    }

}
