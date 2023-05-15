package com.badfic.philbot.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class MinuteTicker extends BaseService {

    private final List<MinuteTickable> minuteTickables;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Scheduled(cron = "${swampy.schedule.minutely}", zone = "${swampy.schedule.timezone}")
    public void tick() {
        if (philJda == null || CollectionUtils.isEmpty(philJda.getGuilds())) {
            log.info("Still setting up philJDA, MinuteTicker shows it as null");
            return; // Context is still setting up and the minute rolled over before phil received the ready message from discord api.
        }

        for (MinuteTickable tickable : minuteTickables) {
            threadPoolTaskExecutor.execute(() -> {
                try {
                    tickable.runMinutelyTask();
                } catch (Exception e) {
                    log.error("Exception in minute tickable [{}]", tickable.getClass().getName(), e);
                }
            });
        }
    }
}
