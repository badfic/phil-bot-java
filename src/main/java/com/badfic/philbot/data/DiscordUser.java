package com.badfic.philbot.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "discord_user")
public class DiscordUser {

    @Id
    @JsonIgnore
    private String id;

    @Transient
    private String profileUrl;

    @Transient
    private String nickname;

    @Column
    private long xp;

    @Column
    private long swiperParticipations;

    @Column
    @Convert(converter = FamilyJsonConverter.class)
    @JsonIgnore
    private Family family;

    @Column(columnDefinition = "TIMESTAMP")
    @UpdateTimestamp
    @JsonIgnore
    private LocalDateTime updateTime = LocalDateTime.now();

    @Column(columnDefinition = "TIMESTAMP")
    @JsonIgnore
    private LocalDateTime voiceJoined;

    @Column(columnDefinition = "TIMESTAMP")
    @JsonIgnore
    private LocalDateTime lastSlots = LocalDateTime.now();

    @Column(columnDefinition = "TIMESTAMP")
    @JsonIgnore
    private LocalDateTime lastMessageBonus = LocalDateTime.now();

    @Column(columnDefinition = "TIMESTAMP")
    @JsonIgnore
    private LocalDateTime lastVote = LocalDateTime.now();

    @Column(columnDefinition = "TIMESTAMP")
    @JsonIgnore
    private LocalDateTime acceptedBoost = LocalDateTime.now();

    @Column(columnDefinition = "TIMESTAMP")
    @JsonIgnore
    private LocalDateTime acceptedMapTrivia = LocalDateTime.now();

    @Column
    private long modGavePoints;

    @Column
    private long modTookPoints;

    @Column
    private long upvotedPoints;

    @Column
    private long upvoterPoints;

    @Column
    private long downvotedPoints;

    @Column
    private long downvoterPoints;

    @Column
    private long swiperPoints;

    @Column
    private long slotsCloseEnoughPoints;

    @Column
    private long slotsWinnerWinnerPoints;

    @Column
    private long slotsLosses;

    @Column
    private long mapsPoints;

    @Column
    private long triviaPoints;

    @Column
    private long trickOrTreatPoints;

    @Column
    private long scooterPoints;

    @Column
    private long sweepstakesPoints;

    @Column
    private long noNoPoints;

    @Column
    private long messagePoints;

    @Column
    private long pictureMessagePoints;

    @Column
    private long boostPoints;

    @Column
    private long voiceChatPoints;

    @Column
    private long reactPoints;

    @Column
    private long reactedPoints;

    @Column
    private long shrekoningPoints;

    @Column
    private long stonksPoints;

    @Column
    private long fightPoints;

    @Column
    private long taxesPoints;

    @Column
    private long robinhoodPoints;

    @Transient
    private long taxesRobinhoodNet;

    @Column
    private long scooterParticipant;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public long getXp() {
        return xp;
    }

