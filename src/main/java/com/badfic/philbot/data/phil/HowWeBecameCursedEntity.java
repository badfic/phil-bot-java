package com.badfic.philbot.data.phil;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "how_we_became_cursed")
public class HowWeBecameCursedEntity {

    @Id
    private long messageId;

    @Column
    private String message;

    @Column
    private LocalDateTime timeCreated;

    @Column
    private LocalDateTime timeEdited;

    public HowWeBecameCursedEntity() {
    }

    public HowWeBecameCursedEntity(long messageId, String message, LocalDateTime timeCreated, LocalDateTime timeEdited) {
        this.messageId = messageId;
        this.message = message;
        this.timeCreated = timeCreated;
        this.timeEdited = timeEdited;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(LocalDateTime timeCreated) {
        this.timeCreated = timeCreated;
    }

    public LocalDateTime getTimeEdited() {
        return timeEdited;
    }

    public void setTimeEdited(LocalDateTime timeEdited) {
        this.timeEdited = timeEdited;
    }
}
