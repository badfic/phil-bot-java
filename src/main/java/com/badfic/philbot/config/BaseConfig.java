package com.badfic.philbot.config;

import static net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_EMOJIS_AND_STICKERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGE_REACTIONS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MODERATION;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES;
import static net.dv8tion.jda.api.requests.GatewayIntent.MESSAGE_CONTENT;

import com.badfic.philbot.commands.swampy.SwampyCommand;
import com.badfic.philbot.listeners.phil.PhilMessageListener;
import com.badfic.philbot.service.MemeCommandsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CommandListener;
import com.jagrosh.jdautilities.command.SlashCommand;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Value("${JOHN_BOT_TOKEN}")
    public String johnBotToken;

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
    public OkHttpClient okHttpClient(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        Dispatcher dispatcher = new Dispatcher(threadPoolTaskExecutor.getThreadPoolExecutor());
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

    @Bean(name = "antoniaJda")
    public JDA antoniaJda(ThreadPoolTaskScheduler taskScheduler,
                          ThreadPoolTaskExecutor threadPoolTaskExecutor,
                          OkHttpClient okHttpClient) {
        return JDABuilder.createLight(antoniaBotToken, Collections.emptyList())
                .setRateLimitPool(taskScheduler.getScheduledExecutor(), false)
                .setCallbackPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setEventPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setGatewayPool(taskScheduler.getScheduledExecutor(), false)
                .setHttpClient(okHttpClient)
                .setActivity(Activity.listening(Constants.pickRandom(ANTONIA_STATUS_LIST)))
                .build();
    }

    @Bean(name = "johnJda")
    public JDA johnJda(ThreadPoolTaskScheduler taskScheduler,
                       ThreadPoolTaskExecutor threadPoolTaskExecutor,
                       OkHttpClient okHttpClient) {
        return JDABuilder.createLight(johnBotToken, Collections.emptyList())
                .setRateLimitPool(taskScheduler.getScheduledExecutor(), false)
                .setCallbackPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setEventPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setGatewayPool(taskScheduler.getScheduledExecutor(), false)
                .setHttpClient(okHttpClient)
                .setActivity(Activity.watching("Constantine"))
                .build();
    }

    @Bean(name = "behradJda")
    public JDA behradJda(ThreadPoolTaskScheduler taskScheduler,
                         ThreadPoolTaskExecutor threadPoolTaskExecutor,
                         OkHttpClient okHttpClient) {
        return JDABuilder.createLight(behradBotToken, Collections.emptyList())
                .setRateLimitPool(taskScheduler.getScheduledExecutor(), false)
                .setCallbackPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setEventPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setGatewayPool(taskScheduler.getScheduledExecutor(), false)
                .setHttpClient(okHttpClient)
                .setActivity(Activity.playing(Constants.pickRandom(BEHRAD_STATUS_LIST)))
                .build();
    }

    @Bean(name = "keanuJda")
    public JDA keanuJda(ThreadPoolTaskScheduler taskScheduler,
                        ThreadPoolTaskExecutor threadPoolTaskExecutor,
                        OkHttpClient okHttpClient) {
        return JDABuilder.createLight(keanuBotToken, Collections.emptyList())
                .setRateLimitPool(taskScheduler.getScheduledExecutor(), false)
                .setCallbackPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setEventPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setGatewayPool(taskScheduler.getScheduledExecutor(), false)
                .setHttpClient(okHttpClient)
                .setActivity(Activity.watching(Constants.pickRandom(KEANU_STATUS_LIST)))
                .build();
    }

    @Bean(name = "philCommandClient")
    public CommandClient philCommandClient(ThreadPoolTaskScheduler taskScheduler,
                                           List<Command> commands,
                                           SwampyCommand swampyCommand,
                                           MemeCommandsService memeCommandsService) {
        CommandClientBuilder builder = new CommandClientBuilder();

        builder.setOwnerId(ownerId)
                .setPrefix(Constants.PREFIX)
                .forceGuildOnly(guildId)
                .useHelpBuilder(false)
                .setScheduleExecutor(taskScheduler.getScheduledExecutor())
                .setActivity(Activity.playing("with our feelings"))
                .setEmojis("✅", "⚠️", "❌");

        for (Command command : commands) {
            if (command instanceof SlashCommand) {
                builder.addSlashCommands((SlashCommand) command);
            } else {
                builder.addCommand(command);
            }
        }

        final MutableObject<CommandClient> thisClient = new MutableObject<>();
        builder.setListener(new CommandListener() {
            @Override
            public void onCommandException(CommandEvent event, Command command, Throwable throwable) {
                log.error("Exception in [command={}]", command.getName(), throwable);
            }

            @Override
            public void onNonCommandMessage(MessageReceivedEvent event) {
                String contentRaw = event.getMessage().getContentRaw();

                if (StringUtils.startsWith(contentRaw, Constants.PREFIX)) {
                    String message = StringUtils.substring(contentRaw, 2);
                    message = StringUtils.trim(message);

                    String[] parts;
                    if (StringUtils.isNotBlank(message)) {
                        if ((parts = message.split("\\s+")).length >= 2) {
                            if (Stream.of("help", "rank", "up", "down", "slots")
                                    .anyMatch(arg -> StringUtils.equalsIgnoreCase(arg, parts[1]))) {
                                swampyCommand.execute(new CommandEvent(event, Constants.PREFIX, parts[1], thisClient.getValue()));
                                return;
                            }
                        }

                        memeCommandsService.executeCustomCommand(parts[0], event.getChannel().asGuildMessageChannel());
                    }
                }
            }
        });

        thisClient.setValue(builder.build());
        return thisClient.getValue();
    }

    @Bean(name = "philJda")
    public JDA philJda(ThreadPoolTaskScheduler taskScheduler,
                       ThreadPoolTaskExecutor threadPoolTaskExecutor,
                       OkHttpClient okHttpClient,
                       PhilMessageListener philMessageListener,
                       @Qualifier("philCommandClient") CommandClient philCommandClient) throws Exception {
        List<GatewayIntent> intents = Arrays.asList(GUILD_MEMBERS, GUILD_MODERATION, GUILD_MESSAGES, MESSAGE_CONTENT, GUILD_VOICE_STATES,
                GUILD_MESSAGE_REACTIONS, GUILD_EMOJIS_AND_STICKERS, DIRECT_MESSAGES);

        return JDABuilder.create(philBotToken, intents)
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOJI, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.FORUM_TAGS,
                        CacheFlag.ROLE_TAGS, CacheFlag.SCHEDULED_EVENTS, CacheFlag.STICKER, CacheFlag.MEMBER_OVERRIDES)
                .setRateLimitPool(taskScheduler.getScheduledExecutor(), false)
                .setCallbackPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setEventPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setGatewayPool(taskScheduler.getScheduledExecutor(), false)
                .setHttpClient(okHttpClient)
                .addEventListeners(philMessageListener, philCommandClient)
                .setActivity(Activity.playing("with our feelings"))
                .build();
    }

}
