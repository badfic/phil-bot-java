package com.badfic.philbot.data;

import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Table("daily_marvel_meme")
@NoArgsConstructor
public class DailyMarvelMemeEntity extends BaseDailyMemeEntity {
    public DailyMarvelMemeEntity(final long messageId, final String message, final String imageUrl, final LocalDateTime timeCreated, final LocalDateTime timeEdited) {
        super(messageId, message, imageUrl, timeCreated, timeEdited);
    }
}
