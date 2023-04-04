package com.badfic.philbot.commands.slash;

import com.badfic.philbot.BaseCommand;
import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.data.DiscordUserRepository;
import com.badfic.philbot.data.SwampyGamesConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import io.honeybadger.reporter.HoneybadgerReporter;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

public abstract class BaseSlashCommand extends SlashCommand implements BaseCommand {

    @Setter(onMethod_ = {@Autowired, @Qualifier("philJda"), @Lazy})
    @Getter
    protected JDA philJda;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected DiscordUserRepository discordUserRepository;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected SwampyGamesConfigRepository swampyGamesConfigRepository;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected ObjectMapper objectMapper;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected BaseConfig baseConfig;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected HoneybadgerReporter honeybadgerReporter;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected RestTemplate restTemplate;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected OkHttpClient okHttpClient;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected ThreadPoolTaskExecutor threadPoolTaskExecutor;

    protected CompletableFuture<InteractionHook> replyToInteractionHook(SlashCommandEvent event, CompletableFuture<InteractionHook> interactionHook,
                                                                        MessageEmbed messageEmbed) {
        return interactionHook.whenComplete((hook, err) -> {
            if (err != null) {
                event.getChannel().sendMessageEmbeds(messageEmbed).queue();
                return;
            }

            hook.editOriginalEmbeds(messageEmbed).queue();
        });
    }

    protected CompletableFuture<InteractionHook> replyToInteractionHook(SlashCommandEvent event, CompletableFuture<InteractionHook> interactionHook,
                                                                        String message) {
        return interactionHook.whenComplete((hook, err) -> {
            if (err != null) {
                event.getChannel().sendMessage(message).queue();
                return;
            }

            hook.editOriginal(message).queue();
        });
    }
}
