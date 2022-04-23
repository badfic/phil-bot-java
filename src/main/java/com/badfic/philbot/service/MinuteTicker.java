package com.badfic.philbot.service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MinuteTicker extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Executor MINUTE_TICKER_EXECUTOR = Executors.newSingleThreadExecutor();

    @Resource
    private List<MinuteTickable> minuteTickables;

    @Scheduled(cron = "${swampy.schedule.minutely}", zone = "${swampy.schedule.timezone}")
    public void masterTick() {
        if (philJda == null || CollectionUtils.isEmpty(philJda.getGuilds())) {
            logger.info("Still setting up philJDA, MinuteTicker shows it as null");
            return; // Context is still setting up and the minute rolled over before phil received the ready message from discord api.
        }

        for (MinuteTickable tickable : minuteTickables) {
            MINUTE_TICKER_EXECUTOR.execute(() -> {
                try {
                    tickable.runMinutelyTask();
                } catch (Exception e) {
                    logger.error("Exception in minute tickable [{}]", tickable.getClass().getName(), e);
                    honeybadgerReporter.reportError(e, null, "Exception in minute tickable: " + tickable.getClass().getName());
                }
            });
        }
    }
}
