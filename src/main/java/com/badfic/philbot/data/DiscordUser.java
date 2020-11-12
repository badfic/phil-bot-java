package com.badfic.philbot.data;

import java.time.LocalDateTime;
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
    private long boostParticipations;

    @Column
    private long swiperParticipations;

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

    public long getBoostParticipations() {
        return boostParticipations;
    }

    public void setBoostParticipations(long boostParticipations) {
        this.boostParticipations = boostParticipations;
    }

    public long getSwiperParticipations() {
        return swiperParticipations;
    }

    public void setSwiperParticipations(long swiperParticipations) {
        this.swiperParticipations = swiperParticipations;
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

}
