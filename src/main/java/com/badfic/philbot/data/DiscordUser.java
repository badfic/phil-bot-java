package com.badfic.philbot.data;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "discord_user")
public class DiscordUser {

    @Id
    private String id;

    @Column
    private long xp;

    @Column(columnDefinition = "TIMESTAMP")
    @UpdateTimestamp
    private LocalDateTime updateTime = LocalDateTime.now();

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime voiceJoined;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime lastSlots = LocalDateTime.now();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getXp() {
        return xp;
    }

    public void setXp(long xp) {
        this.xp = xp;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getVoiceJoined() {
        return voiceJoined;
    }

    public void setVoiceJoined(LocalDateTime voiceJoined) {
        this.voiceJoined = voiceJoined;
    }

    public LocalDateTime getLastSlots() {
        return lastSlots;
    }

    public void setLastSlots(LocalDateTime lastSlots) {
        this.lastSlots = lastSlots;
    }
}
