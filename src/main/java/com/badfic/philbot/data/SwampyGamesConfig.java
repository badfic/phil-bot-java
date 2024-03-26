package com.badfic.philbot.data;

import com.badfic.philbot.config.ControllerConfigurable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("swampy_games_config")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class SwampyGamesConfig {

    @Id
    private Short id;

    @Column
    private String boostPhrase;

    @Column
    private LocalDateTime boostStartTime;

    @Column
    private String mapPhrase;

    @Column
    private LocalDateTime mapTriviaExpiration;

    @Column
    private String triviaMsgId;

    @Column
    private UUID triviaGuid;

    @Column
    private LocalDateTime triviaExpiration;

    @Column
    private String quoteTriviaMsgId;

    @Column
    private Short quoteTriviaCorrectAnswer;

    @Column
    private LocalDateTime quoteTriviaExpiration;

    @Column
    private String nsfwQuoteTriviaMsgId;

    @Column
    private Short nsfwQuoteTriviaCorrectAnswer;

    @Column
    private LocalDateTime nsfwQuoteTriviaExpiration;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.MESSAGE)
    private int normalMsgPoints = 5;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.MESSAGE)
    private int pictureMsgPoints = 250;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.MESSAGE)
    private int pictureMsgTimeoutMinutes = 3;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.MESSAGE)
    private int reactionPoints = 7;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.MESSAGE)
    private int vcPointsPerMinute = 5;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.MESSAGE)
    private int noNoWordsPoints = 100;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.UPVOTE)
    private int upvotePointsToUpvotee = 500;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.UPVOTE)
    private int upvotePointsToUpvoter = 125;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.UPVOTE)
    private int upvoteTimeoutMinutes = 1;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.UPVOTE)
    private int downvotePointsFromDownvotee = 100;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.UPVOTE)
    private int downvotePointsToDownvoter = 50;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.UPVOTE)
    private int downvoteTimeoutMinutes = 1;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.SLOTS)
    private int slotsWinPoints = 10000;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.SLOTS)
    private int slotsTwoOfThreePoints = 50;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.SLOTS)
    private int slotsTimeoutMinutes = 3;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING_SET, category = ControllerConfigurable.Category.SLOTS)
    private String[] slotsEmoji = {"🍓","🍍","🍊","🍋","🍇","🍉","🍌","🍒","🍎"};

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.BOOST)
    private int boostEventPoints = 1000;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.BOOST)
    private int percentChanceBoostHappensOnHour = 15;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG, category = ControllerConfigurable.Category.BOOST)
    private String boostStartImg = "https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG, category = ControllerConfigurable.Category.BOOST)
    private String boostEndImg = "https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING_SET, category = ControllerConfigurable.Category.BOOST)
    private String[] boostWords = {"boost"};

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.TRIVIAS)
    private int mapEventPoints = 100;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.TRIVIAS)
    private int triviaEventPoints = 100;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.TRIVIAS)
    private int quoteTriviaEventPoints = 100;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.TRIVIAS)
    private int nsfwQuoteTriviaEventPoints = 100;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.INT, category = ControllerConfigurable.Category.THIS_OR_THAT)
    private int thisOrThatPoints = 500;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG, category = ControllerConfigurable.Category.THIS_OR_THAT)
    private String thisOrThatImage = "https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING, category = ControllerConfigurable.Category.THIS_OR_THAT)
    private String thisOrThatName = "Checkout or Trampled";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING, category = ControllerConfigurable.Category.THIS_OR_THAT)
    private String thisOrThatGiveEmoji = "\uD83D\uDED2";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING, category = ControllerConfigurable.Category.THIS_OR_THAT)
    private String thisOrThatTakeEmoji = "\uD83D\uDEA7";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING_SET, category = ControllerConfigurable.Category.COLORS_FOOTERS)
    private String[] monthlyColors = {"#599111"};

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING_SET, category = ControllerConfigurable.Category.COLORS_FOOTERS)
    private String[] embedFooters = {"powered by 777"};

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.LONG, category = ControllerConfigurable.Category.CHANNEL_IDS)
    private long sfwSavedMemesChannelId = 0;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.LONG, category = ControllerConfigurable.Category.CHANNEL_IDS)
    private long nsfwSavedMemesChannelId = 0;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.LONG, category = ControllerConfigurable.Category.CHANNEL_IDS)
    private long memberCountVoiceChannelId = 0;

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING, category = ControllerConfigurable.Category.BOTS)
    private String antoniaNickname = "Antonia";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG, category = ControllerConfigurable.Category.BOTS)
    private String antoniaAvatar = "https://cdn.discordapp.com/attachments/794506942906761226/1096712794457526342/antonia-april-2023.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING, category = ControllerConfigurable.Category.BOTS)
    private String behradNickname = "Behrad";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG, category = ControllerConfigurable.Category.BOTS)
    private String behradAvatar = "https://cdn.discordapp.com/attachments/794506942906761226/1096715373245632558/behrad-april-2023.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING, category = ControllerConfigurable.Category.BOTS)
    private String johnNickname = "John";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG, category = ControllerConfigurable.Category.BOTS)
    private String johnAvatar = "https://cdn.discordapp.com/attachments/794506942906761226/1096715500895076412/john-april-2023.png";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.STRING, category = ControllerConfigurable.Category.BOTS)
    private String keanuNickname = "Keanu";

    @Column
    @ControllerConfigurable(type = ControllerConfigurable.Type.IMG, category = ControllerConfigurable.Category.BOTS)
    private String keanuAvatar = "https://cdn.discordapp.com/attachments/794506942906761226/1096715831330754560/keanu-april-2023.png";

}
