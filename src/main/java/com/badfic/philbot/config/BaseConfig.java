package com.badfic.philbot.config;

import static net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_EMOJIS_AND_STICKERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGE_REACTIONS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MODERATION;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES;
import static net.dv8tion.jda.api.requests.GatewayIntent.MESSAGE_CONTENT;

import com.badfic.philbot.listeners.Phil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
public class BaseConfig {

    @Value("${PHIL_BOT_TOKEN}")
    public String philBotToken;

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
    public ExecutorService executorService() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public TaskExecutor applicationTaskExecutor(final ExecutorService executorService) {
        return new TaskExecutorAdapter(executorService);
    }

    @Bean(name = "taskScheduler")
    public ThreadPoolTaskScheduler taskScheduler() {
        final var threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setThreadFactory(Thread.ofVirtual().factory());
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
    public OkHttpClient okHttpClient(final ExecutorService executorService) {
        final var dispatcher = new Dispatcher(executorService);
        dispatcher.setMaxRequestsPerHost(25);
        final var connectionPool = new ConnectionPool(4, 10, TimeUnit.SECONDS);

        //noinspection KotlinInternalInJava
        return new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .dispatcher(dispatcher)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        final var objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }

    @Bean
    public RestTemplate restTemplate(final OkHttpClient okHttpClient) {
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory(okHttpClient));
    }

    @Bean
    public AudioPlayerManager audioPlayerManager() {
        final var manager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(manager);
        return manager;
    }

    @Bean
    public AudioPlayer audioPlayer(final AudioPlayerManager audioPlayerManager) {
        return audioPlayerManager.createPlayer();
    }

    @Bean(name = "philJda")
    public JDA philJda(final ThreadPoolTaskScheduler taskScheduler,
                       final ExecutorService executorService,
                       final OkHttpClient okHttpClient,
                       final Phil.MessageListener messageListener) throws Exception {
        final var intents = Arrays.asList(GUILD_MEMBERS, GUILD_MODERATION, GUILD_MESSAGES, MESSAGE_CONTENT, GUILD_VOICE_STATES,
                GUILD_MESSAGE_REACTIONS, GUILD_EMOJIS_AND_STICKERS, DIRECT_MESSAGES);

        return JDABuilder.create(philBotToken, intents)
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOJI, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.FORUM_TAGS,
                        CacheFlag.ROLE_TAGS, CacheFlag.SCHEDULED_EVENTS, CacheFlag.STICKER, CacheFlag.MEMBER_OVERRIDES)
                .setRateLimitScheduler(taskScheduler.getScheduledExecutor(), false)
                .setRateLimitElastic(executorService, false)
                .setCallbackPool(executorService, false)
                .setEventPool(executorService, false)
                .setGatewayPool(taskScheduler.getScheduledExecutor(), false)
                .setHttpClient(okHttpClient)
                .addEventListeners(messageListener)
                .setAudioPool(taskScheduler.getScheduledExecutor(), false)
                .setActivity(Activity.playing("with our feelings"))
                .build();
    }

}
