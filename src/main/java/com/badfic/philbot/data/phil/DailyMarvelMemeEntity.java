package com.badfic.philbot.data.phil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "daily_marvel_meme")
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
