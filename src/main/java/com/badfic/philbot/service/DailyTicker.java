package com.badfic.philbot.service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyTicker extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Resource
    private List<DailyTickable> dailyTickables;

    @Scheduled(cron = "${swampy.schedule.daily}", zone = "${swampy.schedule.timezone}")
    public void masterTick() {
        for (DailyTickable tickable : dailyTickables) {
            threadPoolTaskExecutor.submit(() -> {
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
