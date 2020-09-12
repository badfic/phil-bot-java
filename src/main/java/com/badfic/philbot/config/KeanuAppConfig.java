package com.badfic.philbot.config;

import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Resource
    private BaseConfig baseConfig;

    @Bean(name = "keanuCommandClient")
    public CommandClient keanuCommandClient(List<Command> commands) {
        List<Command> keanuCommands = commands.stream().filter(c -> c instanceof KeanuMarker).collect(Collectors.toList());

        return new CommandClientBuilder()
                .setOwnerId(baseConfig.ownerId)
                .setPrefix("!!")
                .setHelpConsumer((event) -> {
                    if (event.getMember().getRoles().stream().noneMatch(r -> r.getName().equalsIgnoreCase("queens of the castle"))) {
                        return;
                    }

                    StringBuilder builder = new StringBuilder("**" + event.getSelfUser().getName() + "** commands:\n");
                    Command.Category category = null;
                    for (Command command : keanuCommands) {
                        if (!command.isHidden() && (!command.isOwnerCommand() || event.isOwner())) {
                            if (!Objects.equals(category, command.getCategory())) {
                                category = command.getCategory();
                                builder.append("\n\n  __").append(category == null ? "No Category" : category.getName()).append("__:\n");
                            }
                            builder.append("\n`").append("!!").append(command.getName())
                                    .append(command.getArguments() == null ? "`" : " " + command.getArguments() + "`")
                                    .append(" - ").append(command.getHelp());
                        }
                    }
                    event.getJDA().retrieveUserById(baseConfig.ownerId).submit().whenComplete((owner, err) -> {
                        if (owner != null) {
                            builder.append("\n\nFor additional help, contact **").append(owner.getName()).append("**#").append(owner.getDiscriminator());
                        }
                        event.replyInDm(builder.toString(), s -> {}, f -> event.replyWarning("Help cannot be sent because you are blocking Direct Messages."));
                    });
                })
                .addCommands(keanuCommands.toArray(new Command[0]))
                .setActivity(Activity.watching(WATCHING_STATUS_LIST[ThreadLocalRandom.current().nextInt(WATCHING_STATUS_LIST.length)]))
                .setEmojis("✅", "⚠️", "❌")
                .build();
    }

    @Bean(name = "keanuJda")
    public JDA keanuJda(List<EventListener> eventListeners,
                        @Qualifier("keanuCommandClient") CommandClient keanuCommandClient) throws Exception {
        return JDABuilder.create(keanuBotToken, Collections.singletonList(GUILD_MESSAGES))
                .disableCache(EnumSet.allOf(CacheFlag.class))
                .addEventListeners(eventListeners.stream().filter(e -> e instanceof KeanuMarker).toArray(EventListener[]::new))
                .addEventListeners(keanuCommandClient)
                .setActivity(Activity.watching(WATCHING_STATUS_LIST[ThreadLocalRandom.current().nextInt(WATCHING_STATUS_LIST.length)]))
                .build();
    }

}
