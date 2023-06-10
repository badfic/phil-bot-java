package com.badfic.philbot.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class DailyTicker extends BaseService {
    private final List<DailyTickable> dailyTickables;

    @Scheduled(cron = "${swampy.schedule.daily}", zone = "${swampy.schedule.timezone}")
    public void tick() {
        try {
            philJda.awaitReady();
        } catch (Exception e) {
            log.error("Failed to run daily tasks, Discord API did not initialize", e);
            return;
        }

        for (DailyTickable tickable : dailyTickables) {
            threadPoolTaskExecutor.execute(() -> {
                try {
                    tickable.runDailyTask();
                } catch (Exception e) {
                    log.error("Exception in daily tickable [{}]", tickable.getClass().getName(), e);
                }
            });
        }
    }

}
