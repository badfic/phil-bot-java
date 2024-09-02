package com.badfic.philbot.data;

import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Table("daily_legends_meme")
@NoArgsConstructor
public class DailyLegendsMemeEntity extends BaseDailyMemeEntity {

    public DailyLegendsMemeEntity(final long messageId, final String message, final String imageUrl, final LocalDateTime timeCreated, final LocalDateTime timeEdited) {
        super(messageId, message, imageUrl, timeCreated, timeEdited);
    }

}
