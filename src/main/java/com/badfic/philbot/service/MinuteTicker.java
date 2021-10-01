package com.badfic.philbot.service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MinuteTicker extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Resource
    private List<MinuteTickable> minuteTickables;

    @Scheduled(cron = "0 * * * * ?", zone = "America/Los_Angeles")
    public void masterTick() {
        if (philJda == null || CollectionUtils.isEmpty(philJda.getGuilds())) {
            logger.info("Still setting up philJDA, MinuteTicker shows it as null");
            return; // Context is still setting up and the minute rolled over before phil received the ready message from discord api.
        }

        for (MinuteTickable runnable : minuteTickables) {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.error("Exception in minute tickable [{}]", runnable.getClass().getName(), e);
                honeybadgerReporter.reportError(e, null, "Exception in minute tickable: " + runnable.getClass().getName());
            }
        }
    }
}
