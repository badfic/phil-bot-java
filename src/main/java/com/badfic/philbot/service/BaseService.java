package com.badfic.philbot.service;

import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.SwampyGamesConfigDal;
import com.badfic.philbot.listeners.DiscordWebhookSendService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ExecutorService;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;

public abstract class BaseService {

    @Setter(onMethod_ = {@Autowired, @Lazy})
    protected JDA philJda;

    @Setter(onMethod_ = {@Autowired})
    protected RestTemplate restTemplate;

    @Setter(onMethod_ = {@Autowired})
    protected ExecutorService executorService;

    @Setter(onMethod_ = {@Autowired})
    protected ObjectMapper objectMapper;

    @Setter(onMethod_ = {@Autowired})
    protected DiscordWebhookSendService discordWebhookSendService;

    @Setter(onMethod_ = {@Autowired})
    protected RandomNumberService randomNumberService;

    @Setter(onMethod_ = {@Autowired})
    private SwampyGamesConfigDal swampyGamesConfigDal;

    protected SwampyGamesConfig getSwampyGamesConfig()  {
        return swampyGamesConfigDal.get();
    }

}
