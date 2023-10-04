package com.badfic.philbot.config;

import static net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_EMOJIS_AND_STICKERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGE_REACTIONS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MODERATION;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES;
import static net.dv8tion.jda.api.requests.GatewayIntent.MESSAGE_CONTENT;

import com.badfic.philbot.listeners.phil.PhilMessageListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
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

    @Value("${AIRTABLE_API_TOKEN}")
    public String airtableApiToken;

    @Value("${AO3_SUMMARY_API_KEY}")
    public String ao3SummaryApiKey;

    @Bean
    public ThreadPoolTaskExecutor applicationTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(4);
        threadPoolTaskExecutor.setRejectedExecutionHandler((runnable, executor) -> {
            log.error("Rejected task in threadPoolTaskExecutor. [runnable={}]", runnable);
        });
        return threadPoolTaskExecutor;
    }

    @Bean(name = "taskScheduler")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setRejectedExecutionHandler((runnable, executor) -> {
            log.error("Rejected task in taskScheduler. [runnable={}]", runnable);
        });
        threadPoolTaskScheduler.setErrorHandler(t -> {
            log.error("Error in scheduled task", t);
        });
        threadPoolTaskScheduler.initialize();
        threadPoolTaskScheduler.setPoolSize(4);
        return threadPoolTaskScheduler;
    }

    @Bean
    public OkHttpClient okHttpClient(ThreadPoolTaskExecutor applicationTaskExecutor) {
        Dispatcher dispatcher = new Dispatcher(applicationTaskExecutor.getThreadPoolExecutor());
        dispatcher.setMaxRequestsPerHost(25);
        ConnectionPool connectionPool = new ConnectionPool(4, 10, TimeUnit.SECONDS);
        return new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .dispatcher(dispatcher)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }

    @Bean
    public RestTemplate restTemplate(OkHttpClient okHttpClient) {
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory(okHttpClient));
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propsConfig = new PropertySourcesPlaceholderConfigurer();
        propsConfig.setLocation(new ClassPathResource("git.properties"));
        propsConfig.setIgnoreResourceNotFound(true);
        propsConfig.setIgnoreUnresolvablePlaceholders(true);
        return propsConfig;
    }

    @Bean(name = "philJda")
    public JDA philJda(ThreadPoolTaskScheduler taskScheduler,
                       ThreadPoolTaskExecutor applicationTaskExecutor,
                       OkHttpClient okHttpClient,
                       PhilMessageListener philMessageListener) throws Exception {
        List<GatewayIntent> intents = Arrays.asList(GUILD_MEMBERS, GUILD_MODERATION, GUILD_MESSAGES, MESSAGE_CONTENT, GUILD_VOICE_STATES,
                GUILD_MESSAGE_REACTIONS, GUILD_EMOJIS_AND_STICKERS, DIRECT_MESSAGES);

        return JDABuilder.create(philBotToken, intents)
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOJI, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.FORUM_TAGS,
                        CacheFlag.ROLE_TAGS, CacheFlag.SCHEDULED_EVENTS, CacheFlag.STICKER, CacheFlag.MEMBER_OVERRIDES)
                .setRateLimitPool(taskScheduler.getScheduledExecutor(), false)
                .setCallbackPool(applicationTaskExecutor.getThreadPoolExecutor(), false)
                .setEventPool(applicationTaskExecutor.getThreadPoolExecutor(), false)
                .setGatewayPool(taskScheduler.getScheduledExecutor(), false)
                .setHttpClient(okHttpClient)
                .addEventListeners(philMessageListener)
                .setActivity(Activity.playing("with our feelings"))
                .build();
    }

}
