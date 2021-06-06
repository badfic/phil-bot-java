package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
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
public class SwampyCommand extends BaseSwampy {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // soft point bans
    private static final Pattern NO_NO_WORDS = Constants.compileWords("captain");

    // emoji
    public static final String[] LEADERBOARD_MEDALS = {
            "\uD83E\uDD47", "\uD83E\uDD48", "\uD83E\uDD49",
            "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40"
    };
    public static final String SLOT_MACHINE = "\uD83C\uDFB0";
    public static final Set<String> SLOTS_EMOJIS = ImmutableSet.of(
            "\uD83C\uDFF3️\u200D⚧️", "\uD83C\uDFF3️\u200D\uD83C\uDF08", "\uD83E\uDD0E",
            "\uD83D\uDE24", "\uD83D\uDE2D️", "\uD83D\uDCAF",
            "\uD83D\uDC9C", "\uD83D\uDC9A", "\uD83E\uDD0D"
    );

    // volatile state
    private volatile boolean awaitingResetConfirmation = false;

    private final String modHelp;

    public SwampyCommand() {
        name = "swampy";
        aliases = new String[] {
                "swamp", "bastard", "spooky", "cursed", "gay", "aww", "moist", "moisten", "gobble", "sin", "simp", "simpy", "shrimp", "shrimpy", "shremp",
                "daddy", "punny", "dakota", "grapefruit", "stregg", "destiel", "foot", "oleo", "shack", "pit", "jolly", "shrantiago", "shrek", "billy",
                "deer", "hot", "frosty", "glogg", "beam", "oof", "booty", "love", "chuck", "farm", "nut", "rat", "giraffe", "possum", "lena", "santiago",
                "riverdale", "matthew", "murder", "book", "salami", "spicy", "spice", "salt", "yeet", "wah", "crafty", "bingus", "snarky", "crisco",
                "mint", "spooder", "shark", "latte", "sahar", "obama", "selfcare", "nelly", "void", "baskin", "perry", "british", "duck", "shut", "waffle",
                "vamp", "dap", "dab", "wap", "butter", "mullet", "vax", "vaxx", "steve", "shronk", "oliver", "ophelia", "lisa", "rock", "buck", "bucky", "caw",
                "sucky", "cap", "lesbian", "bi", "ace", "aro", "aroace", "demi", "enby", "nb", "intersex", "pan", "questioning", "trans", "megan", "goat",
                "sam", "rise"};
        help =
                "`!!swampy` aka...\n" + Arrays.stream(aliases).sorted().collect(Collectors.joining(", ")) + "\nHELP:\n" +
                "`!!swampy rank` show your swampy rank\n" +
                "`!!swampy leaderboard bastard` show the 18+ leaderboard\n" +
                "`!!swampy leaderboard chaos` show the chaos children leaderboard\n" +
                "`!!swampy leaderboard [" + Arrays.stream(PointsStat.values()).map(Enum::name).collect(Collectors.joining(", ")) +
                        "]` show specific stat leaderboard\n" +
                "`!!swampy up @Santiago` upvote a user for the swampys\n" +
                "`!!swampy down @Santiago` downvote a user for the swampys\n" +
                "`!!swampy slots` Play slots. Winners for 2 out of 3 matches or 3 out of 3 matches.";
        modHelp = help + "\n\nMODS ONLY COMMANDS:\n" +
                "`!!swampy give 120 @Santiago` give 120 points to Santiago\n" +
                "`!!swampy take 120 @Santiago` remove 120 points from Santiago\n" +
                "`!!swampy reset` reset everyone back to level 0";
    }

    @PostConstruct
    public void init() throws Exception {
        Rank.init();

        // seed swampy games config data
        Optional<SwampyGamesConfig> optionalConfig = this.swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID);
        if (optionalConfig.isEmpty()) {
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
            event.replyError("Please ask a mod to check your roles to participate in the swampys");
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
                givePointsToMember(1, event.getMember(), PointsStat.SWIPER_PARTICIPATIONS);
                swampyGamesConfig.setSwiperSavior(event.getMember().getId());
                swampyGamesConfigRepository.save(swampyGamesConfig);
                return;
            }

            if (NO_NO_WORDS.matcher(msgContent).find()) {
                takePointsFromMember(swampyGamesConfig.getNoNoWordsPoints(), event.getMember(), PointsStat.NO_NO);
                return;
            }

            Message message = event.getMessage();

