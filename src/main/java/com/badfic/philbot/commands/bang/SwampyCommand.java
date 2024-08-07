package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.ModHelpAware;
import com.badfic.philbot.commands.Rank;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.hungersim.PlayerRepository;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
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

    public SwampyCommand(final PlayerRepository playerRepository) {
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
    public void execute(final CommandEvent event) {
        final var args = event.getArgs();
        final var msgContent = event.getMessage().getContentRaw();

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
            final var swampyGamesConfig = getSwampyGamesConfig();

            if (NO_NO_WORDS.matcher(msgContent).find()) {
                takePointsFromMember(swampyGamesConfig.getNoNoWordsPoints(), event.getMember(), PointsStat.NO_NO);
            }

            final var message = event.getMessage();

            final var discordUser = getDiscordUserByMember(event.getMember());
            final var now = LocalDateTime.now();
            final var nextMsgBonusTime = discordUser.getLastMessageBonus().plus(swampyGamesConfig.getPictureMsgTimeoutMinutes(), ChronoUnit.MINUTES);

            final var bonus = CollectionUtils.isNotEmpty(message.getAttachments()) || CollectionUtils.isNotEmpty(message.getEmbeds());

            var pointsStat = PointsStat.MESSAGE;
            var pointsToGive = swampyGamesConfig.getNormalMsgPoints();
            final var eventChannelName = event.getChannel().getName();
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

    public void voiceJoined(final Member member) {
        if (isNotParticipating(member)) {
            return;
        }

        final var user = getDiscordUserByMember(member);
        user.setVoiceJoined(LocalDateTime.now());
        discordUserRepository.save(user);
    }

    public void voiceLeft(final Member member) {
        if (isNotParticipating(member)) {
            return;
        }

        final var user = getDiscordUserByMember(member);
        final var timeTheyJoinedVoice = user.getVoiceJoined();

        if (timeTheyJoinedVoice == null) {
            return;
        }

        final var swampyGamesConfig = getSwampyGamesConfig();

        final var minutes = ChronoUnit.MINUTES.between(timeTheyJoinedVoice, LocalDateTime.now());
        final var points = minutes * swampyGamesConfig.getVcPointsPerMinute();
        user.setVoiceJoined(null);
        user.setLifetimeVoiceChatMinutes(user.getLifetimeVoiceChatMinutes() + minutes);
        discordUserRepository.save(user);

        givePointsToMember(points, member, PointsStat.VOICE_CHAT);
    }

    public void emote(final MessageReactionAddEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) {
            return;
        }

        final var swampyGamesConfig = getSwampyGamesConfig();

        givePointsToMember(swampyGamesConfig.getReactionPoints(), event.getMember(), PointsStat.REACTOR_POINTS);
        final var messageId = event.getMessageIdLong();
        final var reactionGiverId = event.getMember().getIdLong();
        final var channel = event.getChannel().asGuildMessageChannel();
        channel.retrieveMessageById(messageId).queue(msg -> {
            if (msg != null && msg.getMember() != null && msg.getMember().getIdLong() != reactionGiverId) {
                givePointsToMember(swampyGamesConfig.getReactionPoints(), msg.getMember(), PointsStat.REACTED_POINTS);
            }
        });

        final var reactionEmote = event.getReaction().getEmoji();
        final var guildId = event.getGuild().getIdLong();

        final MessageEmbed messageEmbed;
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

    public void removeFromGames(final Guild guild, final String id) {
        final var roleById = guild.getRoleById(id);

        if (roleById != null) {
            roleById.delete().queue();
        }

        final var optionalPlayer = playerRepository.findByDiscordUser(id);
        optionalPlayer.ifPresent(playerRepository::delete);

        if (discordUserRepository.existsById(id)) {
            discordUserRepository.deleteById(id);
        }
    }

    public void acceptedBoost(final Member member) {
        final var discordUser = getDiscordUserByMember(member);
        discordUser.setAcceptedBoost(LocalDateTime.now());
        discordUserRepository.save(discordUser);
    }

    public void acceptedMap(final Member member) {
        final var discordUser = getDiscordUserByMember(member);
        discordUser.setAcceptedMapTrivia(LocalDateTime.now());
        discordUserRepository.save(discordUser);
    }

    private void slots(final CommandEvent event) {
        final var member = event.getMember();
        final var discordUser = getDiscordUserByMember(member);

        final var swampyGamesConfig = getSwampyGamesConfig();

        final var now = LocalDateTime.now();
        final var nextSlotsTime = discordUser.getLastSlots().plus(swampyGamesConfig.getSlotsTimeoutMinutes(), ChronoUnit.MINUTES);
        if (now.isBefore(nextSlotsTime)) {
            final var duration = Duration.between(now, nextSlotsTime);

            if (duration.getSeconds() < 60) {
                event.replyError("You must wait " + (duration.getSeconds() + 1) + " seconds before playing slots again");
            } else {
                event.replyError("You must wait " + (duration.toMinutes() + 1) + " minutes before playing slots again");
            }
            return;
        }

        discordUser.setLastSlots(now);

        final var slotsEmojis = Arrays.stream(swampyGamesConfig.getSlotsEmoji()).collect(Collectors.toSet());
        final var size = slotsEmojis.size();

        final var oddsClosEnough = (1.0 / size) * (1.0 / size) * 300.0;
        final var oddsWinnerWinner = (1.0 / size) * (1.0 / size) * (1.0 / size) * 100.0;
        final var footer = String.format("Odds of a WINNER WINNER: %.3f%%\nOdds of a CLOSE ENOUGH: %.3f%%", oddsWinnerWinner, oddsClosEnough);

        final var one = Constants.pickRandom(slotsEmojis);
        final var two = Constants.pickRandom(slotsEmojis);
        final var three = Constants.pickRandom(slotsEmojis);

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

    private void showRank(final CommandEvent event) {
        var member = event.getMember();
        if (CollectionUtils.size(event.getMessage().getMentions().getMembers()) == 1) {
            member = event.getMessage().getMentions().getMembers().getFirst();
        }

        if (isNotParticipating(member)) {
            event.reply(Constants.simpleEmbed("Your Rank", "Is not participating in the games"));
            return;
        }

        final var user = getDiscordUserByMember(member);

        final var allRanks = Rank.getAllRanks();
        final var rank = Rank.byXp(user.getXp());
        final var nextRank = (rank.getOrdinal() >= allRanks.size() - 1) ? rank : allRanks.get(rank.getOrdinal() + 1);

        final var description = rank.getRankUpMessage().replace("<name>", member.getAsMention()).replace("<rolename>", rank.getRoleName()) +
                "\n\nYou have " + NumberFormat.getIntegerInstance().format(user.getXp()) + " total points.\n\n" +
                "The next level is level " +
                (rank == nextRank
                        ? " LOL NVM YOU'RE THE TOP LEVEL."
                        : nextRank.getLevel() + ": " + nextRank.getRoleName() + ".") +
                "\n You have " + (rank == nextRank
                ? "LOL NVM YOU'RE THE TOP LEVEL"
                : NumberFormat.getIntegerInstance().format(Rank.xpRequiredForLevel(user.getXp(), nextRank.getLevel())))
                + " points to go.\n\nBest of Luck in the Swampys!";

        final var messageEmbed = Constants.simpleEmbed("Level " + rank.getLevel() + " | " + rank.getRoleName(),
                description,
                null,
                null,
                rank.getColor(),
                rank.getRankUpImage());

        event.reply(messageEmbed);
    }

    private void leaderboard(final CommandEvent event) {
        final var split = event.getArgs().split("\\s+");
        if (split.length != 2) {
            event.replyError("Badly formatted command. Example `!!swampy leaderboard bastard`, `!!swampy leaderboard chaos`, or `!!swampy leaderboard full`");
            return;
        }

        final var swampyUsers = discordUserRepository.findAll();

        if ("full".equalsIgnoreCase(split[1])) {
            final var place = new AtomicInteger(1);
            final var description = new StringBuilder();
            swampyUsers.stream()
                    .sorted((u1, u2) -> Long.compare(u2.getXp(), u1.getXp()))
                    .forEachOrdered(swampyUser -> {
                        final var memberById = event.getGuild().getMemberById(swampyUser.getId());

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

        final var optionalPointsStat = Arrays.stream(PointsStat.values())
                .filter(stat -> split[1].trim().equalsIgnoreCase(stat.name()))
                .findAny();

        if (optionalPointsStat.isPresent()) {
            final var stat = optionalPointsStat.get();
            final var description = new StringBuilder();

            swampyUsers.stream()
                    .sorted((u1, u2) -> Long.compare(stat.getter.applyAsLong(u2), stat.getter.applyAsLong(u1)))
                    .forEachOrdered(swampyUser -> {
                        description
                                .append("<@")
                                .append(swampyUser.getId())
                                .append("> - ")
                                .append(NumberFormat.getIntegerInstance().format(stat.getter.applyAsLong(swampyUser)))
                                .append('\n');
                    });
            final var messageEmbed = Constants.simpleEmbed(stat.name() + " Leaderboard", description.toString());

            event.reply(messageEmbed);
            return;
        }

        final var place = new AtomicInteger(0);
        final var description = new StringBuilder();
        swampyUsers.stream().filter(u -> {
            try {
                final var member = Objects.requireNonNull(event.getGuild().getMemberById(u.getId()));

                return hasRole(member, StringUtils.containsIgnoreCase(split[1], "bastard") ? Constants.EIGHTEEN_PLUS_ROLE : Constants.CHAOS_CHILDREN_ROLE);
            } catch (final Exception e) {
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

        final var messageEmbed = Constants.simpleEmbed(StringUtils.containsIgnoreCase(split[1], "bastard") ? "Bastard Leaderboard" : "Chaos Leaderboard",
                description.toString());

        event.reply(messageEmbed);
    }

    private void upvote(final CommandEvent event) {
        final var swampyGamesConfig = getSwampyGamesConfig();

        final var mentionedMembers = event.getMessage().getMentions().getMembers();

        if (CollectionUtils.isEmpty(mentionedMembers)) {
            event.replyError("Please mention at least one user to upvote. Example `!!swampy up @Santiago`");
            return;
        }

        final var eligibleMembers = mentionedMembers.stream().filter(mentionedMember -> {
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

        final var discordUser = getDiscordUserByMember(event.getMember());

        final var now = LocalDateTime.now();
        final var nextVoteTime = discordUser.getLastVote().plus(swampyGamesConfig.getUpvoteTimeoutMinutes(), ChronoUnit.MINUTES);
        if (now.isBefore(nextVoteTime)) {
            final var duration = Duration.between(now, nextVoteTime);

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

        for (final var mentionedMember : eligibleMembers) {
            givePointsToMember(swampyGamesConfig.getUpvotePointsToUpvotee(), mentionedMember, PointsStat.UPVOTED);
            event.replySuccess("Successfully upvoted " + mentionedMember.getEffectiveName());
        }
    }

    private void downvote(final CommandEvent event) {
        final var mentionedMembers = event.getMessage().getMentions().getMembers();

        if (CollectionUtils.size(mentionedMembers) != 1) {
            event.replyError("Please mention one user to downvote. Example `!!swampy down @Santiago`");
            return;
        }

        final var mentionedMember = mentionedMembers.getFirst();
        if (isNotParticipating(mentionedMember)) {
            event.replyError(mentionedMember.getEffectiveName() + " is not participating in the swampys");
            return;
        }

        if (event.getMember().getId().equalsIgnoreCase(mentionedMember.getId())) {
            event.replyError("You can't downvote yourself");
            return;
        }

        final var discordUser = getDiscordUserByMember(event.getMember());

        final var swampyGamesConfig = getSwampyGamesConfig();

        final var now = LocalDateTime.now();
        final var nextVoteTime = discordUser.getLastVote().plus(swampyGamesConfig.getDownvoteTimeoutMinutes(), ChronoUnit.MINUTES);
        if (now.isBefore(nextVoteTime)) {
            final var duration = Duration.between(now, nextVoteTime);

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

    private void give(final CommandEvent event) {
        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        final var split = event.getArgs().split("\\s+");
        if (split.length != 3) {
            event.replyError("Badly formatted command. Example `!!swampy give 100 @Santiago`");
            return;
        }

        final long pointsToGive;
        try {
            pointsToGive = Long.parseLong(split[1]);
        } catch (final NumberFormatException e) {
            event.replyError("Failed to parse the number you provided.");
            return;
        }

        if (pointsToGive < 0) {
            event.replyError("Please provide a positive number");
            return;
        }

        final var mentionedMembers = event.getMessage().getMentions().getMembers();

        if (CollectionUtils.size(mentionedMembers) != 1) {
            event.replyError("Please specify only one user. Example `!!swampy give 100 @Santiago`");
            return;
        }

        final var mentionedMember = mentionedMembers.getFirst();
        if (isNotParticipating(mentionedMember)) {
            event.replyError(mentionedMember.getEffectiveName() + " is not participating in the swampys");
            return;
        }

        givePointsToMember(pointsToGive, mentionedMember, PointsStat.MOD_GIVE);

        event.reply(String.format("Added %s xp to %s", NumberFormat.getIntegerInstance().format(pointsToGive), mentionedMember.getEffectiveName()));
    }

    private void take(final CommandEvent event) {
        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        final var split = event.getArgs().split("\\s+");
        if (split.length != 3) {
            event.replyError("Badly formatted command. Example `!!swampy take 100 @Santiago`");
            return;
        }

        final long pointsToTake;
        try {
            pointsToTake = Long.parseLong(split[1]);
        } catch (final NumberFormatException e) {
            event.replyError("Failed to parse the number you provided.");
            return;
        }

        if (pointsToTake < 0) {
            event.replyError("Please provide a positive number");
            return;
        }

        final var mentionedMembers = event.getMessage().getMentions().getMembers();

        if (CollectionUtils.size(mentionedMembers) != 1) {
            event.replyError("Please specify only one user. Example `!!swampy take 100 @Santiago`");
            return;
        }

        final var mentionedMember = mentionedMembers.getFirst();
        if (isNotParticipating(mentionedMember)) {
            event.replyError(mentionedMember.getEffectiveName() + " is not participating in the swampy games");
            return;
        }

        takePointsFromMember(pointsToTake, mentionedMember, PointsStat.MOD_TAKE);

        event.reply(String.format("Removed %s xp from %s", NumberFormat.getIntegerInstance().format(pointsToTake), mentionedMember.getEffectiveName()));
    }

    private void reset(final CommandEvent event) {
        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        event.reply("Resetting, please wait...");
        for (final var discordUser : discordUserRepository.findAll()) {
            discordUser.setXp(0);

            for (final var stat : PointsStat.values()) {
                stat.setter.accept(discordUser, 0L);
            }

            discordUserRepository.save(discordUser);
        }

        try {
            Rank.init(restTemplate, baseConfig.airtableApiToken);
        } catch (final Exception e) {
            event.replyError("Failed to refresh ranks from Airtable, please manually run the !!rankRefresh command");
            return;
        }

        event.replySuccess("Reset the Swampys");
    }

}
