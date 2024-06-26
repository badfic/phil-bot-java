package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.ModHelpAware;
import com.badfic.philbot.commands.Rank;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.hungersim.Player;
import com.badfic.philbot.data.hungersim.PlayerRepository;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SwampyCommand extends BaseBangCommand implements ModHelpAware {
    // soft point bans
    private static final Pattern NO_NO_WORDS = Constants.compileWords("snowflake|eternals|captain|witch|christmas|santa|grinch");

    // emoji
    public static final String[] LEADERBOARD_MEDALS = {
            "\uD83E\uDD47", "\uD83E\uDD48", "\uD83E\uDD49",
            "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40"
    };
    public static final String SLOT_MACHINE = "\uD83C\uDFB0";

    private volatile boolean awaitingResetConfirmation = false;

    @Getter
    private final String modHelp;
    private final PlayerRepository playerRepository;

    public SwampyCommand(PlayerRepository playerRepository) {
        name = "swampy";
        help = "NOTE: the \"help\", \"rank\", \"up\", \"down\", and \"slots\" swampy commands can now be used with ANY `!!` alias you want.\n\n" +
                "`!!swampy up @Santiago` upvote a user for the swampys\n" +
                "`!!swampy down @Santiago` downvote a user for the swampys\n\n" +
                "`!!swampy rank` show your swampy rank\n\n" +
                "`!!swampy slots` Play slots. Winners for 2 out of 3 matches or 3 out of 3 matches.\n\n" +
                "`!!swampy leaderboard bastard` show the 18+ leaderboard\n" +
                "`!!swampy leaderboard chaos` show the chaos children leaderboard\n\n" +
                "`!!swampy leaderboard [" + Arrays.stream(PointsStat.values()).map(Enum::name).collect(Collectors.joining(", ")) +
                        "]` show specific stat leaderboard\n";
        modHelp = help + "\n\nMODS ONLY COMMANDS:\n" +
                "`!!swampy give 120 @Santiago` give 120 points to Santiago\n" +
                "`!!swampy take 120 @Santiago` remove 120 points from Santiago\n" +
                "`!!swampy reset` reset everyone back to level 0";
        this.playerRepository = playerRepository;
    }

    @Override
    public void execute(CommandEvent event) {
        String args = event.getArgs();
        String msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith(Constants.PREFIX) && isNotParticipating(event.getMember())) {
            event.replyError("Please ask a mod to check your roles to participate in the swampys");
            return;
        }

        if (isNotParticipating(event.getMember())) {
            return;
        }

        if (StringUtils.startsWith(msgContent, Constants.PREFIX)) {
            if (StringUtils.startsWithIgnoreCase(args, "help")) {
                if (hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
                    event.replyInDm(Constants.simpleEmbed("Help", modHelp));
                } else {
                    event.replyInDm(Constants.simpleEmbed("Help", help));
                }
            } else if (StringUtils.startsWithIgnoreCase(args, "rank")) {
                showRank(event);
            } else if (StringUtils.startsWithIgnoreCase(args, "up")) {
                upvote(event);
            } else if (StringUtils.startsWithIgnoreCase(args, "down")) {
                downvote(event);
            } else if (StringUtils.startsWithIgnoreCase(args, "give")) {
                give(event);
            } else if (StringUtils.startsWithIgnoreCase(args, "take")) {
                take(event);
            } else if (StringUtils.startsWithIgnoreCase(args, "leaderboard")) {
                leaderboard(event);
            } else if (StringUtils.startsWithIgnoreCase(args, "slots")) {
                slots(event);
            } else if (StringUtils.startsWithIgnoreCase(args, "reset")) {
                if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
                    event.replyError("You do not have permission to use this command");
                    return;
                }

                if (!awaitingResetConfirmation) {
                    event.reply(Constants.simpleEmbed("Reset The Games", "Are you sure you want to reset the Swampys? This will reset everyone's points. If you are sure, type `!!swampy reset confirm`"));
                    awaitingResetConfirmation = true;
                } else {
                    if (!StringUtils.startsWithIgnoreCase(args, "reset confirm")) {
                        event.reply(Constants.simpleEmbed("Abort Reset", "Swampy reset aborted. You must type `!!swampy reset` and then confirm it with `!!swampy reset confirm`."));
                        awaitingResetConfirmation = false;
                        return;
                    }
                    reset(event);
                    awaitingResetConfirmation = false;
                }
            } else {
                event.replyError("Unrecognized command");
            }
        } else {
            SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

            if (NO_NO_WORDS.matcher(msgContent).find()) {
                takePointsFromMember(swampyGamesConfig.getNoNoWordsPoints(), event.getMember(), PointsStat.NO_NO);
            }

            Message message = event.getMessage();

            DiscordUser discordUser = getDiscordUserByMember(event.getMember());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextMsgBonusTime = discordUser.getLastMessageBonus().plus(swampyGamesConfig.getPictureMsgTimeoutMinutes(), ChronoUnit.MINUTES);

            boolean bonus = CollectionUtils.isNotEmpty(message.getAttachments()) || CollectionUtils.isNotEmpty(message.getEmbeds());

            PointsStat pointsStat = PointsStat.MESSAGE;
            long pointsToGive = swampyGamesConfig.getNormalMsgPoints();
            String eventChannelName = event.getChannel().getName();
            if ("bot-space".equals(eventChannelName) || "sim-games".equals(eventChannelName) || "pokemon".equals(eventChannelName)) {
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

        long minutes = ChronoUnit.MINUTES.between(timeTheyJoinedVoice, LocalDateTime.now());
        long points = minutes * swampyGamesConfig.getVcPointsPerMinute();
        user.setVoiceJoined(null);
        user.setLifetimeVoiceChatMinutes(user.getLifetimeVoiceChatMinutes() + minutes);
        discordUserRepository.save(user);

        givePointsToMember(points, member, PointsStat.VOICE_CHAT);
    }

    public void emote(MessageReactionAddEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) {
            return;
        }

        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        givePointsToMember(swampyGamesConfig.getReactionPoints(), event.getMember(), PointsStat.REACTOR_POINTS);
        long messageId = event.getMessageIdLong();
        long reactionGiverId = event.getMember().getIdLong();
        GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();
        channel.retrieveMessageById(messageId).queue(msg -> {
            if (msg != null && msg.getMember() != null && msg.getMember().getIdLong() != reactionGiverId) {
                givePointsToMember(swampyGamesConfig.getReactionPoints(), msg.getMember(), PointsStat.REACTED_POINTS);
            }
        });

        EmojiUnion reactionEmote = event.getReaction().getEmoji();
        long guildId = event.getGuild().getIdLong();

        MessageEmbed messageEmbed;
        if (reactionEmote.getType() == Emoji.Type.CUSTOM) {
            messageEmbed = Constants.simpleEmbedThumbnail("Reaction Event",
                    String.format("<@%d> reacted %s (see thumbnail) to\n%s\nhttps://discordapp.com/channels/%d/%d/%d\nat %s",
                            reactionGiverId, reactionEmote.asCustom().getName(), channel.getAsMention(), guildId, channel.getIdLong(), messageId,
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())),
                    reactionEmote.asCustom().getImageUrl());
        } else {
            messageEmbed = Constants.simpleEmbed("Reaction Event",
                    String.format("<@%d> reacted %s to\n%s\nhttps://discordapp.com/channels/%d/%d/%d\nat %s",
                            reactionGiverId, reactionEmote.asUnicode().getName(), channel.getAsMention(), guildId, channel.getIdLong(), messageId,
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())));
        }

        Constants.debugToModLogsChannel(event.getJDA(), messageEmbed);
    }

    public void removeFromGames(Guild guild, String id) {
        Role roleById = guild.getRoleById(id);

        if (roleById != null) {
            roleById.delete().queue();
        }

        Optional<Player> optionalPlayer = playerRepository.findByDiscordUser(id);
        optionalPlayer.ifPresent(playerRepository::delete);

        if (discordUserRepository.existsById(id)) {
            discordUserRepository.deleteById(id);
        }
    }

    public void acceptedBoost(Member member) {
        DiscordUser discordUser = getDiscordUserByMember(member);
        discordUser.setAcceptedBoost(LocalDateTime.now());
        discordUserRepository.save(discordUser);
    }

    public void acceptedMap(Member member) {
        DiscordUser discordUser = getDiscordUserByMember(member);
        discordUser.setAcceptedMapTrivia(LocalDateTime.now());
        discordUserRepository.save(discordUser);
    }

    private void slots(CommandEvent event) {
        Member member = event.getMember();
        DiscordUser discordUser = getDiscordUserByMember(member);

        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

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

        Set<String> slotsEmojis = Arrays.stream(swampyGamesConfig.getSlotsEmoji()).collect(Collectors.toSet());
        int size = slotsEmojis.size();

        double oddsClosEnough = (1.0 / size) * (1.0 / size) * 300.0;
        double oddsWinnerWinner = (1.0 / size) * (1.0 / size) * (1.0 / size) * 100.0;
        String footer = String.format("Odds of a WINNER WINNER: %.3f%%\nOdds of a CLOSE ENOUGH: %.3f%%", oddsWinnerWinner, oddsClosEnough);

        String one = Constants.pickRandom(slotsEmojis);
        String two = Constants.pickRandom(slotsEmojis);
        String three = Constants.pickRandom(slotsEmojis);

        if (one.equals(two) && two.equals(three)) {
            givePointsToMember(swampyGamesConfig.getSlotsWinPoints(), member, discordUser, PointsStat.SLOTS_WINNER_WINNER).thenRun(() -> {
                event.reply(Constants.simpleEmbed(SLOT_MACHINE + " WINNER WINNER!! " + SLOT_MACHINE, String.format("%s\n%s%s%s \nYou won "
                        + swampyGamesConfig.getSlotsWinPoints() + " points!", member.getAsMention(), one, two, three), null, footer));

                philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).stream().findAny().ifPresent(swampysChannel -> {
                    swampysChannel.sendMessageEmbeds(Constants.simpleEmbedThumbnail("SLOTS WINNER! " + one + one + one,
                            member.getAsMention()
                                    + " just won "
                                    + NumberFormat.getIntegerInstance().format(swampyGamesConfig.getSlotsWinPoints())
                                    + " points from swampy slots! You too can win, play over in #bot-space",
                            "https://cdn.discordapp.com/attachments/707453916882665552/864307474970443816/tenor.gif",
                            member.getEffectiveAvatarUrl())).queue();
                });
            });
        } else if (one.equals(two) || one.equals(three) || two.equals(three)) {
            givePointsToMember(swampyGamesConfig.getSlotsTwoOfThreePoints(), member, discordUser, PointsStat.SLOTS_CLOSE_ENOUGH).thenRun(() -> {
                event.reply(Constants.simpleEmbed(SLOT_MACHINE + " CLOSE ENOUGH! " + SLOT_MACHINE, String.format("%s\n%s%s%s \nYou got 2 out of 3! You won "
                        + swampyGamesConfig.getSlotsTwoOfThreePoints() + " points!", member.getAsMention(), one, two, three), null, footer));
            });
        } else {
            givePointsToMember(1, member, discordUser, PointsStat.SLOTS_LOSSES).thenRun(() -> {
                event.reply(Constants.simpleEmbed(SLOT_MACHINE + " Better luck next time! " + SLOT_MACHINE, String.format("%s\n%s%s%s",
                        member.getAsMention(), one, two, three), null, footer));
            });
        }
    }

    private void showRank(CommandEvent event) {
        Member member = event.getMember();
        if (CollectionUtils.size(event.getMessage().getMentions().getMembers()) == 1) {
            member = event.getMessage().getMentions().getMembers().getFirst();
        }

        if (isNotParticipating(member)) {
            event.reply(Constants.simpleEmbed("Your Rank", "Is not participating in the games"));
            return;
        }

        DiscordUser user = getDiscordUserByMember(member);

        List<Rank> allRanks = Rank.getAllRanks();
        Rank rank = Rank.byXp(user.getXp());
        Rank nextRank = (rank.getOrdinal() >= allRanks.size() - 1) ? rank : allRanks.get(rank.getOrdinal() + 1);

        String description = rank.getRankUpMessage().replace("<name>", member.getAsMention()).replace("<rolename>", rank.getRoleName()) +
                "\n\nYou have " + NumberFormat.getIntegerInstance().format(user.getXp()) + " total points.\n\n" +
                "The next level is level " +
                (rank == nextRank
                        ? " LOL NVM YOU'RE THE TOP LEVEL."
                        : nextRank.getLevel() + ": " + nextRank.getRoleName() + ".") +
                "\n You have " + (rank == nextRank
                ? "LOL NVM YOU'RE THE TOP LEVEL"
                : NumberFormat.getIntegerInstance().format(Rank.xpRequiredForLevel(user.getXp(), nextRank.getLevel())))
                + " points to go.\n\nBest of Luck in the Swampys!";

        MessageEmbed messageEmbed = Constants.simpleEmbed("Level " + rank.getLevel() + " | " + rank.getRoleName(),
                description,
                null,
                null,
                rank.getColor(),
                rank.getRankUpImage());

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
                                    .append(": <@")
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
                                    .append(": <@")
                                    .append(swampyUser.getId())
                                    .append(">, could not find user in member cache!, xp: ")
                                    .append(NumberFormat.getIntegerInstance().format(swampyUser.getXp()))
                                    .append('\n');
                        }
                    });

            event.getChannel().sendFiles(FileUpload.fromData(description.toString().getBytes(), "leaderboard.txt")).queue();
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
                                .append("<@")
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
                log.error("Unable to lookup user [id={}] for leaderboard", u.getId(), e);
                return false;
            }
        }).sorted((u1, u2) -> Long.compare(u2.getXp(), u1.getXp())).limit(10).forEachOrdered(swampyUser -> {
            description.append(LEADERBOARD_MEDALS[place.getAndIncrement()])
                    .append(": <@")
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

        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();

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
        }).toList();

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
        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();

        if (CollectionUtils.size(mentionedMembers) != 1) {
            event.replyError("Please mention one user to downvote. Example `!!swampy down @Santiago`");
            return;
        }

        Member mentionedMember = mentionedMembers.getFirst();
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

        if (mentionedMember.getUser().isBot()) {
            takePointsFromMember(swampyGamesConfig.getDownvotePointsFromDownvotee(), event.getMember(), PointsStat.DOWNVOTED);
            givePointsToMember(swampyGamesConfig.getDownvotePointsToDownvoter(), mentionedMember, PointsStat.DOWNVOTER);
            event.replySuccess("Successfully downvoted " + event.getMember().getEffectiveName() + " \uD83D\uDE08");
        } else {
            takePointsFromMember(swampyGamesConfig.getDownvotePointsFromDownvotee(), mentionedMember, PointsStat.DOWNVOTED);
            givePointsToMember(swampyGamesConfig.getDownvotePointsToDownvoter(), event.getMember(), PointsStat.DOWNVOTER);
            event.replySuccess("Successfully downvoted " + mentionedMember.getEffectiveName());
        }
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

        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();

        if (CollectionUtils.size(mentionedMembers) != 1) {
            event.replyError("Please specify only one user. Example `!!swampy give 100 @Santiago`");
            return;
        }

        Member mentionedMember = mentionedMembers.getFirst();
        if (isNotParticipating(mentionedMember)) {
            event.replyError(mentionedMember.getEffectiveName() + " is not participating in the swampys");
            return;
        }

        givePointsToMember(pointsToGive, mentionedMember, PointsStat.MOD_GIVE);

        event.reply(String.format("Added %s xp to %s", NumberFormat.getIntegerInstance().format(pointsToGive), mentionedMember.getEffectiveName()));
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

        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();

        if (CollectionUtils.size(mentionedMembers) != 1) {
            event.replyError("Please specify only one user. Example `!!swampy take 100 @Santiago`");
            return;
        }

        Member mentionedMember = mentionedMembers.getFirst();
        if (isNotParticipating(mentionedMember)) {
            event.replyError(mentionedMember.getEffectiveName() + " is not participating in the swampy games");
            return;
        }

        takePointsFromMember(pointsToTake, mentionedMember, PointsStat.MOD_TAKE);

        event.reply(String.format("Removed %s xp from %s", NumberFormat.getIntegerInstance().format(pointsToTake), mentionedMember.getEffectiveName()));
    }

    private void reset(CommandEvent event) {
        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        event.reply("Resetting, please wait...");
        for (DiscordUser discordUser : discordUserRepository.findAll()) {
            discordUser.setXp(0);

            for (PointsStat stat : PointsStat.values()) {
                stat.setter().accept(discordUser, 0L);
            }

            discordUserRepository.save(discordUser);
        }

        try {
            Rank.init(restTemplate, baseConfig.airtableApiToken);
        } catch (Exception e) {
            event.replyError("Failed to refresh ranks from Airtable, please manually run the !!rankRefresh command");
            return;
        }

        event.replySuccess("Reset the Swampys");
    }

}
