package com.badfic.philbot.data.phil;

import com.badfic.philbot.config.ControllerConfigurable;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "swampy_games_config")
public class SwampyGamesConfig {
    public static final Short SINGLETON_ID = 1;

    @Id
    private Short id;

    @Column
    private String swiperAwaiting;

    @Column
    private String swiperSavior;

    @Column
    private String noSwipingPhrase;

    @Column
    private String boostPhrase;

    @Column
    private String mapPhrase;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime mapTriviaExpiration;

    @Column
    private long mostRecentTaxes;

    @Column
    private String triviaMsgId;

    @Column
    private UUID triviaGuid;

    @Column
    @ControllerConfigurable
    private int normalMsgPoints = 5;

    @Column
    @ControllerConfigurable
    private int pictureMsgPoints = 250;

    @Column
    @ControllerConfigurable
    private int reactionPoints = 7;

    @Column
    @ControllerConfigurable
    private int vcPointsPerMinute = 5;

    @Column
    @ControllerConfigurable
    private int noNoWordsPoints = 100;

    @Column
    @ControllerConfigurable
    private int upvoteTimeoutMinutes = 1;

    @Column
    @ControllerConfigurable
    private int downvoteTimeoutMinutes = 1;

    @Column
    @ControllerConfigurable
    private int upvotePointsToUpvotee = 500;

    @Column
    @ControllerConfigurable
    private int upvotePointsToUpvoter = 125;

    @Column
    @ControllerConfigurable
    private int downvotePointsFromDownvotee = 100;

    @Column
    @ControllerConfigurable
    private int downvotePointsToDownvoter = 50;

    @Column
    @ControllerConfigurable
    private int slotsWinPoints = 10000;

    @Column
    @ControllerConfigurable
    private int slotsTwoOfThreePoints = 50;

    @Column
    @ControllerConfigurable
    private int pictureMsgTimeoutMinutes = 3;

    @Column
    @ControllerConfigurable
    private int slotsTimeoutMinutes = 3;

    @Column
    @ControllerConfigurable
    private int boostEventPoints = 1000;

    @Column
    @ControllerConfigurable
    private int percentChanceBoostHappensOnHour = 15;

    @Column
    @ControllerConfigurable
    private int mapEventPoints = 100;

    @Column
    @ControllerConfigurable
    private int triviaEventPoints = 100;

    @Column
    @ControllerConfigurable
    private int robinhoodMinPercent = 5;

    @Column
    @ControllerConfigurable
    private int robinhoodMaxPercent = 16;

    @Column
    @ControllerConfigurable
    private int percentChanceRobinhoodNotHappen = 30;

    @Column
    @ControllerConfigurable
    private int taxesMinPercent = 5;

    @Column
    @ControllerConfigurable
    private int taxesMaxPercent = 16;

    @Column
    @ControllerConfigurable
    private int percentChanceTaxesNotHappen = 30;

    @Column
    @ControllerConfigurable
    private int scooterAnklePoints = 25000;

    @Column
    @ControllerConfigurable
    private int shrekoningMinPoints = 200;

    @Column
    @ControllerConfigurable
    private int shrekoningMaxPoints = 4000;

    @Column
    @ControllerConfigurable
    private int stonksMaxPoints = 4000;

    @Column
    @ControllerConfigurable
    private int sweepstakesPoints = 4000;

    @Column
    @ControllerConfigurable
    private int swiperPoints = 1500;

    @Column
    @ControllerConfigurable
    private int trickOrTreatPoints = 500;

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public String getSwiperAwaiting() {
        return swiperAwaiting;
    }

    public void setSwiperAwaiting(String swiperAwaiting) {
        this.swiperAwaiting = swiperAwaiting;
    }

    public String getSwiperSavior() {
        return swiperSavior;
    }

    public void setSwiperSavior(String swiperSavior) {
        this.swiperSavior = swiperSavior;
    }

    public String getNoSwipingPhrase() {
        return noSwipingPhrase;
    }

    public void setNoSwipingPhrase(String noSwipingPhrase) {
        this.noSwipingPhrase = noSwipingPhrase;
    }

    public String getBoostPhrase() {
        return boostPhrase;
    }

    public void setBoostPhrase(String boostPhrase) {
        this.boostPhrase = boostPhrase;
    }

    public String getMapPhrase() {
        return mapPhrase;
    }

