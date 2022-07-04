package com.badfic.philbot.config;

import static net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_BANS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGE_REACTIONS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES;

import com.badfic.philbot.listeners.GenericReadyListener;
import com.badfic.philbot.listeners.phil.MemeCommandsService;
import com.badfic.philbot.listeners.phil.PhilMessageListener;
import com.badfic.philbot.listeners.phil.swampy.SwampyCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CommandListener;
import com.tumblr.jumblr.JumblrClient;
import io.honeybadger.reporter.HoneybadgerReporter;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class BaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

    @Value("${HOSTNAME}")
    public String hostname;

    @Value("${DISCORD_CLIENT_ID}")
    public String discordClientId;

    @Value("${DISCORD_CLIENT_SECRET}")
    public String discordClientSecret;

    @Value("${TUMBLR_CONSUMER_KEY}")
    public String tumblrConsumerKey;

    @Value("${TUMBLR_CONSUMER_SECRET}")
    public String tumblrConsumerSecret;

    @Value("${TUMBLR_OAUTH_TOKEN}")
    public String tumblrOauthToken;

    @Value("${TUMBLR_OAUTH_SECRET}")
    public String tumblrOauthSecret;

    @Value("${OWNCAST_INSTANCE}")
    public String owncastInstance;

    @Bean(name = "userTriggeredTasksExecutor")
    public ExecutorService userTriggeredTasksExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(2);
        threadPoolTaskExecutor.setRejectedExecutionHandler((runnable, executor) -> {
            logger.error("Rejected task in threadPoolTaskExecutor. [runnable={}]", runnable);
            honeybadgerReporter().reportError(new RuntimeException("Rejected task in threadPoolTaskExecutor"), runnable,
                    "Rejected task in threadPoolTaskExecutor");
        });
        return threadPoolTaskExecutor;
    }

    @Bean(name = "taskScheduler")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(2);
        threadPoolTaskScheduler.setRejectedExecutionHandler((runnable, executor) -> {
            logger.error("Rejected task in taskScheduler. [runnable={}]", runnable);
            honeybadgerReporter().reportError(new RuntimeException("Rejected task in taskScheduler"), runnable, "Rejected task in taskScheduler");
        });
        threadPoolTaskScheduler.setErrorHandler(t -> {
            logger.error("Error in scheduled task", t);
            honeybadgerReporter().reportError(t, null, "Error in scheduled task");
        });
        return threadPoolTaskScheduler;
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return IOUtil.newHttpClientBuilder().build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
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
                          OkHttpClient okHttpClient) throws Exception {
        return JDABuilder.createLight(antoniaBotToken, Collections.emptyList())
                .setRateLimitPool(taskScheduler.getScheduledExecutor(), false)
                .setCallbackPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setEventPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setGatewayPool(taskScheduler.getScheduledExecutor(), false)
                .setHttpClient(okHttpClient)
                .setActivity(Activity.listening(ANTONIA_STATUS_LIST[ThreadLocalRandom.current().nextInt(ANTONIA_STATUS_LIST.length)]))
                .build();
    }

    @Bean(name = "johnJda")
    public JDA johnJda(ThreadPoolTaskScheduler taskScheduler,
                       ThreadPoolTaskExecutor threadPoolTaskExecutor,
                       OkHttpClient okHttpClient) throws Exception {
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
                         OkHttpClient okHttpClient) throws Exception {
        return JDABuilder.createLight(behradBotToken, Collections.emptyList())
                .setRateLimitPool(taskScheduler.getScheduledExecutor(), false)
                .setCallbackPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setEventPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setGatewayPool(taskScheduler.getScheduledExecutor(), false)
                .setHttpClient(okHttpClient)
                .setActivity(Activity.playing(BEHRAD_STATUS_LIST[ThreadLocalRandom.current().nextInt(BEHRAD_STATUS_LIST.length)]))
                .build();
    }

    @Bean(name = "keanuJda")
    public JDA keanuJda(ThreadPoolTaskScheduler taskScheduler,
                        ThreadPoolTaskExecutor threadPoolTaskExecutor,
                        OkHttpClient okHttpClient) throws Exception {
        return JDABuilder.createLight(keanuBotToken, Collections.emptyList())
                .setRateLimitPool(taskScheduler.getScheduledExecutor(), false)
                .setCallbackPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setEventPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setGatewayPool(taskScheduler.getScheduledExecutor(), false)
                .setHttpClient(okHttpClient)
                .setActivity(Activity.watching(KEANU_STATUS_LIST[ThreadLocalRandom.current().nextInt(KEANU_STATUS_LIST.length)]))
                .build();
    }

    @Bean(name = "philCommandClient")
    public CommandClient philCommandClient(ThreadPoolTaskScheduler taskScheduler,
                                           List<Command> commands,
                                           HoneybadgerReporter honeybadgerReporter,
                                           MemeCommandsService memeCommandsService) {
        Optional<SwampyCommand> optSwampyCommand = commands.stream()
                .filter(c -> c instanceof SwampyCommand)
                .findAny()
                .map(c -> (SwampyCommand) c);

        final MutableObject<CommandClient> thisClient = new MutableObject<>();

        thisClient.setValue(new CommandClientBuilder()
                .setOwnerId(ownerId)
                .setPrefix(Constants.PREFIX)
                .useHelpBuilder(false)
                .addCommands(commands.toArray(Command[]::new))
                .setScheduleExecutor(taskScheduler.getScheduledExecutor())
                .setActivity(Activity.playing("with our feelings"))
                .setEmojis("\uD83E\uDDC1", "⚠️", "\uD83E\uDEA8")
                .setListener(new CommandListener() {
                    @Override
                    public void onCommandException(CommandEvent event, Command command, Throwable throwable) {
                        logger.error("Exception in command: " + command.getName(), throwable);
                        honeybadgerReporter.reportError(throwable, event, "Exception in command: " + command.getName());
                    }

                    @Override
                    public void onNonCommandMessage(MessageReceivedEvent event) {
                        if (optSwampyCommand.isPresent()) {
                            SwampyCommand swampyCommand = optSwampyCommand.get();

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

                                    memeCommandsService.executeCustomCommand(parts[0], event.getTextChannel());
                                }
                            }
                        }
                    }
                })
                .build());

        return thisClient.getValue();
    }

    @Bean(name = "philJda")
    public JDA philJda(ThreadPoolTaskScheduler taskScheduler,
                       ThreadPoolTaskExecutor threadPoolTaskExecutor,
                       OkHttpClient okHttpClient,
                       GenericReadyListener genericReadyListener,
                       PhilMessageListener philMessageListener,
                       @Qualifier("philCommandClient") CommandClient philCommandClient) throws Exception {
        return JDABuilder.create(philBotToken, Arrays.asList(GUILD_MEMBERS, GUILD_BANS, GUILD_MESSAGES, GUILD_VOICE_STATES, GUILD_MESSAGE_REACTIONS, DIRECT_MESSAGES))
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                .setRateLimitPool(taskScheduler.getScheduledExecutor(), false)
                .setCallbackPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setEventPool(threadPoolTaskExecutor.getThreadPoolExecutor(), false)
                .setGatewayPool(taskScheduler.getScheduledExecutor(), false)
                .setHttpClient(okHttpClient)
                .addEventListeners(genericReadyListener, philMessageListener, philCommandClient)
                .setActivity(Activity.playing("with our feelings"))
                .build();
    }

    @Bean
    public HoneybadgerReporter honeybadgerReporter() {
        return new HoneybadgerReporter();
    }

    @Bean
    public JumblrClient jumblrClient() {
        return new JumblrClient(tumblrConsumerKey, tumblrConsumerSecret, tumblrOauthToken, tumblrOauthSecret);
    }

}
