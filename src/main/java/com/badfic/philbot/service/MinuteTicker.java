package com.badfic.philbot.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MinuteTicker extends BaseService {
    private static final ExecutorService MINUTE_TICKER_EXECUTOR = Executors.newSingleThreadExecutor();

    private final List<MinuteTickable> minuteTickables;

    public MinuteTicker(List<MinuteTickable> minuteTickables) {
        this.minuteTickables = minuteTickables;
    }

    @Scheduled(cron = "${swampy.schedule.minutely}", zone = "${swampy.schedule.timezone}")
    public void masterTick() {
        if (philJda == null || CollectionUtils.isEmpty(philJda.getGuilds())) {
            log.info("Still setting up philJDA, MinuteTicker shows it as null");
            return; // Context is still setting up and the minute rolled over before phil received the ready message from discord api.
        }

        for (MinuteTickable tickable : minuteTickables) {
            MINUTE_TICKER_EXECUTOR.execute(() -> {
                try {
                    tickable.runMinutelyTask();
                } catch (Exception e) {
                    log.error("Exception in minute tickable [{}]", tickable.getClass().getName(), e);
                    honeybadgerReporter.reportError(e, null, "Exception in minute tickable: " + tickable.getClass().getName());
                }
            });
        }
    }
}
