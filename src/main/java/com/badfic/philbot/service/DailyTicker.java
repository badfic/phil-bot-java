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

    @Scheduled(cron = "0 0 12 * * ?", zone = "GMT")
    public void masterTick() {
        for (DailyTickable runnable : dailyTickables) {
            threadPoolTaskExecutor.submit(() -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    logger.error("Exception in daily tickable [{}]", runnable.getClass().getName(), e);
                    honeybadgerReporter.reportError(e, null, "Exception in daily tickable: " + runnable.getClass().getName());
                }
            });
        }
    }

}
