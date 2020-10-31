package com.badfic.philbot.data;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Convert;
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

    @Column
    @Convert(converter = FamilyJsonConverter.class)
    private Family family;

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
    private LocalDateTime acceptedBoost = LocalDateTime.now();

    @Column
    private UUID triviaGuid;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime triviaGuidTime;

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

    public Family getFamily() {
        return family;
    }

    public void setFamily(Family family) {
        this.family = family;
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

    public LocalDateTime getAcceptedBoost() {
        return acceptedBoost;
    }

    public void setAcceptedBoost(LocalDateTime acceptedBoost) {
        this.acceptedBoost = acceptedBoost;
    }

    public void setTriviaGuid(UUID triviaGuid) {
        this.triviaGuid = triviaGuid;
    }

    public UUID getTriviaGuid() {
        return triviaGuid;
    }

    public void setTriviaGuidTime(LocalDateTime triviaGuidTime) {
        this.triviaGuidTime = triviaGuidTime;
    }

    public LocalDateTime getTriviaGuidTime() {
        return triviaGuidTime;
    }
}
