package com.badfic.philbot.data;

import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("daily_marvel_meme")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "messageId")
public class DailyMarvelMemeEntity {

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

    public DailyMarvelMemeEntity(long messageId, String message, String imageUrl, LocalDateTime timeCreated, LocalDateTime timeEdited) {
        this.messageId = messageId;
        this.message = message;
        this.imageUrl = imageUrl;
        this.timeCreated = timeCreated;
        this.timeEdited = timeEdited;
    }

}
