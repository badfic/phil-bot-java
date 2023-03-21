package com.badfic.philbot.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "discord_user")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class DiscordUser {

    @Id
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
    private long lifetimeUpvotesReceived;

    @Column
    private long upvoterPoints;

    @Column
    private long lifetimeUpvotesGiven;

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
    private long lifetimeSlotsWins;

    @Column
    private long slotsLosses;

    @Column
    private long lifetimeSlotsLosses;

    @Column
    private long mapsPoints;

    @Column
    private long lifetimeMapsCorrect;

    @Column
    private long triviaPoints;

    @Column
    private long lifetimeTriviasCorrect;

    @Column
    private long quoteTriviaPoints;

    @Column
    private long lifetimeQuoteTriviasCorrect;

    @Column
    private long trickOrTreatPoints;

    @Column
    private long scooterPoints;

    @Column
    private long sweepstakesPoints;

    @Column
    private long lifetimeSweepstakesWins;

    @Column
    private long noNoPoints;

    @Column
    private long lifetimeNoNoWords;

    @Column
    private long messagePoints;

    @Column
    private long lifetimeMessages;

    @Column
    private long pictureMessagePoints;

    @Column
    private long lifetimePictures;

    @Column
    private long boostPoints;

    @Column
    private long lifetimeBoostParticipations;

    @Column
    private long voiceChatPoints;

    @Column
    private long lifetimeVoiceChatMinutes;

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

    public void setUpvotedPoints(long upvotedPoints) {
        this.upvotedPoints = upvotedPoints;
        lifetimeUpvotesReceived++;
    }

    public void setUpvoterPoints(long upvoterPoints) {
        this.upvoterPoints = upvoterPoints;
        lifetimeUpvotesGiven++;
    }

    public void setSlotsCloseEnoughPoints(long slotsPoints) {
        this.slotsCloseEnoughPoints = slotsPoints;
        lifetimeSlotsLosses++;
    }

    public void setSlotsWinnerWinnerPoints(long slotsWinnerWinnerPoints) {
        this.slotsWinnerWinnerPoints = slotsWinnerWinnerPoints;
        lifetimeSlotsWins++;
    }

    public void setSlotsLosses(long slotsLosses) {
        this.slotsLosses = slotsLosses;
        lifetimeSlotsLosses++;
    }

    public void setMapsPoints(long mapsPoints) {
        if (mapsPoints > this.mapsPoints) {
            lifetimeMapsCorrect++;
        }

        this.mapsPoints = mapsPoints;
    }

    public void setTriviaPoints(long triviaPoints) {
        if (triviaPoints > this.triviaPoints) {
            lifetimeTriviasCorrect++;
        }

        this.triviaPoints = triviaPoints;
    }

    public void setQuoteTriviaPoints(long quoteTriviaPoints) {
        if (quoteTriviaPoints > this.quoteTriviaPoints) {
            lifetimeQuoteTriviasCorrect++;
        }

        this.quoteTriviaPoints = quoteTriviaPoints;
    }

    public void setSweepstakesPoints(long sweepstakesPoints) {
        this.sweepstakesPoints = sweepstakesPoints;
        lifetimeSweepstakesWins++;
    }

    public void setNoNoPoints(long noNoPoints) {
        this.noNoPoints = noNoPoints;
        lifetimeNoNoWords++;
    }

    public void setMessagePoints(long messagePoints) {
        this.messagePoints = messagePoints;
        lifetimeMessages++;
    }

    public void setPictureMessagePoints(long pictureMessagePoints) {
        this.pictureMessagePoints = pictureMessagePoints;
        lifetimePictures++;
    }

    public void setBoostPoints(long boostPoints) {
        this.boostPoints = boostPoints;
        lifetimeBoostParticipations++;
    }

}
