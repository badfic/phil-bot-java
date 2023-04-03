package com.badfic.philbot.commands;

import com.badfic.philbot.BaseCommand;
import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.data.DiscordUserRepository;
import com.badfic.philbot.data.SwampyGamesConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.Command;
import io.honeybadger.reporter.HoneybadgerReporter;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
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

}
