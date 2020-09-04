package com.badfic.philbot.config;

import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_BANS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
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
public class PhilbotAppConfig {

    @Value("${PHIL_BOT_TOKEN}")
    public String philBotToken;

    @Resource
    private BaseConfig baseConfig;

    @Bean(name = "philCommandClient")
    public CommandClient philCommandClient(List<Command> commands) {
        List<Command> philCommands = commands.stream().filter(c -> c instanceof PhilMarker).collect(Collectors.toList());

        return new CommandClientBuilder()
                .setOwnerId(baseConfig.ownerId)
                .setPrefix("!!")
                .setHelpConsumer((event) -> {
                    if (event.getMember().getRoles().stream().noneMatch(r -> r.getName().equalsIgnoreCase("queens of the castle"))) {
                        return;
                    }

                    StringBuilder builder = new StringBuilder("**" + event.getSelfUser().getName() + "** commands:\n");
                    Command.Category category = null;
                    for (Command command : philCommands) {
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
                .addCommands(philCommands.toArray(new Command[0]))
                .setActivity(Activity.playing("with our feelings"))
                .setEmojis("✔️", "⚠️", "❌")
                .build();
    }

    @Bean(name = "philJda")
    public JDA philJda(List<EventListener> eventListeners,
                       @Qualifier("philCommandClient") CommandClient philCommandClient) throws Exception {
        return JDABuilder.create(philBotToken, Arrays.asList(GUILD_MEMBERS, GUILD_BANS, GUILD_MESSAGES, GUILD_VOICE_STATES))
                .disableCache(EnumSet.allOf(CacheFlag.class))
                .enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(eventListeners.stream().filter(e -> e instanceof PhilMarker).toArray(EventListener[]::new))
                .addEventListeners(philCommandClient)
                .setActivity(Activity.playing("with our feelings"))
                .build();
    }

}
