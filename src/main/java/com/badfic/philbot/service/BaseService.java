package com.badfic.philbot.service;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.data.SwampyGamesConfigDal;
import com.badfic.philbot.listeners.DiscordWebhookSendService;
import java.util.concurrent.ExecutorService;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

public abstract class BaseService {

    @Setter(onMethod_ = {@Autowired})
    protected BaseConfig baseConfig;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    protected JDA philJda;

    @Setter(onMethod_ = {@Autowired})
    protected RestTemplate restTemplate;

    @Setter(onMethod_ = {@Autowired})
    protected ExecutorService executorService;

    @Setter(onMethod_ = {@Autowired})
    protected JsonMapper jsonMapper;

    @Setter(onMethod_ = {@Autowired})
    protected SwampyGamesConfigDal swampyGamesConfigDal;

    @Setter(onMethod_ = {@Autowired})
    protected DiscordWebhookSendService discordWebhookSendService;

    @Setter(onMethod_ = {@Autowired})
    protected RandomNumberService randomNumberService;

}
