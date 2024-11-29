package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.BaseDailyMemeEntity;
import com.badfic.philbot.data.DailyRiverdaleMemeEntity;
import com.badfic.philbot.data.DailyRiverdaleMemeRepository;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DailyRiverdaleMemeService extends BaseDailyMemeService {
    public DailyRiverdaleMemeService(final DailyRiverdaleMemeRepository dailyRiverdaleMemeRepository) {
        super(dailyRiverdaleMemeRepository, "updateDailyRiverdaleMemes", "out of context riverdale meme", Constants.CURSED_SWAMP_CHANNEL);
    }

    @Override
    public BaseDailyMemeEntity create(final long messageId, final String message, final String imageUrl, final LocalDateTime timeCreated, final LocalDateTime timeEdited) {
        return new DailyRiverdaleMemeEntity(messageId, message, imageUrl, timeCreated, timeEdited);
    }
}
