package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.Rank;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.google.common.collect.ImmutableSet;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SwampyCommand extends BaseSwampy implements PhilMarker {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // soft point bans
    private static final Pattern NO_NO_WORDS = Pattern.compile("\\b(nut|nice|simp|rep|daddy)\\b", Pattern.CASE_INSENSITIVE);

    // emoji
    public static final String[] LEADERBOARD_MEDALS = {
            "\uD83E\uDD47", "\uD83E\uDD48", "\uD83E\uDD49",
            "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40"
    };
    public static final String SLOT_MACHINE = "\uD83C\uDFB0";
    public static final Set<String> PLAIN_SLOTS = ImmutableSet.of(
            "\uD83E\uDD5D", "\uD83C\uDF53", "\uD83C\uDF4B", "\uD83E\uDD6D", "\uD83C\uDF51",
            "\uD83C\uDF48", "\uD83C\uDF4A", "\uD83C\uDF4D", "\uD83C\uDF50", "\uD83C\uDF47"
    );
    public static final Set<String> SPOOKY_SLOTS = ImmutableSet.of(
            "\uD83C\uDF83", "\uD83D\uDC7B", "\uD83D\uDC80", "\uD83C\uDF42", "\uD83C\uDF15",
            "\uD83E\uDDDB", "\uD83E\uDDDF", "\uD83D\uDD77️", "\uD83E\uDD87", "\uD83C\uDF6C"
    );
    public static final Set<String> TURKEY_SLOTS = ImmutableSet.of(
            "\uD83E\uDD83", "\uD83C\uDF57", "\uD83E\uDD54", "\uD83C\uDF60", "\uD83E\uDD24",
            "\uD83D\uDC6A", "\uD83E\uDD55", "\uD83C\uDF3D", "\uD83E\uDD67", "\uD83C\uDFC8"
    );
    public static final Set<String> CHRISTMAS_SLOTS = ImmutableSet.of(
            "\uD83C\uDF84", "\uD83C\uDF85", "\uD83E\uDD36", "\uD83C\uDF81", "\uD83D\uDD4E",
            "\uD83C\uDF80", "\uD83E\uDDE8", "\uD83E\uDDC8", "\uD83C\uDF6A", "\uD83E\uDD5B"
    );

    // volatile state
    private volatile boolean awaitingResetConfirmation = false;

    private final String modHelp;

    public SwampyCommand() {
        name = "swampy";
        aliases = new String[] {
                "swamp", "bastard", "spooky", "cursed", "gay", "aww", "moist", "moisten", "gobble", "sin", "simp", "simpy", "shrimp", "shrimpy", "shremp",
                "daddy", "punny", "dakota", "grapefruit", "stregg", "destiel", "foot", "oleo", "shack", "pit", "jolly", "shrantiago", "shrek", "billy",
                "deer", "hot"};
        help =
                "`!!swampy` aka... `" + Arrays.toString(aliases) + "` HELP:\n" +
                "`!!swampy rank` show your swampy rank\n" +
                "`!!swampy leaderboard bastard` show the 18+ leaderboard\n" +
                "`!!swampy leaderboard chaos` show the chaos children leaderboard\n" +
                "`!!swampy up @incogmeato` upvote a user for the swampys\n" +
                "`!!swampy down @incogmeato` downvote a user for the swampys\n" +
                "`!!swampy slots` Play slots. Winners for 2 out of 3 matches or 3 out of 3 matches.";
        modHelp = help + "\n\nMODS ONLY COMMANDS:\n" +
                "`!!swampy give 120 @incogmeato` give 120 points to incogmeato\n" +
                "`!!swampy take 120 @incogmeato` remove 120 points from incogmeato\n" +
                "`!!swampy set 120 @incogmeato` set incogmeato to 120 points\n" +
                "`!!swampy reset` reset everyone back to level 0";
    }

    @PostConstruct
    public void init() throws Exception {
        Rank.init();

        // seed swampy games config data
        Optional<SwampyGamesConfig> optionalConfig = this.swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID);
        if (!optionalConfig.isPresent()) {
            SwampyGamesConfig singleton = new SwampyGamesConfig();
            singleton.setId(SwampyGamesConfig.SINGLETON_ID);
            this.swampyGamesConfigRepository.save(singleton);
        }
    }

    @Override
    public void execute(CommandEvent event) {
        String args = event.getArgs();
        String msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith("!!") && isNotParticipating(event.getMember())) {
            event.replyError("Please get sorted into a house to participate");
            return;
        }

        if (isNotParticipating(event.getMember())) {
            return;
        }

        if (args.startsWith("help")) {
            if (hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
                event.replyInDm(Constants.simpleEmbed("Help", modHelp));
            } else {
                event.replyInDm(Constants.simpleEmbed("Help", help));
            }
        } else if (args.startsWith("rank")) {
            showRank(event);
        } else if (args.startsWith("up")) {
            upvote(event);
        } else if (args.startsWith("down")) {
            downvote(event);
        } else if (args.startsWith("give")) {
            give(event);
        } else if (args.startsWith("take")) {
            take(event);
        } else if (args.startsWith("set")) {
            set(event);
        } else if (args.startsWith("leaderboard")) {
            leaderboard(event);
        } else if (args.startsWith("slots")) {
            slots(event);
        } else if (args.startsWith("reset")) {
            if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
                event.replyError("You do not have permission to use this command");
                return;
            }

            if (!awaitingResetConfirmation) {
                event.reply(Constants.simpleEmbed("Reset The Games", "Are you sure you want to reset the Swampys? This will reset everyone's points. If you are sure, type `!!swampy reset confirm`"));
                awaitingResetConfirmation = true;
            } else {
                if (!args.startsWith("reset confirm")) {
                    event.reply(Constants.simpleEmbed("Abort Reset", "Swampy reset aborted. You must type `!!swampy reset` and then confirm it with `!!swampy reset confirm`."));
                    awaitingResetConfirmation = false;
                    return;
                }
                reset(event);
                awaitingResetConfirmation = false;
            }
        } else if (msgContent.startsWith("!!")) {
            event.replyError("Unrecognized command");
        } else {
            SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
            if (swampyGamesConfig == null) {
                return;
            }

            if (swampyGamesConfig.getBoostPhrase() != null && StringUtils.containsIgnoreCase(msgContent, swampyGamesConfig.getBoostPhrase())) {
                acceptedBoost(event.getMember());
                return;
            }

            if (swampyGamesConfig.getMapPhrase() != null && StringUtils.containsIgnoreCase(msgContent, swampyGamesConfig.getMapPhrase())) {
                acceptedMap(event.getMember());
                return;
            }

            if (swampyGamesConfig.getSwiperAwaiting() != null && StringUtils.containsIgnoreCase(msgContent, swampyGamesConfig.getNoSwipingPhrase())) {
                swampyGamesConfig.setSwiperSavior(event.getMember().getId());
                swampyGamesConfigRepository.save(swampyGamesConfig);
                return;
            }

            if (NO_NO_WORDS.matcher(msgContent).find()) {
                takePointsFromMember(swampyGamesConfig.getNoNoWordsPoints(), event.getMember());
                return;
            }

            Message message = event.getMessage();

            DiscordUser discordUser = getDiscordUserByMember(event.getMember());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextMsgBonusTime = discordUser.getLastMessageBonus().plus(swampyGamesConfig.getPictureMsgTimeoutMinutes(), ChronoUnit.MINUTES);

            boolean bonus = CollectionUtils.isNotEmpty(message.getAttachments()) || CollectionUtils.isNotEmpty(message.getEmbeds());

            long pointsToGive = swampyGamesConfig.getNormalMsgPoints();
            if ("bot-space".equals(event.getChannel().getName()) || "sim-games".equals(event.getChannel().getName())) {
                pointsToGive = 1;
            } else if (bonus && now.isAfter(nextMsgBonusTime)) {
                pointsToGive = swampyGamesConfig.getPictureMsgPoints();
                discordUser.setLastMessageBonus(now);
            }

            givePointsToMember(pointsToGive, event.getMember(), discordUser);
        }
    }

    public void voiceJoined(Member member) {
        if (isNotParticipating(member)) {
            return;
        }

        DiscordUser user = getDiscordUserByMember(member);
        user.setVoiceJoined(LocalDateTime.now());
        discordUserRepository.save(user);
    }

    public void voiceLeft(Member member) {
        if (isNotParticipating(member)) {
            return;
        }

        DiscordUser user = getDiscordUserByMember(member);
        LocalDateTime timeTheyJoinedVoice = user.getVoiceJoined();

        if (timeTheyJoinedVoice == null) {
            return;
        }

        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        if (swampyGamesConfig == null) {
            return;
        }

        long minutes = ChronoUnit.MINUTES.between(timeTheyJoinedVoice, LocalDateTime.now());
        long points = minutes * swampyGamesConfig.getVcPointsPerMinute();
        user.setVoiceJoined(null);
        discordUserRepository.save(user);

        givePointsToMember(points, member);
    }

    public void emote(GuildMessageReactionAddEvent event) {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        if (swampyGamesConfig == null) {
            return;
        }

        givePointsToMember(swampyGamesConfig.getReactionPoints(), event.getMember());
        long messageId = event.getMessageIdLong();
        long reactionGiverId = event.getMember().getIdLong();
        event.getChannel().retrieveMessageById(messageId).queue(msg -> {
            if (msg != null && msg.getMember() != null && msg.getMember().getIdLong() != reactionGiverId) {
                givePointsToMember(swampyGamesConfig.getReactionPoints(), msg.getMember());
            }
        });
    }

    public void removeFromGames(String id) {
        if (discordUserRepository.existsById(id)) {
            discordUserRepository.deleteById(id);
        }
    }

    private void acceptedBoost(Member member) {
        DiscordUser discordUser = getDiscordUserByMember(member);
        discordUser.setAcceptedBoost(LocalDateTime.now());
        discordUserRepository.save(discordUser);
    }

    private void acceptedMap(Member member) {
        DiscordUser discordUser = getDiscordUserByMember(member);
        discordUser.setAcceptedMapTrivia(LocalDateTime.now());
        discordUserRepository.save(discordUser);
    }

    private void slots(CommandEvent event) {
        Member member = event.getMember();
        DiscordUser discordUser = getDiscordUserByMember(member);

        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        if (swampyGamesConfig == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextSlotsTime = discordUser.getLastSlots().plus(swampyGamesConfig.getSlotsTimeoutMinutes(), ChronoUnit.MINUTES);
        if (now.isBefore(nextSlotsTime)) {
            Duration duration = Duration.between(now, nextSlotsTime);

            if (duration.getSeconds() < 60) {
                event.replyError("You must wait " + (duration.getSeconds() + 1) + " seconds before playing slots again");
            } else {
                event.replyError("You must wait " + (duration.toMinutes() + 1) + " minutes before playing slots again");
            }
            return;
        }

        discordUser.setLastSlots(now);

        String one = Constants.pickRandom(CHRISTMAS_SLOTS);
        String two = Constants.pickRandom(CHRISTMAS_SLOTS);
        String three = Constants.pickRandom(CHRISTMAS_SLOTS);

        if (one.equalsIgnoreCase(two) && two.equalsIgnoreCase(three)) {
            givePointsToMember(swampyGamesConfig.getSlotsWinPoints(), member, discordUser);
            event.reply(Constants.simpleEmbed(SLOT_MACHINE + " WINNER WINNER!! " + SLOT_MACHINE, String.format("%s\n%s%s%s \nYou won "
                            + swampyGamesConfig.getSlotsWinPoints() + " points!", member.getAsMention(), one, two, three)));
        } else if (one.equalsIgnoreCase(two) || one.equalsIgnoreCase(three) || two.equalsIgnoreCase(three)) {
            givePointsToMember(swampyGamesConfig.getSlotsTwoOfThreePoints(), member, discordUser);
            event.reply(Constants.simpleEmbed(SLOT_MACHINE + " CLOSE ENOUGH! " + SLOT_MACHINE, String.format("%s\n%s%s%s \nYou got 2 out of 3! You won "
                            + swampyGamesConfig.getSlotsTwoOfThreePoints() + " points!", member.getAsMention(), one, two, three)));
        } else {
            event.reply(Constants.simpleEmbed(SLOT_MACHINE + " Better luck next time! " + SLOT_MACHINE, String.format("%s\n%s%s%s",
                    member.getAsMention(), one, two, three)));
            discordUserRepository.save(discordUser);
        }
    }

    private void showRank(CommandEvent event) {
        Member member = event.getMember();
        if (CollectionUtils.size(event.getMessage().getMentionedMembers()) == 1) {
            member = event.getMessage().getMentionedMembers().get(0);
        }

        if (isNotParticipating(member)) {
            event.reply(Constants.simpleEmbed("Your Rank", "Is not participating in the games"));
            return;
        }

        DiscordUser user = getDiscordUserByMember(member);
        Role role = assignRolesIfNeeded(member, user).getLeft();

        Rank[] allRanks = Rank.getAllRanks();
        Rank rank = Rank.byXp(user.getXp());
        Rank nextRank = (rank.ordinal() >= allRanks.length - 1) ? rank : allRanks[rank.ordinal() + 1];

        String description = rank.getRankUpMessage().replace("<name>", member.getAsMention()).replace("<rolename>", rank.getRoleName()) +
                "\n\nYou have " + NumberFormat.getIntegerInstance().format(user.getXp()) + " total points.\n\n" +
                "The next level is level " +
                (rank == nextRank
                        ? " LOL NVM YOU'RE THE TOP LEVEL."
                        : nextRank.getLevel() + ": " + nextRank.getRoleName() + ".") +
                "\n You have " + (rank == nextRank
                ? "LOL NVM YOU'RE THE TOP LEVEL"
                : NumberFormat.getIntegerInstance().format((nextRank.getLevel() * Rank.LVL_MULTIPLIER) - user.getXp()))
                + " points to go.\n\nBest of Luck in the Swampys!";

        MessageEmbed messageEmbed = Constants.simpleEmbed("Level " + rank.getLevel() + ": " + rank.getRoleName(),
                description,
                rank.getRankUpImage(),
                role.getColor());

        event.reply(messageEmbed);
    }

    private void leaderboard(CommandEvent event) {
        List<DiscordUser> swampyUsers = discordUserRepository.findAll();
        long dryBastards = 0;
        long dryCinnamons = 0;
        long swampyBastards = 0;
        long swampyCinnamons = 0;

        for (DiscordUser swampyUser : swampyUsers) {
            Member memberById = event.getGuild().getMemberById(swampyUser.getId());

            if (memberById != null) {
                if (hasRole(memberById, Constants.DRY_BASTARDS_ROLE)) {
                    dryBastards += swampyUser.getXp();
                }
                if (hasRole(memberById, Constants.DRY_CINNAMON_ROLE)) {
                    dryCinnamons += swampyUser.getXp();
                }
                if (hasRole(memberById, Constants.SWAMPY_BASTARDS_ROLE)) {
                    swampyBastards += swampyUser.getXp();
                }
                if (hasRole(memberById, Constants.SWAMPY_CINNAMON_ROLE)) {
                    swampyCinnamons += swampyUser.getXp();
                }
            }
        }

        String title;
        String thumbnail;
        if (dryBastards >= dryCinnamons && dryBastards >= swampyBastards && dryBastards >= swampyCinnamons) {
            title = "Dry Bastards Are Leading";
            thumbnail = Constants.DRY_BASTARDS_CREST;
        } else if (dryCinnamons >= dryBastards && dryCinnamons >= swampyBastards && dryCinnamons >= swampyCinnamons) {
            title = "Dry Cinnamon Rolls Are Leading";
            thumbnail = Constants.DRY_CINNAMON_CREST;
        } else if (swampyBastards >= dryBastards && swampyBastards >= dryCinnamons && swampyBastards >= swampyCinnamons) {
            title = "Swampy Bastards Are Leading";
            thumbnail = Constants.SWAMPY_BASTARDS_CREST;
        } else {
            title = "Swampy Cinnamon Rolls Are Leading";
            thumbnail = Constants.SWAMPY_CINNAMON_CREST;
        }

        MessageEmbed message = new EmbedBuilder()
                .addField("Dry Bastards", NumberFormat.getIntegerInstance().format(dryBastards), true)
                .addField("Dry Cinnamon Rolls", NumberFormat.getIntegerInstance().format(dryCinnamons), true)
                .addBlankField(true)
                .addField("Swampy Bastards", NumberFormat.getIntegerInstance().format(swampyBastards), true)
                .addField("Swampy Cinnamon Rolls", NumberFormat.getIntegerInstance().format(swampyCinnamons), true)
                .addBlankField(true)
                .setTitle(title)
                .setThumbnail(thumbnail)
                .setColor(Constants.COLOR_OF_THE_MONTH)
                .build();

        event.reply(message);
    }

    private void upvote(CommandEvent event) {
        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.size(mentionedMembers) != 1) {
            event.replyError("Please mention one user to upvote. Example `!!swampy up @incogmeato`");
            return;
        }

        Member mentionedMember = mentionedMembers.get(0);
        if (isNotParticipating(mentionedMember)) {
            event.replyError(mentionedMember.getEffectiveName() + " is not participating in the swampys");
            return;
        }

        if (event.getMember().getId().equalsIgnoreCase(mentionedMember.getId())) {
            event.replyError("You can't upvote yourself");
            return;
        }

        DiscordUser discordUser = getDiscordUserByMember(event.getMember());

        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        if (swampyGamesConfig == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextVoteTime = discordUser.getLastVote().plus(swampyGamesConfig.getUpvoteTimeoutMinutes(), ChronoUnit.MINUTES);
        if (now.isBefore(nextVoteTime)) {
            Duration duration = Duration.between(now, nextVoteTime);

            if (duration.getSeconds() < 60) {
                event.replyError("You must wait " + (duration.getSeconds() + 1) + " seconds before up/down-voting again");
            } else {
                event.replyError("You must wait " + (duration.toMinutes() + 1) + " minutes before up/down-voting again");
            }
            return;
        }
        discordUser.setLastVote(now);
        discordUserRepository.save(discordUser);

        givePointsToMember(swampyGamesConfig.getUpvotePointsToUpvotee(), mentionedMember);
        givePointsToMember(swampyGamesConfig.getUpvotePointsToUpvoter(), event.getMember());

        event.replySuccess("Successfully upvoted " + mentionedMember.getEffectiveName());
    }

    private void downvote(CommandEvent event) {
        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.size(mentionedMembers) != 1) {
            event.replyError("Please mention one user to downvote. Example `!!swampy down @incogmeato`");
            return;
        }

        Member mentionedMember = mentionedMembers.get(0);
        if (isNotParticipating(mentionedMember)) {
            event.replyError(mentionedMember.getEffectiveName() + " is not participating in the swampys");
            return;
        }

        if (event.getMember().getId().equalsIgnoreCase(mentionedMember.getId())) {
            event.replyError("You can't downvote yourself");
            return;
        }

        DiscordUser discordUser = getDiscordUserByMember(event.getMember());

        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        if (swampyGamesConfig == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextVoteTime = discordUser.getLastVote().plus(swampyGamesConfig.getDownvoteTimeoutMinutes(), ChronoUnit.MINUTES);
        if (now.isBefore(nextVoteTime)) {
            Duration duration = Duration.between(now, nextVoteTime);

            if (duration.getSeconds() < 60) {
                event.replyError("You must wait " + (duration.getSeconds() + 1) + " seconds before up/down-voting again");
            } else {
                event.replyError("You must wait " + (duration.toMinutes() + 1) + " minutes before up/down-voting again");
            }
            return;
        }
        discordUser.setLastVote(now);
        discordUserRepository.save(discordUser);

        takePointsFromMember(swampyGamesConfig.getDownvotePointsFromDownvotee(), mentionedMember);
        givePointsToMember(swampyGamesConfig.getDownvotePointsToDownvoter(), event.getMember());

        event.replySuccess("Successfully downvoted " + mentionedMember.getEffectiveName());
    }

    private void give(CommandEvent event) {
        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        String[] split = event.getArgs().split("\\s+");
        if (split.length != 3) {
            event.replyError("Badly formatted command. Example `!!swampy give 100 @incogmeato`");
            return;
        }

        long pointsToGive;
        try {
            pointsToGive = Long.parseLong(split[1]);
        } catch (NumberFormatException e) {
            event.replyError("Failed to parse the number you provided.");
            return;
        }

        if (pointsToGive < 0) {
            event.replyError("Please provide a positive number");
            return;
        }

        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.size(mentionedMembers) != 1) {
            event.replyError("Please specify only one user. Example `!!swampy give 100 @incogmeato`");
            return;
        }

        Member mentionedMember = mentionedMembers.get(0);
        if (isNotParticipating(mentionedMember)) {
            event.replyError(mentionedMember.getEffectiveName() + " is not participating in the swampys");
            return;
        }

        givePointsToMember(pointsToGive, mentionedMember);

        event.replyFormatted("Added %s xp to %s", NumberFormat.getIntegerInstance().format(pointsToGive), mentionedMember.getEffectiveName());
    }

    private void take(CommandEvent event) {
        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        String[] split = event.getArgs().split("\\s+");
        if (split.length != 3) {
            event.replyError("Badly formatted command. Example `!!swampy take 100 @incogmeato`");
            return;
        }

        long pointsToTake;
        try {
            pointsToTake = Long.parseLong(split[1]);
        } catch (NumberFormatException e) {
            event.replyError("Failed to parse the number you provided.");
            return;
        }

        if (pointsToTake < 0) {
            event.replyError("Please provide a positive number");
            return;
        }

        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.size(mentionedMembers) != 1) {
            event.replyError("Please specify only one user. Example `!!swampy take 100 @incogmeato`");
            return;
        }

        Member mentionedMember = mentionedMembers.get(0);
        if (isNotParticipating(mentionedMember)) {
            event.replyError(mentionedMember.getEffectiveName() + " is not participating in the swampy games");
            return;
        }

        takePointsFromMember(pointsToTake, mentionedMember);

        event.replyFormatted("Removed %s xp from %s", NumberFormat.getIntegerInstance().format(pointsToTake), mentionedMember.getEffectiveName());
    }

    private void set(CommandEvent event) {
        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        String[] split = event.getArgs().split("\\s+");
        if (split.length != 3) {
            event.replyError("Badly formatted command. Example `!!swampy set 100 @incogmeato`");
            return;
        }

        long pointsToSet;
        try {
            pointsToSet = Long.parseLong(split[1]);
        } catch (NumberFormatException e) {
            event.replyError("Failed to parse the number you provided.");
            return;
        }

        if (pointsToSet < 0) {
            event.replyError("Please provide a positive number");
            return;
        }

        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.size(mentionedMembers) != 1) {
            event.replyError("Please specify only one user. Example `!!swampy set 100 @incogmeato`");
            return;
        }

        Member mentionedMember = mentionedMembers.get(0);
        if (isNotParticipating(mentionedMember)) {
            event.replyError(mentionedMember.getEffectiveName() + " is not participating in the swampys");
            return;
        }

        DiscordUser user = getDiscordUserByMember(mentionedMember);
        user.setXp(pointsToSet);
        user = discordUserRepository.save(user);
        assignRolesIfNeeded(mentionedMember, user);

        event.replyFormatted("Set xp to %s for %s", NumberFormat.getIntegerInstance().format(pointsToSet), mentionedMember.getEffectiveName());
    }

    private void reset(CommandEvent event) {
        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        Optional<SwampyGamesConfig> optionalConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID);
        if (optionalConfig.isPresent()) {
            SwampyGamesConfig swampyGamesConfig = optionalConfig.get();
            swampyGamesConfig.setMostRecentTaxes(0);
            swampyGamesConfigRepository.save(swampyGamesConfig);
        }

        event.reply("Resetting, please wait...");
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (DiscordUser discordUser : discordUserRepository.findAll()) {
            discordUser.setXp(0);
            discordUser.setSwiperParticipations(0);
            discordUser.setBoostParticipations(0);
            discordUser.setScooterAnkleParticipant(false);
            discordUser.setAdventCounter(0);
            discordUser = discordUserRepository.save(discordUser);

            try {
                Member memberById = event.getGuild().getMemberById(discordUser.getId());

                if (memberById == null) {
                    event.replyError("Failed to reset roles for user with discord id: <@!" + discordUser.getId() + '>');
                }

                futures.add(assignRolesIfNeeded(memberById, discordUser).getRight());
            } catch (Exception e) {
                logger.error("Failed to reset roles for user with [id={}]", discordUser.getId(), e);
                event.replyError("Failed to reset roles for user with discord id: <@!" + discordUser.getId() + '>');
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> event.replySuccess("Reset the Swampys"));
    }

}
