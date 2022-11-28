package com.badfic.philbot.service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyTicker extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ExecutorService DAILY_TICKER_EXECUTOR = Executors.newSingleThreadExecutor();

    @Autowired
    private List<DailyTickable> dailyTickables;

    @Scheduled(cron = "${swampy.schedule.daily}", zone = "${swampy.schedule.timezone}")
    public void masterTick() {
        for (DailyTickable tickable : dailyTickables) {
            DAILY_TICKER_EXECUTOR.execute(() -> {
                try {
                    tickable.runDailyTask();
                } catch (Exception e) {
                    logger.error("Exception in daily tickable [{}]", tickable.getClass().getName(), e);
                    honeybadgerReporter.reportError(e, null, "Exception in daily tickable: " + tickable.getClass().getName());
                }
            });
        }
    }

}