            DiscordUser discordUser = getDiscordUserByMember(event.getMember());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextMsgBonusTime = discordUser.getLastMessageBonus().plus(swampyGamesConfig.getPictureMsgTimeoutMinutes(), ChronoUnit.MINUTES);

            boolean bonus = CollectionUtils.isNotEmpty(message.getAttachments()) || CollectionUtils.isNotEmpty(message.getEmbeds());

            PointsStat pointsStat = PointsStat.MESSAGE;
            long pointsToGive = swampyGamesConfig.getNormalMsgPoints();
            if ("bot-space".equals(event.getChannel().getName()) || "sim-games".equals(event.getChannel().getName())) {
                pointsToGive = 1;
            } else if (bonus && now.isAfter(nextMsgBonusTime)) {
                pointsStat = PointsStat.PICTURE_MESSAGE;
                pointsToGive = swampyGamesConfig.getPictureMsgPoints();
                discordUser.setLastMessageBonus(now);
            }

            givePointsToMember(pointsToGive, event.getMember(), discordUser, pointsStat);
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

        givePointsToMember(points, member, PointsStat.VOICE_CHAT);
    }

    public void emote(GuildMessageReactionAddEvent event) {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        if (swampyGamesConfig == null) {
            return;
        }

        givePointsToMember(swampyGamesConfig.getReactionPoints(), event.getMember(), PointsStat.REACTOR_POINTS);
        long messageId = event.getMessageIdLong();
        long reactionGiverId = event.getMember().getIdLong();
        event.getChannel().retrieveMessageById(messageId).queue(msg -> {
            if (msg != null && msg.getMember() != null && msg.getMember().getIdLong() != reactionGiverId) {
                givePointsToMember(swampyGamesConfig.getReactionPoints(), msg.getMember(), PointsStat.REACTED_POINTS);
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

        double oddsClosEnough = ((1.0 / SLOTS_EMOJIS.size()) * (1.0 / SLOTS_EMOJIS.size())) * 100.0;
        double oddsWinnerWinner = ((1.0 / SLOTS_EMOJIS.size()) * (1.0 / SLOTS_EMOJIS.size()) * (1.0 / SLOTS_EMOJIS.size())) * 100.0;
        String footer = String.format("Odds of a WINNER WINNER: %.3f%%\nOdds of a CLOSE ENOUGH: %.3f%%", oddsWinnerWinner, oddsClosEnough);

        String one = Constants.pickRandom(SLOTS_EMOJIS);
        String two = Constants.pickRandom(SLOTS_EMOJIS);
        String three = Constants.pickRandom(SLOTS_EMOJIS);

        if (one.equals(two) && two.equals(three)) {
            givePointsToMember(swampyGamesConfig.getSlotsWinPoints(), member, discordUser, PointsStat.SLOTS_WINNER_WINNER);
            event.reply(Constants.simpleEmbed(SLOT_MACHINE + " WINNER WINNER!! " + SLOT_MACHINE, String.format("%s\n%s%s%s \nYou won "
                            + swampyGamesConfig.getSlotsWinPoints() + " points!", member.getAsMention(), one, two, three), null, footer));
        } else if (one.equals(two) || one.equals(three) || two.equals(three)) {
            givePointsToMember(swampyGamesConfig.getSlotsTwoOfThreePoints(), member, discordUser, PointsStat.SLOTS_CLOSE_ENOUGH);
            event.reply(Constants.simpleEmbed(SLOT_MACHINE + " CLOSE ENOUGH! " + SLOT_MACHINE, String.format("%s\n%s%s%s \nYou got 2 out of 3! You won "
                            + swampyGamesConfig.getSlotsTwoOfThreePoints() + " points!", member.getAsMention(), one, two, three), null, footer));
        } else {
            event.reply(Constants.simpleEmbed(SLOT_MACHINE + " Better luck next time! " + SLOT_MACHINE, String.format("%s\n%s%s%s",
                    member.getAsMention(), one, two, three), null, footer));
            givePointsToMember(1, member, discordUser, PointsStat.SLOTS_LOSSES);
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

        Optional<PointsStat> optionalPointsStat = Arrays.stream(PointsStat.values())
                .filter(stat -> split[1].trim().equalsIgnoreCase(stat.name()))
                .findAny();

        if (optionalPointsStat.isPresent()) {
            PointsStat stat = optionalPointsStat.get();
            StringBuilder description = new StringBuilder();

            swampyUsers.stream()
                    .sorted((u1, u2) -> Long.compare(stat.getter().applyAsLong(u2), stat.getter().applyAsLong(u1)))
                    .forEachOrdered(swampyUser -> {
                        description
                                .append("<@!")
                                .append(swampyUser.getId())
                                .append("> - ")
                                .append(NumberFormat.getIntegerInstance().format(stat.getter().applyAsLong(swampyUser)))
                                .append('\n');
                    });
            MessageEmbed messageEmbed = Constants.simpleEmbed(stat.name() + " Leaderboard", description.toString());

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
                honeybadgerReporter.reportError(e, "unable to lookup user for leaderboard: " + u.getId());
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

        MessageEmbed messageEmbed = Constants.simpleEmbed(StringUtils.containsIgnoreCase(split[1], "bastard") ? "Bastard Leaderboard" : "Chaos Leaderboard",
                description.toString());

        event.reply(messageEmbed);
    }

    private void upvote(CommandEvent event) {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        if (swampyGamesConfig == null) {
            return;
        }

        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.isEmpty(mentionedMembers)) {
            event.replyError("Please mention at least one user to upvote. Example `!!swampy up @Santiago`");
            return;
        }

        List<Member> eligibleMembers = mentionedMembers.stream().filter(mentionedMember -> {
            if (isNotParticipating(mentionedMember)) {
                event.replyError(mentionedMember.getEffectiveName() + " is not participating in the swampys");
                return false;
            }

            if (event.getMember().getId().equalsIgnoreCase(mentionedMember.getId())) {
                event.replyError("You can't upvote yourself");
                return false;
            }

            return true;
        }).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(eligibleMembers)) {
            event.replyError("Please mention at least one eligible user to upvote. Example `!!swampy up @Santiago`");
            return;
        }

        DiscordUser discordUser = getDiscordUserByMember(event.getMember());

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

        givePointsToMember(swampyGamesConfig.getUpvotePointsToUpvoter(), event.getMember(), PointsStat.UPVOTER);

        for (Member mentionedMember : eligibleMembers) {
            givePointsToMember(swampyGamesConfig.getUpvotePointsToUpvotee(), mentionedMember, PointsStat.UPVOTED);
            event.replySuccess("Successfully upvoted " + mentionedMember.getEffectiveName());
        }
    }

    private void downvote(CommandEvent event) {
        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.size(mentionedMembers) != 1) {
            event.replyError("Please mention one user to downvote. Example `!!swampy down @Santiago`");
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

        takePointsFromMember(swampyGamesConfig.getDownvotePointsFromDownvotee(), mentionedMember, PointsStat.DOWNVOTED);
        givePointsToMember(swampyGamesConfig.getDownvotePointsToDownvoter(), event.getMember(), PointsStat.DOWNVOTER);

        event.replySuccess("Successfully downvoted " + mentionedMember.getEffectiveName());
    }

    private void give(CommandEvent event) {
        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        String[] split = event.getArgs().split("\\s+");
        if (split.length != 3) {
            event.replyError("Badly formatted command. Example `!!swampy give 100 @Santiago`");
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
            event.replyError("Please specify only one user. Example `!!swampy give 100 @Santiago`");
            return;
        }

        Member mentionedMember = mentionedMembers.get(0);
        if (isNotParticipating(mentionedMember)) {
            event.replyError(mentionedMember.getEffectiveName() + " is not participating in the swampys");
            return;
        }

        givePointsToMember(pointsToGive, mentionedMember, PointsStat.MOD_GIVE);

        event.replyFormatted("Added %s xp to %s", NumberFormat.getIntegerInstance().format(pointsToGive), mentionedMember.getEffectiveName());
    }

    private void take(CommandEvent event) {
        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        String[] split = event.getArgs().split("\\s+");
        if (split.length != 3) {
            event.replyError("Badly formatted command. Example `!!swampy take 100 @Santiago`");
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
            event.replyError("Please specify only one user. Example `!!swampy take 100 @Santiago`");
            return;
        }

        Member mentionedMember = mentionedMembers.get(0);
        if (isNotParticipating(mentionedMember)) {
            event.replyError(mentionedMember.getEffectiveName() + " is not participating in the swampy games");
            return;
        }

        takePointsFromMember(pointsToTake, mentionedMember, PointsStat.MOD_TAKE);

        event.replyFormatted("Removed %s xp from %s", NumberFormat.getIntegerInstance().format(pointsToTake), mentionedMember.getEffectiveName());
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

            for (PointsStat stat : PointsStat.values()) {
                stat.setter().accept(discordUser, 0L);
            }

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
