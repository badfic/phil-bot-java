package com.badfic.philbot.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class DailyTicker extends BaseService {
    private final List<DailyTickable> dailyTickables;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Scheduled(cron = "${swampy.schedule.daily}", zone = "${swampy.schedule.timezone}")
    public void tick() {
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
