package com.badfic.philbot.data;

import com.badfic.philbot.config.ControllerConfigurable;
import com.badfic.philbot.data.converters.JsonSetConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "swampy_games_config")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
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
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING)
    private String carrotName = "The Carrot Person";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int potatoEventPoints = 1000;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG)
    private String potatoImg = "https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING)
    private String potatoName = "The Potato Person";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT)
    private int glitterEventPoints = 1000;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG)
    private String glitterImg = "https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING)
    private String glitterName = "The Glitter Person";

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
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING_SET)
    @Convert(converter = JsonSetConverter.class)
    private Set<String> slotsEmoji = Set.of("🍓","🍍","🍊","🍋","🍇","🍉","🍌","🍒","🍎");

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING_SET)
    @Convert(converter = JsonSetConverter.class)
    private Set<String> boostWords = Set.of("boost");

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING_SET)
    @Convert(converter = JsonSetConverter.class)
    private Set<String> monthlyColors = Set.of("#599111");

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING_SET)
    @Convert(converter = JsonSetConverter.class)
    private Set<String> embedFooters = Set.of("powered by 777");

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.LONG)
    private long sfwSavedMemesChannelId = 0;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.LONG)
    private long nsfwSavedMemesChannelId = 0;

}
