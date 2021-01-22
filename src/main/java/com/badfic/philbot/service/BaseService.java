package com.badfic.philbot.service;

import io.honeybadger.reporter.HoneybadgerReporter;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

public abstract class BaseService {

    @Resource
    protected HoneybadgerReporter honeybadgerReporter;

    @Resource(name = "philJda")
    @Lazy
    protected JDA philJda;

    @Resource
    protected RestTemplate restTemplate;

    @Resource
    protected ThreadPoolTaskExecutor threadPoolTaskExecutor;

}