    public void setMapPhrase(String mapPhrase) {
        this.mapPhrase = mapPhrase;
    }

    public LocalDateTime getMapTriviaExpiration() {
        return mapTriviaExpiration;
    }

    public void setMapTriviaExpiration(LocalDateTime mapTriviaExpiration) {
        this.mapTriviaExpiration = mapTriviaExpiration;
    }

    public long getMostRecentTaxes() {
        return mostRecentTaxes;
    }

    public void setMostRecentTaxes(long mostRecentTaxes) {
        this.mostRecentTaxes = mostRecentTaxes;
    }

    public void setTriviaMsgId(String triviaMsgId) {
        this.triviaMsgId = triviaMsgId;
    }

    public String getTriviaMsgId() {
        return triviaMsgId;
    }

    public UUID getTriviaGuid() {
        return triviaGuid;
    }

    public void setTriviaGuid(UUID triviaGuid) {
        this.triviaGuid = triviaGuid;
    }

    public int getNormalMsgPoints() {
        return normalMsgPoints;
    }

    public void setNormalMsgPoints(int normalMsgPoints) {
        this.normalMsgPoints = normalMsgPoints;
    }

    public int getPictureMsgPoints() {
        return pictureMsgPoints;
    }

    public void setPictureMsgPoints(int pictureMsgPoints) {
        this.pictureMsgPoints = pictureMsgPoints;
    }

    public int getReactionPoints() {
        return reactionPoints;
    }

    public void setReactionPoints(int reactionPoints) {
        this.reactionPoints = reactionPoints;
    }

    public int getVcPointsPerMinute() {
        return vcPointsPerMinute;
    }

    public void setVcPointsPerMinute(int vcPointsPerMinute) {
        this.vcPointsPerMinute = vcPointsPerMinute;
    }

    public int getNoNoWordsPoints() {
        return noNoWordsPoints;
    }

    public void setNoNoWordsPoints(int noNoWordsPoints) {
        this.noNoWordsPoints = noNoWordsPoints;
    }

    public int getUpvoteTimeoutMinutes() {
        return upvoteTimeoutMinutes;
    }

    public void setUpvoteTimeoutMinutes(int upvoteTimeoutMinutes) {
        this.upvoteTimeoutMinutes = upvoteTimeoutMinutes;
    }

    public int getDownvoteTimeoutMinutes() {
        return downvoteTimeoutMinutes;
    }

    public void setDownvoteTimeoutMinutes(int downvoteTimeoutMinutes) {
        this.downvoteTimeoutMinutes = downvoteTimeoutMinutes;
    }

    public int getUpvotePointsToUpvotee() {
        return upvotePointsToUpvotee;
    }

    public void setUpvotePointsToUpvotee(int upvotePointsToUpvotee) {
        this.upvotePointsToUpvotee = upvotePointsToUpvotee;
    }

    public int getUpvotePointsToUpvoter() {
        return upvotePointsToUpvoter;
    }

    public void setUpvotePointsToUpvoter(int upvotePointsToUpvoter) {
        this.upvotePointsToUpvoter = upvotePointsToUpvoter;
    }

    public int getDownvotePointsFromDownvotee() {
        return downvotePointsFromDownvotee;
    }

    public void setDownvotePointsFromDownvotee(int downvotePointsFromDownvotee) {
        this.downvotePointsFromDownvotee = downvotePointsFromDownvotee;
    }

    public int getDownvotePointsToDownvoter() {
        return downvotePointsToDownvoter;
    }

    public void setDownvotePointsToDownvoter(int downvotePointsToDownvoter) {
        this.downvotePointsToDownvoter = downvotePointsToDownvoter;
    }

    public int getSlotsWinPoints() {
        return slotsWinPoints;
    }

    public void setSlotsWinPoints(int slotsWinPoints) {
        this.slotsWinPoints = slotsWinPoints;
    }

    public int getSlotsTwoOfThreePoints() {
        return slotsTwoOfThreePoints;
    }

    public void setSlotsTwoOfThreePoints(int slotsTwoOfThreePoints) {
        this.slotsTwoOfThreePoints = slotsTwoOfThreePoints;
    }

    public int getPictureMsgTimeoutMinutes() {
        return pictureMsgTimeoutMinutes;
    }

