package com.badfic.philbot.commands.slash;

import com.badfic.philbot.BaseCommand;
import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.data.DiscordUserRepository;
import com.badfic.philbot.data.SwampyGamesConfigDal;
import com.badfic.philbot.service.RandomNumberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

@Getter
public abstract class BaseSlashCommand implements BaseCommand {

    @Setter(onMethod_ = {@Autowired, @Lazy})
    protected JDA philJda;

    @Setter(onMethod_ = {@Autowired})
    protected DiscordUserRepository discordUserRepository;

    @Setter(onMethod_ = {@Autowired})
    protected SwampyGamesConfigDal swampyGamesConfigDal;

    @Setter(onMethod_ = {@Autowired})
    protected ObjectMapper objectMapper;

    @Setter(onMethod_ = {@Autowired})
    protected JdbcAggregateTemplate jdbcAggregateTemplate;

    @Setter(onMethod_ = {@Autowired})
    protected BaseConfig baseConfig;

    @Setter(onMethod_ = {@Autowired})
    protected RestTemplate restTemplate;

    @Setter(onMethod_ = {@Autowired})
    protected ExecutorService executorService;

    @Setter(onMethod_ = {@Autowired})
    protected RandomNumberService randomNumberService;

    @Setter(onMethod_ = {@Autowired})
    private ThreadPoolTaskScheduler taskScheduler;

    protected String name;
    protected String help;
    protected String requiredRole;
    protected boolean nsfwOnly;
    protected boolean ownerCommand;
    protected List<OptionData> options;

    public void onAutoComplete(final CommandAutoCompleteInteractionEvent event) {
        // do nothing
    }

    public abstract void execute(final SlashCommandInteractionEvent event);

    protected CompletableFuture<InteractionHook> replyToInteractionHook(final SlashCommandInteractionEvent event,
            final CompletableFuture<InteractionHook> interactionHook, final MessageEmbed messageEmbed) {
        return interactionHook.whenComplete((hook, err) -> {
            if (err != null) {
                event.getChannel().sendMessageEmbeds(messageEmbed).queue();
                return;
            }

            hook.editOriginalEmbeds(messageEmbed).queue();
        });
    }

    protected CompletableFuture<InteractionHook> replyToInteractionHook(final SlashCommandInteractionEvent event,
            final CompletableFuture<InteractionHook> interactionHook, final String message) {
        return interactionHook.whenComplete((hook, err) -> {
            if (err != null) {
                event.getChannel().sendMessage(message).queue();
                return;
            }

            hook.editOriginal(message).queue();
        });
    }

    protected CompletableFuture<InteractionHook> replyToInteractionHook(final SlashCommandInteractionEvent event,
            final CompletableFuture<InteractionHook> interactionHook, final FileUpload file) {
        return interactionHook.whenComplete((hook, err) -> {
            if (err != null) {
                event.getChannel().sendMessage(" ").addFiles(file).queue();
                return;
            }

            hook.editOriginalAttachments(file).queue();
        });
    }

}
