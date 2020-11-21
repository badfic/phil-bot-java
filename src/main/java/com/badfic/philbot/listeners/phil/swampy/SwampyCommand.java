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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
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

    // message/vc/emote points
    private static final long NORMAL_MSG_POINTS = 5;
    private static final long CURSED_MSG_POINTS = 10;
    private static final Set<String> CURSED_MSG_CHANNELS = ImmutableSet.of(
            "cursed-swamp",
            "nate-heywoods-simp-hour",
            "thirsty-legends",
            "gay-receipts",
            "the-swampys"
    );
    private static final long PICTURE_MSG_POINTS = 150;
    private static final long CURSED_PICTURE_MSG_POINTS = 250;
    private static final long REACTION_POINTS = 7;
    private static final long VOICE_CHAT_POINTS_PER_MINUTE = 5;
    private static final int NO_NO_WORDS_TAKE_POINTS = 100;

    // upvote/downvote points
    private static final long UPVOTE_TIMEOUT_MINUTES = 1;
    private static final long DOWNVOTE_TIMEOUT_MINUTES = 1;
    private static final long UPVOTE_POINTS_TO_UPVOTEE = 500;
    private static final long UPVOTE_POINTS_TO_UPVOTER = 125;
    private static final long DOWNVOTE_POINTS_FROM_DOWNVOTEE = 100;
    private static final long DOWNVOTE_POINTS_TO_DOWNVOTER = 50;

    // slots
    public static final long SLOTS_WIN_POINTS = 10_000;
    public static final long SLOTS_TWO_OUT_OF_THREE_POINTS = 50;

    // Timeouts
    private static final long PICTURE_MSG_BONUS_TIMEOUT_MINUTES = 3;
    private static final long SLOTS_TIMEOUT_MINUTES = 3;

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

    // volatile state
    private volatile boolean awaitingResetConfirmation = false;

    private final String modHelp;

    public SwampyCommand() {
        name = "swampy";
        aliases = new String[] {"bastard", "spooky", "cursed", "gay", "aww", "moist", "moisten", "gobble", "sin", "simpy", "daddy", "punny"};
        help =
                "`!!swampy` aka... `bastard, spooky, cursed, gay, aww, moist, moisten, gobble, sin, simpy, daddy, punny` HELP:\n" +
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
    public void init() {
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
        if (isNotParticipating(event.getMember())) {
            return;
        }

        String args = event.getArgs();
        String msgContent = event.getMessage().getContentRaw();

        if (args.startsWith("help")) {
            if (hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
                event.replyInDm(simpleEmbed("Help", modHelp));
            } else {
                event.replyInDm(simpleEmbed("Help", help));
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
                event.reply(simpleEmbed("Reset The Games", "Are you sure you want to reset the Swampys? This will reset everyone's points. If you are sure, type `!!swampy reset confirm`"));
                awaitingResetConfirmation = true;
            } else {
                if (!args.startsWith("reset confirm")) {
                    event.reply(simpleEmbed("Abort Reset", "Swampy reset aborted. You must type `!!swampy reset` and then confirm it with `!!swampy reset confirm`."));
                    awaitingResetConfirmation = false;
                    return;
                }
                reset(event);
                awaitingResetConfirmation = false;
            }
        } else if (msgContent.startsWith("!!")) {
            event.replyError("Unrecognized command");
        } else {
            if (Constants.SWAMPYS_CHANNEL.equals(event.getChannel().getName())) {
                Optional<SwampyGamesConfig> optionalConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID);
                if (!optionalConfig.isPresent()) {
                    return;
                }
                SwampyGamesConfig swampyGamesConfig = optionalConfig.get();

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
            }

            Message message = event.getMessage();

            DiscordUser discordUser = getDiscordUserByMember(event.getMember());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextMsgBonusTime = discordUser.getLastMessageBonus().plus(PICTURE_MSG_BONUS_TIMEOUT_MINUTES, ChronoUnit.MINUTES);

            boolean bonus = CollectionUtils.isNotEmpty(message.getAttachments())
                    || CollectionUtils.isNotEmpty(message.getEmbeds())
                    || CollectionUtils.isNotEmpty(message.getEmotes());

            long pointsToGive = NORMAL_MSG_POINTS;
            if ("bot-space".equals(event.getChannel().getName()) || "sim-games".equals(event.getChannel().getName())) {
                pointsToGive = 1;
            } else if (CURSED_MSG_CHANNELS.contains(event.getChannel().getName())) {
                pointsToGive = CURSED_MSG_POINTS;

                if (bonus && now.isAfter(nextMsgBonusTime)) {
                    if ("cursed-swamp".equalsIgnoreCase(event.getChannel().getName()) || "valigaytion".equalsIgnoreCase(event.getChannel().getName())) {
                        pointsToGive = CURSED_PICTURE_MSG_POINTS;
                    } else {
                        pointsToGive = PICTURE_MSG_POINTS;
                    }
                    discordUser.setLastMessageBonus(now);
                }
            } else if (bonus && now.isAfter(nextMsgBonusTime)) {
                pointsToGive = PICTURE_MSG_POINTS;
                discordUser.setLastMessageBonus(now);
            }

            if (NO_NO_WORDS.matcher(msgContent).find()) {
                takePointsFromMember(NO_NO_WORDS_TAKE_POINTS, event.getMember());
                return;
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

        long minutes = ChronoUnit.MINUTES.between(timeTheyJoinedVoice, LocalDateTime.now());
        long points = minutes * VOICE_CHAT_POINTS_PER_MINUTE;
        user.setVoiceJoined(null);
        discordUserRepository.save(user);

        givePointsToMember(points, member);
    }

    public void emote(GuildMessageReactionAddEvent event) {
        givePointsToMember(REACTION_POINTS, event.getMember());
        long messageId = event.getMessageIdLong();
        long reactionGiverId = event.getMember().getIdLong();
        event.getChannel().retrieveMessageById(messageId).queue(msg -> {
            if (msg != null && msg.getMember() != null && msg.getMember().getIdLong() != reactionGiverId) {
                givePointsToMember(REACTION_POINTS, msg.getMember());
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

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextSlotsTime = discordUser.getLastSlots().plus(SLOTS_TIMEOUT_MINUTES, ChronoUnit.MINUTES);
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

        String one = Constants.pickRandom(TURKEY_SLOTS);
        String two = Constants.pickRandom(TURKEY_SLOTS);
        String three = Constants.pickRandom(TURKEY_SLOTS);

        if (one.equalsIgnoreCase(two) && two.equalsIgnoreCase(three)) {
            givePointsToMember(SLOTS_WIN_POINTS, member, discordUser);
            event.reply(simpleEmbed(SLOT_MACHINE + " WINNER WINNER!! " + SLOT_MACHINE, String.format("%s\n%s%s%s \nYou won " + SLOTS_WIN_POINTS + " points!",
                    member.getAsMention(), one, two, three)));
        } else if (one.equalsIgnoreCase(two) || one.equalsIgnoreCase(three) || two.equalsIgnoreCase(three)) {
            givePointsToMember(SLOTS_TWO_OUT_OF_THREE_POINTS, member, discordUser);
            event.reply(simpleEmbed(SLOT_MACHINE + " CLOSE ENOUGH! " + SLOT_MACHINE, String.format("%s\n%s%s%s \nYou got 2 out of 3! You won " + SLOTS_TWO_OUT_OF_THREE_POINTS + " points!",
                    member.getAsMention(), one, two, three)));
        } else {
            event.reply(simpleEmbed(SLOT_MACHINE + " Better luck next time! " + SLOT_MACHINE, String.format("%s\n%s%s%s",
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
            event.reply(simpleEmbed("Your Rank", "You can't see rank because it appears you are a newbie or a bot"));
            return;
        }

        DiscordUser user = getDiscordUserByMember(member);
        Role role = assignRolesIfNeeded(member, user).getLeft();

        Rank[] allRanks = Rank.getAllRanks();
        Rank rank = Rank.byXp(user.getXp());
        Rank nextRank = (rank.ordinal() > allRanks.length - 1) ? rank : allRanks[rank.ordinal() + 1];

        MessageEmbed messageEmbed = new EmbedBuilder()
                .setImage(rank.getRankUpImage())
                .setTitle("Level " + rank.getLevel() + ": " + rank.getRoleName())
                .setColor(role.getColor())
                .setDescription(rank.getRankUpMessage().replace("<name>", member.getAsMention()).replace("<rolename>", rank.getRoleName()) +
                        "\n\nYou have " + NumberFormat.getIntegerInstance().format(user.getXp()) + " total points.\n\n" +
                        "The next level is level " +
                        (rank == nextRank
                                ? " LOL NVM YOU'RE THE TOP LEVEL."
                                : nextRank.getLevel() + ": " + nextRank.getRoleName() + ".") +
                        "\n You have " + NumberFormat.getIntegerInstance().format((nextRank.getLevel() * Rank.LVL_MULTIPLIER) - user.getXp()) + " points to go." +
                        "\n\nBest of Luck in the Swampys!")
                .build();

        event.reply(messageEmbed);
    }

    private void leaderboard(CommandEvent event) {
        String[] split = event.getArgs().split("\\s+");
        if (split.length != 2) {
            event.replyError("Badly formatted command. Example `!!swampy leaderboard bastard`, `!!swampy leaderboard chaos`, or `!!swampy leaderboard full`");
            return;
        }

        List<DiscordUser> swampyUsers = discordUserRepository.findAll();

        if ("full".equalsIgnoreCase(split[1])) {
            AtomicInteger place = new AtomicInteger(1);
            StringBuilder description = new StringBuilder();
            swampyUsers.stream()
                    .sorted((u1, u2) -> Long.compare(u2.getXp(), u1.getXp()))
                    .forEachOrdered(swampyUser -> {
                        Member memberById = event.getGuild().getMemberById(swampyUser.getId());

                        description.append(place.getAndIncrement());
                        if (memberById != null) {
                            description
                                    .append(": <@!")
                                    .append(swampyUser.getId())
                                    .append(">, name: \"")
                                    .append(memberById.getEffectiveName())
                                    .append("\", role: \"")
                                    .append(hasRole(memberById, Constants.EIGHTEEN_PLUS_ROLE) ? Constants.EIGHTEEN_PLUS_ROLE : Constants.CHAOS_CHILDREN_ROLE)
                                    .append("\", xp: ")
                                    .append(NumberFormat.getIntegerInstance().format(swampyUser.getXp()))
                                    .append('\n');
                        } else {
                            description
                                    .append(": <@!")
                                    .append(swampyUser.getId())
                                    .append(">, could not find user in member cache!, xp: ")
                                    .append(NumberFormat.getIntegerInstance().format(swampyUser.getXp()))
                                    .append('\n');
                        }
                    });

            event.getChannel().sendFile(description.toString().getBytes(), "leaderboard.txt").queue();
            return;
        }

        if ("swiper".equalsIgnoreCase(split[1])) {
            AtomicInteger place = new AtomicInteger(0);
            StringBuilder description = new StringBuilder();

            swampyUsers.stream()
                    .sorted((u1, u2) -> Long.compare(u2.getSwiperParticipations(), u1.getSwiperParticipations()))
                    .limit(10)
                    .forEachOrdered(swampyUser -> {
                        description.append(LEADERBOARD_MEDALS[place.getAndIncrement()])
                                .append(": <@!")
                                .append(swampyUser.getId())
                                .append("> - ")
                                .append(NumberFormat.getIntegerInstance().format(swampyUser.getSwiperParticipations()))
                                .append('\n');
                    });
            MessageEmbed messageEmbed = new EmbedBuilder()
                    .setTitle("Swiper Leaderboard")
                    .setDescription(description.toString())
                    .build();

            event.reply(messageEmbed);
            return;
        }

        if ("boost".equalsIgnoreCase(split[1])) {
            AtomicInteger place = new AtomicInteger(0);
            StringBuilder description = new StringBuilder();

            swampyUsers.stream()
                    .sorted((u1, u2) -> Long.compare(u2.getBoostParticipations(), u1.getBoostParticipations()))
                    .limit(10)
                    .forEachOrdered(swampyUser -> {
                        description.append(LEADERBOARD_MEDALS[place.getAndIncrement()])
                                .append(": <@!")
                                .append(swampyUser.getId())
                                .append("> - ")
                                .append(NumberFormat.getIntegerInstance().format(swampyUser.getBoostParticipations()))
                                .append('\n');
                    });
            MessageEmbed messageEmbed = new EmbedBuilder()
                    .setTitle("Boost Leaderboard")
                    .setDescription(description.toString())
                    .build();

            event.reply(messageEmbed);
            return;
        }

        AtomicInteger place = new AtomicInteger(0);
        StringBuilder description = new StringBuilder();
        swampyUsers.stream().filter(u -> {
            try {
                Member member = Objects.requireNonNull(event.getGuild().getMemberById(u.getId()));

                return hasRole(member, StringUtils.containsIgnoreCase(split[1], "bastard") ? Constants.EIGHTEEN_PLUS_ROLE : Constants.CHAOS_CHILDREN_ROLE);
            } catch (Exception e) {
                logger.error("Unable to lookup user [id={}] for leaderboard", u.getId(), e);
                return false;
            }
        }).sorted((u1, u2) -> Long.compare(u2.getXp(), u1.getXp())).limit(10).forEachOrdered(swampyUser -> {
            description.append(LEADERBOARD_MEDALS[place.getAndIncrement()])
                    .append(": <@!")
                    .append(swampyUser.getId())
                    .append("> - ")
                    .append(NumberFormat.getIntegerInstance().format(swampyUser.getXp()))
                    .append('\n');
        });

        MessageEmbed messageEmbed = new EmbedBuilder()
                .setTitle(StringUtils.containsIgnoreCase(split[1], "bastard") ? "Bastard Leaderboard" : "Chaos Leaderboard")
                .setDescription(description.toString())
                .build();

        event.reply(messageEmbed);
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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextVoteTime = discordUser.getLastVote().plus(UPVOTE_TIMEOUT_MINUTES, ChronoUnit.MINUTES);
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

        givePointsToMember(UPVOTE_POINTS_TO_UPVOTEE, mentionedMember);
        givePointsToMember(UPVOTE_POINTS_TO_UPVOTER, event.getMember());

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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextVoteTime = discordUser.getLastVote().plus(DOWNVOTE_TIMEOUT_MINUTES, ChronoUnit.MINUTES);
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

        takePointsFromMember(DOWNVOTE_POINTS_FROM_DOWNVOTEE, mentionedMember);
        givePointsToMember(DOWNVOTE_POINTS_TO_DOWNVOTER, event.getMember());

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

        event.reply("Resetting, please wait...");
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (DiscordUser discordUser : discordUserRepository.findAll()) {
            discordUser.setXp(0);
            discordUser.setSwiperParticipations(0);
            discordUser.setBoostParticipations(0);
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
