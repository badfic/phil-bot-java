package com.badfic.philbot.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DailyTicker extends BaseService {
    private final List<DailyTickable> dailyTickables;

    public DailyTicker(List<DailyTickable> dailyTickables) {
        this.dailyTickables = dailyTickables;
    }

    @Scheduled(cron = "${swampy.schedule.daily}", zone = "${swampy.schedule.timezone}")
    public void tick() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        for (DailyTickable tickable : dailyTickables) {
            executor.execute(() -> {
                try {
                    tickable.runDailyTask();
                } catch (Exception e) {
                    log.error("Exception in daily tickable [{}]", tickable.getClass().getName(), e);
                }
            });
        }
        executor.shutdown();
    }

}
