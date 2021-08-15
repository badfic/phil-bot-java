package com.badfic.philbot.config;

import static net.dv8tion.jda.api.requests.GatewayIntent.DIRECT_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_BANS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGE_REACTIONS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES;

import com.badfic.philbot.listeners.GenericReadyListener;
import com.badfic.philbot.listeners.phil.PhilMessageListener;
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
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;
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

    @Value("${TIMEOUT_CHANNEL_ID}")
    public String timeoutChannelId;

    @Value("${OWNCAST_INSTANCE}")
    public String owncastInstance;

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setMaxPoolSize(4);
        return threadPoolTaskExecutor;
    }

    @Bean(name = "taskScheduler")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(2);
        threadPoolTaskScheduler.setErrorHandler(t -> {
            logger.error("Error in scheduled task", t);
            honeybadgerReporter().reportError(t, null, "Error in scheduled task");
        });
        return threadPoolTaskScheduler;
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder().build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory(okHttpClient()));
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
                .setRateLimitPool(taskScheduler().getScheduledExecutor(), false)
                .setCallbackPool(threadPoolTaskExecutor().getThreadPoolExecutor(), false)
                .setEventPool(threadPoolTaskExecutor().getThreadPoolExecutor(), false)
                .setGatewayPool(taskScheduler().getScheduledExecutor(), false)
                .setHttpClient(okHttpClient())
                .setActivity(Activity.listening(ANTONIA_STATUS_LIST[ThreadLocalRandom.current().nextInt(ANTONIA_STATUS_LIST.length)]))
                .build();
    }

    @Bean(name = "johnJda")
    public JDA johnJda() throws Exception {
        return JDABuilder.createLight(johnBotToken, Collections.emptyList())
                .setRateLimitPool(taskScheduler().getScheduledExecutor(), false)
                .setCallbackPool(threadPoolTaskExecutor().getThreadPoolExecutor(), false)
                .setEventPool(threadPoolTaskExecutor().getThreadPoolExecutor(), false)
                .setGatewayPool(taskScheduler().getScheduledExecutor(), false)
                .setHttpClient(okHttpClient())
                .setActivity(Activity.watching("Constantine"))
                .build();
    }

    @Bean(name = "behradJda")
    public JDA behradJda() throws Exception {
        return JDABuilder.createLight(behradBotToken, Collections.emptyList())
                .setRateLimitPool(taskScheduler().getScheduledExecutor(), false)
                .setCallbackPool(threadPoolTaskExecutor().getThreadPoolExecutor(), false)
                .setEventPool(threadPoolTaskExecutor().getThreadPoolExecutor(), false)
                .setGatewayPool(taskScheduler().getScheduledExecutor(), false)
                .setHttpClient(okHttpClient())
                .setActivity(Activity.playing(BEHRAD_STATUS_LIST[ThreadLocalRandom.current().nextInt(BEHRAD_STATUS_LIST.length)]))
                .build();
    }

    @Bean(name = "keanuJda")
    public JDA keanuJda() throws Exception {
        return JDABuilder.createLight(keanuBotToken, Collections.emptyList())
                .setRateLimitPool(taskScheduler().getScheduledExecutor(), false)
                .setCallbackPool(threadPoolTaskExecutor().getThreadPoolExecutor(), false)
                .setEventPool(threadPoolTaskExecutor().getThreadPoolExecutor(), false)
                .setGatewayPool(taskScheduler().getScheduledExecutor(), false)
                .setHttpClient(okHttpClient())
                .setActivity(Activity.watching(KEANU_STATUS_LIST[ThreadLocalRandom.current().nextInt(KEANU_STATUS_LIST.length)]))
                .build();
    }

    @Bean(name = "philCommandClient")
    public CommandClient philCommandClient(List<Command> commands, HoneybadgerReporter honeybadgerReporter) {
        return new CommandClientBuilder()
                .setOwnerId(ownerId)
                .setPrefix(Constants.PREFIX)
                .useHelpBuilder(false)
                .addCommands(commands.toArray(Command[]::new))
                .setScheduleExecutor(taskScheduler().getScheduledExecutor())
                .setActivity(Activity.playing("with our feelings"))
                .setEmojis("\uD83E\uDD90", "⚠️", "\uD83D\uDC7F")
                .setListener(new CommandListener() {
                    @Override
                    public void onCommandException(CommandEvent event, Command command, Throwable throwable) {
                        logger.error("Exception in command: " + command.getName(), throwable);
                        honeybadgerReporter.reportError(throwable, event, "Exception in command: " + command.getName());
                    }
                })
                .build();
    }

    @Bean(name = "philJda")
    public JDA philJda(GenericReadyListener genericReadyListener,
                       PhilMessageListener philMessageListener,
                       @Qualifier("philCommandClient") CommandClient philCommandClient) throws Exception {
        return JDABuilder.create(philBotToken, Arrays.asList(GUILD_MEMBERS, GUILD_BANS, GUILD_MESSAGES, GUILD_VOICE_STATES, GUILD_MESSAGE_REACTIONS, DIRECT_MESSAGES))
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                .setRateLimitPool(taskScheduler().getScheduledExecutor(), false)
                .setCallbackPool(threadPoolTaskExecutor().getThreadPoolExecutor(), false)
                .setEventPool(threadPoolTaskExecutor().getThreadPoolExecutor(), false)
                .setGatewayPool(taskScheduler().getScheduledExecutor(), false)
                .setHttpClient(okHttpClient())
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
