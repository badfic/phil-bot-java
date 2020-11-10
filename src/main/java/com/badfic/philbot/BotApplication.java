package com.badfic.philbot;

import io.honeybadger.reporter.HoneybadgerUncaughtExceptionHandler;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BotApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        HoneybadgerUncaughtExceptionHandler.registerAsUncaughtExceptionHandler();
        SpringApplication.run(BotApplication.class, args);
    }

}
