package com.badfic.philbot.service;

import com.badfic.philbot.config.BaseConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

public abstract class BaseService {

    @Setter(onMethod_ = {@Autowired})
    protected BaseConfig baseConfig;

    @Setter(onMethod_ = {@Autowired, @Qualifier("philJda"), @Lazy})
    protected JDA philJda;

    @Setter(onMethod_ = {@Autowired})
    protected RestTemplate restTemplate;

    @Setter(onMethod_ = {@Autowired})
    protected ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Setter(onMethod_ = {@Autowired})
    protected ObjectMapper objectMapper;

}
