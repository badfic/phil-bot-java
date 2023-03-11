package com.badfic.philbot.service;

import com.badfic.philbot.config.BaseConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.honeybadger.reporter.HoneybadgerReporter;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

public abstract class BaseService {

    @Autowired
    protected BaseConfig baseConfig;

    @Autowired
    protected HoneybadgerReporter honeybadgerReporter;

    @Autowired
    @Qualifier("philJda")
    @Lazy
    protected JDA philJda;

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    protected ObjectMapper objectMapper;

}
