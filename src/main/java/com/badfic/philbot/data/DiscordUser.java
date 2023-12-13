package com.badfic.philbot.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("discord_user")
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
    @JsonIgnore
    private Family family;

    @Column
    @JsonIgnore
    private LocalDateTime updateTime;

    @Column
    @JsonIgnore
    private LocalDateTime voiceJoined;

    @Column
    @JsonIgnore
    private LocalDateTime lastSlots;

    @Column
    @JsonIgnore
    private LocalDateTime lastMessageBonus;

    @Column
    @JsonIgnore
    private LocalDateTime lastVote;

    @Column
    @JsonIgnore
    private LocalDateTime acceptedBoost;

    @Column
    @JsonIgnore
    private LocalDateTime acceptedMapTrivia;

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
    private long lifetimeSweepstakesWins; // Keeping this for legacy reasons

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
    private long fightPoints;

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
