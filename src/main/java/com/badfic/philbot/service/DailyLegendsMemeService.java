package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.BaseDailyMemeEntity;
import com.badfic.philbot.data.DailyLegendsMemeEntity;
import com.badfic.philbot.data.DailyLegendsMemeRepository;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DailyLegendsMemeService extends BaseDailyMemeService implements DailyTickable {
    public DailyLegendsMemeService(final DailyLegendsMemeRepository dailyLegendsMemeRepository) {
        super(dailyLegendsMemeRepository, "updateDailyLegendsMemes", "out of context legends of tomorrow meme", Constants.MEMES_CHANNEL);
    }

    @Override
    public BaseDailyMemeEntity create(final long messageId, final String message, final String imageUrl, final LocalDateTime timeCreated, final LocalDateTime timeEdited) {
        return new DailyLegendsMemeEntity(messageId, message, imageUrl, timeCreated, timeEdited);
    }

    @Override
    public void runDailyTask() {
        scrapeAllMemes();
    }
}
