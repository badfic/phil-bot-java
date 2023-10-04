package com.badfic.philbot.commands;

import com.badfic.philbot.BaseCommand;
import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.data.DiscordUserRepository;
import com.badfic.philbot.data.SwampyGamesConfigDal;
import com.badfic.philbot.listeners.DiscordWebhookSendService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ExecutorService;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
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
    protected ExecutorService executorService;

    @Setter(onMethod_ = {@Autowired})
    private ThreadPoolTaskScheduler taskScheduler;

    protected String name;
    protected String requiredRole;
    protected String help = StringUtils.EMPTY;
    protected String[] aliases = ArrayUtils.EMPTY_STRING_ARRAY;
    protected boolean guildOnly = true;
    protected boolean ownerCommand = false;
    protected boolean nsfwOnly = false;

    public abstract void execute(CommandEvent event);

}
