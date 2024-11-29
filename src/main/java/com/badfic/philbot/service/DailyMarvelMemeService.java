package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.BaseDailyMemeEntity;
import com.badfic.philbot.data.DailyMarvelMemeEntity;
import com.badfic.philbot.data.DailyMarvelMemeRepository;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DailyMarvelMemeService extends BaseDailyMemeService {
    public DailyMarvelMemeService(final DailyMarvelMemeRepository dailyMarvelMemeRepository) {
        super(dailyMarvelMemeRepository, "updateDailyMarvelMemes", "Out of context Marvel meme", Constants.MEMES_CHANNEL);
    }

    @Override
    public BaseDailyMemeEntity create(final long messageId, final String message, final String imageUrl, final LocalDateTime timeCreated, final LocalDateTime timeEdited) {
        return new DailyMarvelMemeEntity(messageId, message, imageUrl, timeCreated, timeEdited);
    }
}
