package com.badfic.philbot.commands;

import com.badfic.philbot.BaseCommand;
import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.data.DiscordUserRepository;
import com.badfic.philbot.data.SwampyGamesConfigDal;
import com.badfic.philbot.listeners.DiscordWebhookSendService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.Command;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

public abstract class BaseNormalCommand extends Command implements BaseCommand {

    @Setter(onMethod_ = {@Autowired, @Qualifier("philJda"), @Lazy})
    @Getter
    protected JDA philJda;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected DiscordUserRepository discordUserRepository;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected SwampyGamesConfigDal swampyGamesConfigDal;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected DiscordWebhookSendService discordWebhookSendService;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected ObjectMapper objectMapper;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected JdbcAggregateTemplate jdbcAggregateTemplate;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected BaseConfig baseConfig;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected RestTemplate restTemplate;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected OkHttpClient okHttpClient;

    @Setter(onMethod_ = {@Autowired})
    @Getter
    protected ThreadPoolTaskExecutor threadPoolTaskExecutor;

}
