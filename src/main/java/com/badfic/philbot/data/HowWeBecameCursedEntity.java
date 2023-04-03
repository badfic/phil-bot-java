package com.badfic.philbot.data;

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
@Table(name = "how_we_became_cursed")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "messageId")
public class HowWeBecameCursedEntity {

    @Id
    private long messageId;

    @Column
    private String message;

    @Column
    private LocalDateTime timeCreated;

    @Column
    private LocalDateTime timeEdited;

    public HowWeBecameCursedEntity(long messageId, String message, LocalDateTime timeCreated, LocalDateTime timeEdited) {
        this.messageId = messageId;
        this.message = message;
        this.timeCreated = timeCreated;
        this.timeEdited = timeEdited;
    }

}
