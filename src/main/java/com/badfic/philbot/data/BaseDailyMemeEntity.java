package com.badfic.philbot.data;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

@Getter
@Setter
@NoArgsConstructor
public class BaseDailyMemeEntity {

    @Id
    private long messageId;

    @Column
    private String message;

    @Column
    private String imageUrl;

    @Column
    private LocalDateTime timeCreated;

    @Column
    private LocalDateTime timeEdited;

    public BaseDailyMemeEntity(final long messageId, final String message, final String imageUrl, final LocalDateTime timeCreated, final LocalDateTime timeEdited) {
        this.messageId = messageId;
        this.message = message;
        this.imageUrl = imageUrl;
        this.timeCreated = timeCreated;
        this.timeEdited = timeEdited;
    }

}
