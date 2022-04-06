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

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime swiperExpiration;

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

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime triviaExpiration;

    @Column
    private String quoteTriviaMsgId;

    @Column
    private Short quoteTriviaCorrectAnswer;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime quoteTriviaExpiration;

    @Column
    private String nsfwQuoteTriviaMsgId;

    @Column
    private Short nsfwQuoteTriviaCorrectAnswer;

    @Column
    private long memberCountChannel;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime nsfwQuoteTriviaExpiration;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int normalMsgPoints = 5;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int pictureMsgPoints = 250;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int pictureMsgTimeoutMinutes = 3;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int reactionPoints = 7;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int vcPointsPerMinute = 5;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int noNoWordsPoints = 100;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int upvotePointsToUpvotee = 500;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int upvotePointsToUpvoter = 125;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int upvoteTimeoutMinutes = 1;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int downvotePointsFromDownvotee = 100;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int downvotePointsToDownvoter = 50;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int downvoteTimeoutMinutes = 1;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int slotsWinPoints = 10000;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int slotsTwoOfThreePoints = 50;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int slotsTimeoutMinutes = 3;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int carrotEventPoints = 1000;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG)
    private String carrotImg = "https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int potatoEventPoints = 1000;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG)
    private String potatoImg = "https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int boostEventPoints = 1000;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int percentChanceBoostHappensOnHour = 15;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG)
    private String boostStartImg = "https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG)
    private String boostEndImg = "https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int mapEventPoints = 100;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int triviaEventPoints = 100;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int quoteTriviaEventPoints = 100;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int nsfwQuoteTriviaEventPoints = 100;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int robinhoodMinPercent = 5;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int robinhoodMaxPercent = 16;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int percentChanceRobinhoodNotHappen = 30;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING)
    private String robinhoodPerson = "Guy Fieri";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING)
    private String robinhoodStopperPerson = "Saundra Lee";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING)
    private String robinhoodStopperPhrase = "I NEED TO SPEAK TO THE MANAGER!!!";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG)
    private String robinhoodImg = "https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG)
    private String robinhoodStoppedImg = "https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int scooterAnklePoints = 25000;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG)
    private String scooterAnkleImg = "https://cdn.discordapp.com/attachments/752665380182425677/780324301311049728/scooter_ankle.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int shrekoningMinPoints = 200;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int shrekoningMaxPoints = 4000;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG)
    private String shrekoningImg = "https://cdn.discordapp.com/attachments/741053845098201099/763280555793580042/the_shrekoning.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int stonksMaxPoints = 4000;

    // TODO: Stonks image list

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int sweepstakesPoints = 4000;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG)
    private String sweepstakesImg = "https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int swiperPoints = 1500;

    // TODO: Add the various swiper personas here too?

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int taxesMinPercent = 5;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int taxesMaxPercent = 16;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int percentChanceTaxesNotHappen = 30;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING)
    private String taxesPerson = "Guy Fieri";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING)
    private String taxesStopperPerson = "Saundra Lee";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING)
    private String taxesStopperPhrase = "I NEED TO SPEAK TO THE MANAGER!!!";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG)
    private String taxesImg = "https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG)
    private String taxesStoppedImg = "https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int trickOrTreatPoints = 500;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG)
    private String trickOrTreatImg = "https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING)
    private String trickOrTreatName = "Checkout or Trampled";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING)
    private String trickOrTreatTreatEmoji = "\uD83D\uDED2";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING)
    private String trickOrTreatTrickEmoji = "\uD83D\uDEA7";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.LONG)
    private long sfwSavedMemesChannelId = 0;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.LONG)
    private long nsfwSavedMemesChannelId = 0;

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

    public LocalDateTime getSwiperExpiration() {
        return swiperExpiration;
    }

    public void setSwiperExpiration(LocalDateTime swiperExpiration) {
        this.swiperExpiration = swiperExpiration;
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

    public String getTriviaMsgId() {
        return triviaMsgId;
    }

    public void setTriviaMsgId(String triviaMsgId) {
        this.triviaMsgId = triviaMsgId;
    }

    public UUID getTriviaGuid() {
        return triviaGuid;
    }

    public void setTriviaGuid(UUID triviaGuid) {
        this.triviaGuid = triviaGuid;
    }

    public LocalDateTime getTriviaExpiration() {
        return triviaExpiration;
    }

    public void setTriviaExpiration(LocalDateTime triviaExpiration) {
        this.triviaExpiration = triviaExpiration;
    }

    public String getQuoteTriviaMsgId() {
        return quoteTriviaMsgId;
    }

    public void setQuoteTriviaMsgId(String quoteTriviaMsgId) {
        this.quoteTriviaMsgId = quoteTriviaMsgId;
    }

    public Short getQuoteTriviaCorrectAnswer() {
        return quoteTriviaCorrectAnswer;
    }

    public void setQuoteTriviaCorrectAnswer(Short quoteTriviaCorrectAnswer) {
        this.quoteTriviaCorrectAnswer = quoteTriviaCorrectAnswer;
    }

    public LocalDateTime getQuoteTriviaExpiration() {
        return quoteTriviaExpiration;
    }

    public void setQuoteTriviaExpiration(LocalDateTime quoteTriviaExpiration) {
        this.quoteTriviaExpiration = quoteTriviaExpiration;
    }

    public String getNsfwQuoteTriviaMsgId() {
        return nsfwQuoteTriviaMsgId;
    }

    public void setNsfwQuoteTriviaMsgId(String nsfwQuoteTriviaMsgId) {
        this.nsfwQuoteTriviaMsgId = nsfwQuoteTriviaMsgId;
    }

    public Short getNsfwQuoteTriviaCorrectAnswer() {
        return nsfwQuoteTriviaCorrectAnswer;
    }

    public void setNsfwQuoteTriviaCorrectAnswer(Short nsfwQuoteTriviaCorrectAnswer) {
        this.nsfwQuoteTriviaCorrectAnswer = nsfwQuoteTriviaCorrectAnswer;
    }

    public LocalDateTime getNsfwQuoteTriviaExpiration() {
        return nsfwQuoteTriviaExpiration;
    }

    public void setNsfwQuoteTriviaExpiration(LocalDateTime nsfwQuoteTriviaExpiration) {
        this.nsfwQuoteTriviaExpiration = nsfwQuoteTriviaExpiration;
    }

    public long getMemberCountChannel() {
        return memberCountChannel;
    }

    public void setMemberCountChannel(long memberCountChannel) {
        this.memberCountChannel = memberCountChannel;
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

    public int getPictureMsgTimeoutMinutes() {
        return pictureMsgTimeoutMinutes;
    }

    public void setPictureMsgTimeoutMinutes(int pictureMsgTimeoutMinutes) {
        this.pictureMsgTimeoutMinutes = pictureMsgTimeoutMinutes;
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

    public int getUpvoteTimeoutMinutes() {
        return upvoteTimeoutMinutes;
    }

    public void setUpvoteTimeoutMinutes(int upvoteTimeoutMinutes) {
        this.upvoteTimeoutMinutes = upvoteTimeoutMinutes;
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

    public int getDownvoteTimeoutMinutes() {
        return downvoteTimeoutMinutes;
    }

    public void setDownvoteTimeoutMinutes(int downvoteTimeoutMinutes) {
        this.downvoteTimeoutMinutes = downvoteTimeoutMinutes;
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

    public int getSlotsTimeoutMinutes() {
        return slotsTimeoutMinutes;
    }

    public void setSlotsTimeoutMinutes(int slotsTimeoutMinutes) {
        this.slotsTimeoutMinutes = slotsTimeoutMinutes;
    }

    public int getCarrotEventPoints() {
        return carrotEventPoints;
    }

    public void setCarrotEventPoints(int carrotEventPoints) {
        this.carrotEventPoints = carrotEventPoints;
    }

    public String getCarrotImg() {
        return carrotImg;
    }

    public void setCarrotImg(String carrotImg) {
        this.carrotImg = carrotImg;
    }

    public int getPotatoEventPoints() {
        return potatoEventPoints;
    }

    public void setPotatoEventPoints(int potatoEventPoints) {
        this.potatoEventPoints = potatoEventPoints;
    }

    public String getPotatoImg() {
        return potatoImg;
    }

    public void setPotatoImg(String potatoImg) {
        this.potatoImg = potatoImg;
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

    public String getBoostStartImg() {
        return boostStartImg;
    }

    public void setBoostStartImg(String boostStartImg) {
        this.boostStartImg = boostStartImg;
    }

    public String getBoostEndImg() {
        return boostEndImg;
    }

    public void setBoostEndImg(String boostEndImg) {
        this.boostEndImg = boostEndImg;
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

    public int getQuoteTriviaEventPoints() {
        return quoteTriviaEventPoints;
    }

    public void setQuoteTriviaEventPoints(int quoteTriviaEventPoints) {
        this.quoteTriviaEventPoints = quoteTriviaEventPoints;
    }

    public int getNsfwQuoteTriviaEventPoints() {
        return nsfwQuoteTriviaEventPoints;
    }

    public void setNsfwQuoteTriviaEventPoints(int nsfwQuoteTriviaEventPoints) {
        this.nsfwQuoteTriviaEventPoints = nsfwQuoteTriviaEventPoints;
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

    public String getRobinhoodPerson() {
        return robinhoodPerson;
    }

    public void setRobinhoodPerson(String robinhoodPerson) {
        this.robinhoodPerson = robinhoodPerson;
    }

    public String getRobinhoodStopperPerson() {
        return robinhoodStopperPerson;
    }

    public void setRobinhoodStopperPerson(String robinhoodStopperPerson) {
        this.robinhoodStopperPerson = robinhoodStopperPerson;
    }

    public String getRobinhoodStopperPhrase() {
        return robinhoodStopperPhrase;
    }

    public void setRobinhoodStopperPhrase(String robinhoodStopperPhrase) {
        this.robinhoodStopperPhrase = robinhoodStopperPhrase;
    }

    public String getRobinhoodImg() {
        return robinhoodImg;
    }

    public void setRobinhoodImg(String robinhoodImg) {
        this.robinhoodImg = robinhoodImg;
    }

    public String getRobinhoodStoppedImg() {
        return robinhoodStoppedImg;
    }

    public void setRobinhoodStoppedImg(String robinhoodStoppedImg) {
        this.robinhoodStoppedImg = robinhoodStoppedImg;
    }

    public int getScooterAnklePoints() {
        return scooterAnklePoints;
    }

    public void setScooterAnklePoints(int scooterAnklePoints) {
        this.scooterAnklePoints = scooterAnklePoints;
    }

    public String getScooterAnkleImg() {
        return scooterAnkleImg;
    }

    public void setScooterAnkleImg(String scooterAnkleImg) {
        this.scooterAnkleImg = scooterAnkleImg;
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

    public String getShrekoningImg() {
        return shrekoningImg;
    }

    public void setShrekoningImg(String shrekoningImg) {
        this.shrekoningImg = shrekoningImg;
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

    public String getSweepstakesImg() {
        return sweepstakesImg;
    }

    public void setSweepstakesImg(String sweepstakesImg) {
        this.sweepstakesImg = sweepstakesImg;
    }

    public int getSwiperPoints() {
        return swiperPoints;
    }

    public void setSwiperPoints(int swiperPoints) {
        this.swiperPoints = swiperPoints;
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

    public String getTaxesPerson() {
        return taxesPerson;
    }

    public void setTaxesPerson(String taxesPerson) {
        this.taxesPerson = taxesPerson;
    }

    public String getTaxesStopperPerson() {
        return taxesStopperPerson;
    }

    public void setTaxesStopperPerson(String taxesStopperPerson) {
        this.taxesStopperPerson = taxesStopperPerson;
    }

    public String getTaxesStopperPhrase() {
        return taxesStopperPhrase;
    }

    public void setTaxesStopperPhrase(String taxesStopperPhrase) {
        this.taxesStopperPhrase = taxesStopperPhrase;
    }

    public String getTaxesImg() {
        return taxesImg;
    }

    public void setTaxesImg(String taxesImg) {
        this.taxesImg = taxesImg;
    }

    public String getTaxesStoppedImg() {
        return taxesStoppedImg;
    }

    public void setTaxesStoppedImg(String taxesStoppedImg) {
        this.taxesStoppedImg = taxesStoppedImg;
    }

    public int getTrickOrTreatPoints() {
        return trickOrTreatPoints;
    }

    public void setTrickOrTreatPoints(int trickOrTreatPoints) {
        this.trickOrTreatPoints = trickOrTreatPoints;
    }

    public String getTrickOrTreatImg() {
        return trickOrTreatImg;
    }

    public void setTrickOrTreatImg(String trickOrTreatImg) {
        this.trickOrTreatImg = trickOrTreatImg;
    }

    public String getTrickOrTreatName() {
        return trickOrTreatName;
    }

    public void setTrickOrTreatName(String trickOrTreatName) {
        this.trickOrTreatName = trickOrTreatName;
    }

    public String getTrickOrTreatTreatEmoji() {
        return trickOrTreatTreatEmoji;
    }

    public void setTrickOrTreatTreatEmoji(String trickOrTreatTreatEmoji) {
        this.trickOrTreatTreatEmoji = trickOrTreatTreatEmoji;
    }

    public String getTrickOrTreatTrickEmoji() {
        return trickOrTreatTrickEmoji;
    }

    public void setTrickOrTreatTrickEmoji(String trickOrTreatTrickEmoji) {
        this.trickOrTreatTrickEmoji = trickOrTreatTrickEmoji;
    }

    public long getSfwSavedMemesChannelId() {
        return sfwSavedMemesChannelId;
    }

    public void setSfwSavedMemesChannelId(long sfwSavedMemesChannelId) {
        this.sfwSavedMemesChannelId = sfwSavedMemesChannelId;
    }

    public long getNsfwSavedMemesChannelId() {
        return nsfwSavedMemesChannelId;
    }

    public void setNsfwSavedMemesChannelId(long nsfwSavedMemesChannelId) {
        this.nsfwSavedMemesChannelId = nsfwSavedMemesChannelId;
    }

}
