package com.badfic.philbot.service;

import com.badfic.philbot.config.BaseConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.honeybadger.reporter.HoneybadgerReporter;
import java.util.concurrent.ExecutorService;
import javax.annotation.Resource;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;

public abstract class BaseService {

    @Resource
    protected BaseConfig baseConfig;

    @Resource
    protected HoneybadgerReporter honeybadgerReporter;

    @Resource(name = "philJda")
    @Lazy
    protected JDA philJda;

    @Resource
    protected RestTemplate restTemplate;

    @Resource
    protected ExecutorService userTriggeredTasksExecutor;

    @Resource
    protected ObjectMapper objectMapper;

}
