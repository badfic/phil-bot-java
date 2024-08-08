package com.badfic.philbot;

import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class BotApplication {

    public static void main(final String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error("Uncaught exception in [thread={}]", t.getName(), e));
        SpringApplication.run(BotApplication.class, args);
    }

}
