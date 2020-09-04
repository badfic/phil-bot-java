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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Resource
    private BaseConfig baseConfig;

    @Bean(name = "behradCommandClient")
    public CommandClient behradCommandClient(List<Command> commands) {
        List<Command> behradCommands = commands.stream().filter(c -> c instanceof BehradMarker).collect(Collectors.toList());

        return new CommandClientBuilder()
                .setOwnerId(baseConfig.ownerId)
                .setPrefix("!!")
                .setHelpConsumer((event) -> {
                    if (event.getMember().getRoles().stream().noneMatch(r -> r.getName().equalsIgnoreCase("queens of the castle"))) {
                        return;
                    }

                    StringBuilder builder = new StringBuilder("**" + event.getSelfUser().getName() + "** commands:\n");
                    Command.Category category = null;
                    for (Command command : behradCommands) {
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
                    User owner = event.getJDA().getUserById(baseConfig.ownerId);
                    if (owner != null) {
                        builder.append("\n\nFor additional help, contact **").append(owner.getName()).append("**#").append(owner.getDiscriminator());
                    }
                    event.replyInDm(builder.toString(), s -> {}, f -> event.replyWarning("Help cannot be sent because you are blocking Direct Messages."));
                })
                .addCommands(behradCommands.toArray(new Command[0]))
                .setActivity(Activity.playing(PLAYING_STATUS_LIST[ThreadLocalRandom.current().nextInt(PLAYING_STATUS_LIST.length)]))
                .setEmojis("✔️", "⚠️", "❌")
                .build();
    }

    @Bean(name = "behradJda")
    public JDA behradJda(List<EventListener> eventListeners,
                         @Qualifier("behradCommandClient") CommandClient behradCommandClient) throws Exception {
        return JDABuilder.create(behradBotToken, Collections.singletonList(GUILD_MESSAGES))
                .disableCache(EnumSet.allOf(CacheFlag.class))
                .addEventListeners(eventListeners.stream().filter(e -> e instanceof BehradMarker).toArray(EventListener[]::new))
                .addEventListeners(behradCommandClient)
                .setActivity(Activity.playing(PLAYING_STATUS_LIST[ThreadLocalRandom.current().nextInt(PLAYING_STATUS_LIST.length)]))
                .build();
    }

}
