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

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime lastMessageBonus = LocalDateTime.now();

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime lastVote = LocalDateTime.now();

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime lastSteal = LocalDateTime.now();

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime lastFlip = LocalDateTime.now();

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime acceptedBoost = LocalDateTime.now();

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

    public LocalDateTime getLastMessageBonus() {
        return lastMessageBonus;
    }

    public void setLastMessageBonus(LocalDateTime lastMessageBonus) {
        this.lastMessageBonus = lastMessageBonus;
    }

    public LocalDateTime getLastVote() {
        return lastVote;
    }

    public void setLastVote(LocalDateTime lastVote) {
        this.lastVote = lastVote;
    }

    public LocalDateTime getLastSteal() {
        return lastSteal;
    }

    public void setLastSteal(LocalDateTime lastSteal) {
        this.lastSteal = lastSteal;
    }

    public LocalDateTime getLastFlip() {
        return lastFlip;
    }

    public void setLastFlip(LocalDateTime lastFlip) {
        this.lastFlip = lastFlip;
    }

    public LocalDateTime getAcceptedBoost() {
        return acceptedBoost;
    }

    public void setAcceptedBoost(LocalDateTime acceptedBoost) {
        this.acceptedBoost = acceptedBoost;
    }
}