    public void setPictureMsgTimeoutMinutes(int pictureMsgTimeoutMinutes) {
        this.pictureMsgTimeoutMinutes = pictureMsgTimeoutMinutes;
    }

    public int getSlotsTimeoutMinutes() {
        return slotsTimeoutMinutes;
    }

    public void setSlotsTimeoutMinutes(int slotsTimeoutMinutes) {
        this.slotsTimeoutMinutes = slotsTimeoutMinutes;
    }

    public int getBoostEventPoints() {
        return boostEventPoints;
    }

    public void setBoostEventPoints(int boostEventPoints) {
        this.boostEventPoints = boostEventPoints;
    }

    public int getPercentChanceBoostHappensOnHour() {
        return percentChanceBoostHappensOnHour;
    }

    public void setPercentChanceBoostHappensOnHour(int percentChanceBoostHappensOnHour) {
        this.percentChanceBoostHappensOnHour = percentChanceBoostHappensOnHour;
    }

    public int getMapEventPoints() {
        return mapEventPoints;
    }

    public void setMapEventPoints(int mapEventPoints) {
        this.mapEventPoints = mapEventPoints;
    }

    public int getTriviaEventPoints() {
        return triviaEventPoints;
    }

    public void setTriviaEventPoints(int triviaEventPoints) {
        this.triviaEventPoints = triviaEventPoints;
    }

    public int getRobinhoodMinPercent() {
        return robinhoodMinPercent;
    }

    public void setRobinhoodMinPercent(int robinhoodMinPercent) {
        this.robinhoodMinPercent = robinhoodMinPercent;
    }

    public int getRobinhoodMaxPercent() {
        return robinhoodMaxPercent;
    }

    public void setRobinhoodMaxPercent(int robinhoodMaxPercent) {
        this.robinhoodMaxPercent = robinhoodMaxPercent;
    }

    public int getPercentChanceRobinhoodNotHappen() {
        return percentChanceRobinhoodNotHappen;
    }

    public void setPercentChanceRobinhoodNotHappen(int percentChanceRobinhoodNotHappen) {
        this.percentChanceRobinhoodNotHappen = percentChanceRobinhoodNotHappen;
    }

    public int getTaxesMinPercent() {
        return taxesMinPercent;
    }

    public void setTaxesMinPercent(int taxesMinPercent) {
        this.taxesMinPercent = taxesMinPercent;
    }

    public int getTaxesMaxPercent() {
        return taxesMaxPercent;
    }

    public void setTaxesMaxPercent(int taxesMaxPercent) {
        this.taxesMaxPercent = taxesMaxPercent;
    }

    public int getPercentChanceTaxesNotHappen() {
        return percentChanceTaxesNotHappen;
    }

    public void setPercentChanceTaxesNotHappen(int percentChanceTaxesNotHappen) {
        this.percentChanceTaxesNotHappen = percentChanceTaxesNotHappen;
    }

    public int getScooterAnklePoints() {
        return scooterAnklePoints;
    }

    public void setScooterAnklePoints(int scooterAnklePoints) {
        this.scooterAnklePoints = scooterAnklePoints;
    }

    public int getShrekoningMinPoints() {
        return shrekoningMinPoints;
    }

    public void setShrekoningMinPoints(int shrekoningMinPoints) {
        this.shrekoningMinPoints = shrekoningMinPoints;
    }

    public int getShrekoningMaxPoints() {
        return shrekoningMaxPoints;
    }

    public void setShrekoningMaxPoints(int shrekoningMaxPoints) {
        this.shrekoningMaxPoints = shrekoningMaxPoints;
    }

    public int getStonksMaxPoints() {
        return stonksMaxPoints;
    }

    public void setStonksMaxPoints(int stonksMaxPoints) {
        this.stonksMaxPoints = stonksMaxPoints;
    }

    public int getSweepstakesPoints() {
        return sweepstakesPoints;
    }

    public void setSweepstakesPoints(int sweepstakesPoints) {
        this.sweepstakesPoints = sweepstakesPoints;
    }

    public int getSwiperPoints() {
        return swiperPoints;
    }

    public void setSwiperPoints(int swiperPoints) {
        this.swiperPoints = swiperPoints;
    }

    public int getTrickOrTreatPoints() {
        return trickOrTreatPoints;
    }

    public void setTrickOrTreatPoints(int trickOrTreatPoints) {
        this.trickOrTreatPoints = trickOrTreatPoints;
    }
}
