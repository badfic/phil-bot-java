package com.badfic.philbot.commands;

import com.badfic.philbot.BaseCommand;
import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.data.DiscordUserRepository;
import com.badfic.philbot.data.SwampyGamesConfigDal;
import com.badfic.philbot.listeners.DiscordWebhookSendService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

@Getter
public abstract class BaseNormalCommand implements BaseCommand {

    @Setter(onMethod_ = {@Autowired, @Lazy})
    protected JDA philJda;

    @Setter(onMethod_ = {@Autowired})
    protected DiscordUserRepository discordUserRepository;

    @Setter(onMethod_ = {@Autowired})
    protected SwampyGamesConfigDal swampyGamesConfigDal;

    @Setter(onMethod_ = {@Autowired})
    protected DiscordWebhookSendService discordWebhookSendService;

    @Setter(onMethod_ = {@Autowired})
    protected ObjectMapper objectMapper;

    @Setter(onMethod_ = {@Autowired})
    protected JdbcAggregateTemplate jdbcAggregateTemplate;

    @Setter(onMethod_ = {@Autowired})
    protected BaseConfig baseConfig;

    @Setter(onMethod_ = {@Autowired})
    protected RestTemplate restTemplate;

    @Setter(onMethod_ = {@Autowired})
    protected OkHttpClient okHttpClient;

    @Setter(onMethod_ = {@Autowired})
    protected ThreadPoolTaskExecutor threadPoolTaskExecutor;

    protected String name;
    protected String requiredRole;
    protected String help;
    protected String[] aliases;
    protected boolean guildOnly = true;
    protected boolean ownerCommand = false;
    protected boolean nsfwOnly = false;

    public abstract void execute(CommandEvent event);

}