    public void setXp(long xp) {
        this.xp = xp;
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

    public LocalDateTime getAcceptedMapTrivia() {
        return acceptedMapTrivia;
    }

    public void setAcceptedMapTrivia(LocalDateTime acceptedMapTrivia) {
        this.acceptedMapTrivia = acceptedMapTrivia;
    }

    public long getModGavePoints() {
        return modGavePoints;
    }

    public void setModGavePoints(long modPoints) {
        this.modGavePoints = modPoints;
    }

    public long getModTookPoints() {
        return modTookPoints;
    }

    public void setModTookPoints(long modTookPoints) {
        this.modTookPoints = modTookPoints;
    }

    public long getUpvotedPoints() {
        return upvotedPoints;
    }

    public void setUpvotedPoints(long upvotedPoints) {
        this.upvotedPoints = upvotedPoints;
    }

    public long getUpvoterPoints() {
        return upvoterPoints;
    }

    public void setUpvoterPoints(long upvoterPoints) {
        this.upvoterPoints = upvoterPoints;
    }

    public long getDownvotedPoints() {
        return downvotedPoints;
    }

    public void setDownvotedPoints(long downvotedPoints) {
        this.downvotedPoints = downvotedPoints;
    }

    public long getDownvoterPoints() {
        return downvoterPoints;
    }

    public void setDownvoterPoints(long downvoterPoints) {
        this.downvoterPoints = downvoterPoints;
    }

    public long getSwiperPoints() {
        return swiperPoints;
    }

    public void setSwiperPoints(long swiperPoints) {
        this.swiperPoints = swiperPoints;
    }

    public long getSlotsCloseEnoughPoints() {
        return slotsCloseEnoughPoints;
    }

    public void setSlotsCloseEnoughPoints(long slotsPoints) {
        this.slotsCloseEnoughPoints = slotsPoints;
    }

    public long getSlotsWinnerWinnerPoints() {
        return slotsWinnerWinnerPoints;
    }

    public void setSlotsWinnerWinnerPoints(long slotsWinnerWinnerPoints) {
        this.slotsWinnerWinnerPoints = slotsWinnerWinnerPoints;
    }

    public long getSlotsLosses() {
        return slotsLosses;
    }

    public void setSlotsLosses(long slotsLosses) {
        this.slotsLosses = slotsLosses;
    }

    public long getMapsPoints() {
        return mapsPoints;
    }

    public void setMapsPoints(long mapsPoints) {
        this.mapsPoints = mapsPoints;
    }

    public long getTriviaPoints() {
        return triviaPoints;
    }

    public void setTriviaPoints(long triviaPoints) {
        this.triviaPoints = triviaPoints;
    }

    public long getTrickOrTreatPoints() {
        return trickOrTreatPoints;
    }

    public void setTrickOrTreatPoints(long trickOrTreatPoints) {
        this.trickOrTreatPoints = trickOrTreatPoints;
    }

    public long getScooterPoints() {
        return scooterPoints;
    }

    public void setScooterPoints(long scooterPoints) {
        this.scooterPoints = scooterPoints;
    }

    public long getSweepstakesPoints() {
        return sweepstakesPoints;
    }

    public void setSweepstakesPoints(long sweepstakesPoints) {
        this.sweepstakesPoints = sweepstakesPoints;
    }

    public long getNoNoPoints() {
        return noNoPoints;
    }

    public void setNoNoPoints(long noNoPoints) {
        this.noNoPoints = noNoPoints;
    }

    public long getMessagePoints() {
        return messagePoints;
    }

    public void setMessagePoints(long messagePoints) {
        this.messagePoints = messagePoints;
    }

    public long getPictureMessagePoints() {
        return pictureMessagePoints;
    }

    public void setPictureMessagePoints(long pictureMessagePoints) {
        this.pictureMessagePoints = pictureMessagePoints;
    }

    public long getBoostPoints() {
        return boostPoints;
    }

    public void setBoostPoints(long boostPoints) {
        this.boostPoints = boostPoints;
    }

    public long getVoiceChatPoints() {
        return voiceChatPoints;
    }

    public void setVoiceChatPoints(long voiceChatPoints) {
        this.voiceChatPoints = voiceChatPoints;
    }

    public long getReactPoints() {
        return reactPoints;
    }

    public void setReactPoints(long reactPoints) {
        this.reactPoints = reactPoints;
    }

    public long getReactedPoints() {
        return reactedPoints;
    }

    public void setReactedPoints(long reactedPoints) {
        this.reactedPoints = reactedPoints;
    }

    public long getShrekoningPoints() {
        return shrekoningPoints;
    }

    public void setShrekoningPoints(long shrekoningPoints) {
        this.shrekoningPoints = shrekoningPoints;
    }

    public long getStonksPoints() {
        return stonksPoints;
    }

    public void setStonksPoints(long stonksPoints) {
        this.stonksPoints = stonksPoints;
    }

    public long getFightPoints() {
        return fightPoints;
    }

    public void setFightPoints(long fightPoints) {
        this.fightPoints = fightPoints;
    }

    public long getTaxesPoints() {
        return taxesPoints;
    }

    public void setTaxesPoints(long taxesPoints) {
        this.taxesPoints = taxesPoints;
    }

    public long getRobinhoodPoints() {
        return robinhoodPoints;
    }

    public void setRobinhoodPoints(long robinhoodPoints) {
        this.robinhoodPoints = robinhoodPoints;
    }

    public long getTaxesRobinhoodNet() {
        return taxesPoints + robinhoodPoints;
    }

    public long getScooterParticipant() {
        return scooterParticipant;
    }

    public void setScooterParticipant(long scooterParticipation) {
        this.scooterParticipant = scooterParticipation;
    }

}
