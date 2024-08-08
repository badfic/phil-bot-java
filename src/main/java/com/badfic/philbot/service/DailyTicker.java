package com.badfic.philbot.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
class DailyTicker extends BaseService {
    private final List<DailyTickable> dailyTickables;

    @Scheduled(cron = "${swampy.schedule.daily}", zone = "${swampy.schedule.timezone}")
    void tick() {
        try {
            philJda.awaitReady();
        } catch (final Exception e) {
            log.error("Failed to run daily tasks, Discord API did not initialize", e);
            return;
        }

        executorService.submit(() -> {
            for (final var tickable : dailyTickables) {
                try {
                    tickable.runDailyTask();
                } catch (final Exception e) {
                    log.error("Exception in daily tickable [{}]", tickable.getClass().getName(), e);
                }
            }
            System.gc(); // Some of the daily tasks create a lot of objects, might as well force a GC to clear up what we can
        });
    }
}
