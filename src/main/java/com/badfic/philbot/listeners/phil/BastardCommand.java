package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.Rank;
import com.badfic.philbot.repository.DiscordUserRepository;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Resource;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BastardCommand extends Command implements PhilMarker {

    private static volatile boolean AWAITING_RESET_CONFIRMATION = false;
    private static final long NORMAL_MSG_POINTS = 7;
    private static final String[] LEADERBOARD_MEDALS = {
            "\uD83E\uDD47", "\uD83E\uDD48", "\uD83E\uDD49",
            "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40"
    };
    private static final String SLOT_MACHINE = "\uD83C\uDFB0";
    private static final Set<String> SLOTS = new HashSet<>(Arrays.asList(
            "\uD83E\uDD5D", "\uD83C\uDF53", "\uD83C\uDF4B", "\uD83E\uDD6D", "\uD83C\uDF51", "\uD83C\uDF48", "\uD83C\uDF4A", "\uD83C\uDF4D", "\uD83C\uDF50",
            "\uD83C\uDF47", "\uD83C\uDF49", "\uD83C\uDF4C", "\uD83C\uDF52", "\uD83C\uDF4E"
    ));

    private final DiscordUserRepository discordUserRepository;

    @Resource(name = "philJda")
    @Lazy
    private JDA philJda;

    @Autowired
    public BastardCommand(DiscordUserRepository discordUserRepository) {
        name = "bastard";
        help = "`!!bastard`\n" +
                "\t`!!bastard rank` show your bastard rank\n" +
                "\t`!!bastard leaderboard` show the bastard leaderboard\n" +
                "\t`!!bastard up @incogmeato` upvote a user for the bastard games\n" +
                "\t`!!bastard down @incogmeato` downvote a user for the bastard games\n" +
                "\t`!!bastard flip` Flip a coin. Heads you win, tails you lose\n" +
                "\t`!!bastard slots` Play slots. Winners for 2 out of 3 or 3 out of 3.\n" +
                "\t`!!bastard steal @incogmeato` Attempt to steal some points from incogmeato\n" +
                "\t`!!bastard give 120 @incogmeato` give 120 points to incogmeato (mods only)\n" +
                "\t`!!bastard take 120 @incogmeato` remove 120 points from incogmeato (mods only)\n" +
                "\t`!!bastard set 120 @incogmeato` set incogmeato to 120 points (mods only)\n" +
                "\t`!!bastard reset` reset everyone back to level 0\n (mods only)";
        this.discordUserRepository = discordUserRepository;
    }

    @Scheduled(cron = "0 0 1 * * ?", zone = "GMT")
    public void lottery() {
        List<DiscordUser> allUsers = discordUserRepository.findAll();

        long startTime = System.currentTimeMillis();
        Member member = null;
        while (member == null && System.currentTimeMillis() - startTime < TimeUnit.SECONDS.toMillis(30)) {
            DiscordUser winningUser = pickRandom(allUsers);

            try {
                Member memberById = philJda.getGuilds().get(0).retrieveMemberById(winningUser.getId()).complete();
                if (memberById != null && hasRole(memberById, "18+") && winningUser.getUpdateTime().isAfter(LocalDateTime.now().minusHours(24))) {
                    member = memberById;
                }
            } catch (Exception ignored) {}
        }

        if (member == null) {
            philJda.getTextChannelsByName("bastard-of-the-week", false)
                    .get(0)
                    .sendMessage(simpleEmbed("Lottery Results", "Unable to choose a lottery winner, nobody wins"))
                    .queue();
            return;
        }

        givePointsToMember(1000, member);

        philJda.getTextChannelsByName("bastard-of-the-week", false)
                .get(0)
                .sendMessage(simpleEmbed("Lottery Results", "Congratulations %s you won today's lottery worth 1000 bastard points!", member.getAsMention()))
                .queue();
    }

    public void givePointsToMember(long pointsToGive, Member member, DiscordUser user) {
        if (!hasRole(member, "18+")) {
            return;
        }

        user.setXp(user.getXp() + pointsToGive);
        user = discordUserRepository.save(user);
        assignRolesIfNeeded(member, user);
    }

    public void givePointsToMember(long pointsToGive, Member member) {
        givePointsToMember(pointsToGive, member, getDiscordUserByMember(member));
    }

    public void voiceJoined(Member member) {
        if (!hasRole(member, "18+")) {
            return;
        }

        DiscordUser user = getDiscordUserByMember(member);
        user.setVoiceJoined(LocalDateTime.now());
        discordUserRepository.save(user);
    }

    public void voiceLeft(Member member) {
        if (!hasRole(member, "18+")) {
            return;
        }

        DiscordUser user = getDiscordUserByMember(member);
        LocalDateTime timeTheyJoinedVoice = user.getVoiceJoined();

        if (timeTheyJoinedVoice == null) {
            return;
        }

        long minutes = ChronoUnit.MINUTES.between(timeTheyJoinedVoice, LocalDateTime.now());
        long points = minutes * 5;
        user.setVoiceJoined(null);
        discordUserRepository.save(user);

        givePointsToMember(points, member);
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getAuthor().isBot() || !hasRole(event.getMember(), "18+")) {
            return;
        }

        String msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith("!!bastard rank")) {
            showRank(event);
        } else if (msgContent.startsWith("!!bastard up")) {
            upvote(event);
        } else if (msgContent.startsWith("!!bastard down")) {
            downvote(event);
        } else if (msgContent.startsWith("!!bastard give")) {
            give(event);
        } else if (msgContent.startsWith("!!bastard take")) {
            take(event);
        } else if (msgContent.startsWith("!!bastard set")) {
            set(event);
        } else if (msgContent.startsWith("!!bastard leaderboard")) {
            leaderboard(event);
        } else if (msgContent.startsWith("!!bastard steal")) {
            steal(event);
        } else if (msgContent.startsWith("!!bastard fight")) {
            event.reply(simpleEmbed("Fight", "Fights not implemented yet, come back soon. Here's 1 bastard point for your troubles"));
            givePointsToMember(1, event.getMember());
        } else if (msgContent.startsWith("!!bastard slots")) {
            slots(event);
        } else if (msgContent.startsWith("!!bastard flip")) {
            flip(event);
        } else if (msgContent.startsWith("!!bastard reset")) {
            if (!hasRole(event.getMember(), "queens of the castle")) {
                event.replyError("You do not have permission to use this command");
                return;
            }

            if (!AWAITING_RESET_CONFIRMATION) {
                event.reply(simpleEmbed("Reset The Games", "Are you sure you want to reset the bastard games? This will reset everyone's points. If you are sure, type `!!bastard reset confirm`"));
                AWAITING_RESET_CONFIRMATION = true;
            } else {
                if (!msgContent.startsWith("!!bastard reset confirm")) {
                    event.reply(simpleEmbed("Abort Reset", "Bastard reset aborted. You must type `!!bastard reset` and then confirm it with `!!bastard reset confirm`."));
                    AWAITING_RESET_CONFIRMATION = false;
                    return;
                }
                reset(event);
                AWAITING_RESET_CONFIRMATION = false;
            }
        } else if (msgContent.startsWith("!!bastard")){
            event.replyError("Unrecognized bastard command");
        } else {
            Message message = event.getMessage();

            long pointsToGive = NORMAL_MSG_POINTS;
            DiscordUser discordUser = getDiscordUserByMember(event.getMember());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextMsgBonusTime = discordUser.getLastMessageBonus().plus(1, ChronoUnit.MINUTES);
            if (now.isAfter(nextMsgBonusTime)) {
                discordUser.setLastMessageBonus(now);

                pointsToGive += CollectionUtils.isNotEmpty(message.getAttachments()) ? 100 : 0;
                pointsToGive += CollectionUtils.isNotEmpty(message.getEmbeds()) ? 100 : 0;
                pointsToGive += CollectionUtils.isNotEmpty(message.getEmotes()) ? 100 : 0;

                if (event.getChannel().getName().equalsIgnoreCase("bot-space")) {
                    pointsToGive = 1;
                } else if (event.getChannel().getName().equalsIgnoreCase("cursed-swamp")) {
                    pointsToGive += NORMAL_MSG_POINTS;
                }
            }

            givePointsToMember(pointsToGive, event.getMember(), discordUser);
        }
    }

    private DiscordUser getDiscordUserByMember(Member member) {
        String userId = member.getId();
        Optional<DiscordUser> optionalUserEntity = discordUserRepository.findById(userId);

        if (!optionalUserEntity.isPresent()) {
            DiscordUser newUser = new DiscordUser();
            newUser.setId(userId);
            optionalUserEntity = Optional.of(discordUserRepository.save(newUser));
        }

        return optionalUserEntity.get();
    }

    private void flip(CommandEvent event) {
        int randomNumber = ThreadLocalRandom.current().nextInt(100);

        if (randomNumber < 5) {
            takePointsFromMember(4, event.getMember());
            event.reply(simpleEmbed("Flip", "I don't feel like flipping a coin, I'm taking 4 bastard points from you \uD83D\uDCA9"));
        } else if (randomNumber % 2 == 0) {
            givePointsToMember(20, event.getMember());
            event.reply(simpleEmbed("Flip", "I flipped a coin and it landed on heads, here's 20 bastard points \uD83D\uDCB0"));
        } else {
            takePointsFromMember(10, event.getMember());
            event.reply(simpleEmbed("Flip", "I flipped a coin and it landed on tails, you lost 10 bastard points \uD83D\uDE2C"));
        }
    }

    private void slots(CommandEvent event) {
        Member member = event.getMember();
        DiscordUser discordUser = getDiscordUserByMember(member);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextSlotsTime = discordUser.getLastSlots().plus(3, ChronoUnit.MINUTES);
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

        String one = pickRandom(SLOTS);
        String two = pickRandom(SLOTS);
        String three = pickRandom(SLOTS);

        if (one.equalsIgnoreCase(two) && two.equalsIgnoreCase(three)) {
            givePointsToMember(1000, member, discordUser);
            event.reply(simpleEmbed(SLOT_MACHINE + " WINNER WINNER!! " + SLOT_MACHINE, "%s\n%s%s%s \nYou won 1000 bastard points!",
                    member.getAsMention(), one, two, three));
        } else if (one.equalsIgnoreCase(two) || one.equalsIgnoreCase(three) || two.equalsIgnoreCase(three)) {
            givePointsToMember(10, member, discordUser);
            event.reply(simpleEmbed(SLOT_MACHINE + " CLOSE ENOUGH! " + SLOT_MACHINE, "%s\n%s%s%s \nYou got 2 out of 3! You won 10 bastard points!",
                    member.getAsMention(), one, two, three));
        } else {
            event.reply(simpleEmbed(SLOT_MACHINE + " Better luck next time! " + SLOT_MACHINE, "%s\n%s%s%s",
                    member.getAsMention(), one, two, three));
            discordUserRepository.save(discordUser);
        }
    }

    private void steal(CommandEvent event) {
        List<Member> mentionedUsers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.isEmpty(mentionedUsers) || mentionedUsers.size() > 1) {
            event.replyError("Please only mention one user. Example `!!bastard steal @incogmeato`");
            return;
        }

        Member mentionedMember = mentionedUsers.get(0);
        if (event.getMember().getId().equalsIgnoreCase(mentionedMember.getId())) {
            event.replyError("You can't steal from yourself");
            return;
        }

        if (!hasRole(mentionedMember, "18+")) {
            event.replyError("You can't steal from non 18+ members");
            return;
        }

        DiscordUser mentionedUser = getDiscordUserByMember(mentionedMember);
        if (mentionedUser.getXp() <= 0) {
            event.replyError("You can't steal from them, they don't have any points");
            return;
        }

        int randomNumber;
        if ((randomNumber = ThreadLocalRandom.current().nextInt(1, 101)) < 30) {
            event.reply(simpleEmbed("Steal", "You attempt to steal points from %s and fail miserably, you pay them %s points to forget this ever happened. \uD83D\uDE2C",
                    mentionedMember, randomNumber));
            takePointsFromMember(randomNumber, event.getMember());
            givePointsToMember(randomNumber, mentionedMember);
        } else if ((randomNumber = ThreadLocalRandom.current().nextInt(1, 101)) < 30) {
            event.reply(simpleEmbed("Steal", "You successfully stole %s points from %s \uD83D\uDCB0", randomNumber, mentionedMember));
            takePointsFromMember(randomNumber, mentionedMember);
            givePointsToMember(randomNumber, event.getMember());
        } else if (ThreadLocalRandom.current().nextInt(1, 101) < 80) {
            event.reply(simpleEmbed("Steal", "You failed to steal from %s but escaped unnoticed. Take 2 points for your troubles. \uD83D\uDCB0", mentionedMember));
            givePointsToMember(2, event.getMember());
        } else {
            randomNumber = ThreadLocalRandom.current().nextInt(1, 21);
            event.reply(simpleEmbed("Steal", "I steal %s points, from both of you! Phil wins! \uD83D\uDE2C", randomNumber));
            takePointsFromMember(randomNumber, event.getMember());
            takePointsFromMember(randomNumber, mentionedMember);
        }
    }

    private void takePointsFromMember(long pointsToTake, Member member) {
        if (!hasRole(member, "18+")) {
            return;
        }

        DiscordUser user = getDiscordUserByMember(member);
        user.setXp(Math.max(0, user.getXp() - pointsToTake));
        user = discordUserRepository.save(user);
        assignRolesIfNeeded(member, user);
    }

    private void setPointsForMember(long points, Member member) {
        DiscordUser user = getDiscordUserByMember(member);
        user.setXp(points);
        user = discordUserRepository.save(user);
        assignRolesIfNeeded(member, user);
    }

    private void showRank(CommandEvent event) {
        Member member = event.getMember();
        if (CollectionUtils.isNotEmpty(event.getMessage().getMentionedUsers())) {
            member = event.getMessage().getMentionedMembers().get(0);
        }

        if (!hasRole(member, "18+")) {
            event.reply(simpleEmbed("Your Rank", "You can't see your rank because it appears you don't have the 18+ role"));
            return;
        }

        DiscordUser user = getDiscordUserByMember(member);
        Role role = assignRolesIfNeeded(member, user);

        Rank[] allRanks = Rank.values();
        Rank rank = Rank.byXp(user.getXp());
        Rank nextRank = (rank.ordinal() > allRanks.length - 1) ? rank : allRanks[rank.ordinal() + 1];

        MessageEmbed messageEmbed = new EmbedBuilder()
                .setImage(rank.getRankUpImage())
                .setTitle("Level " + rank.getLevel() + ": " + rank.getRoleName())
                .setColor(role.getColor())
                .setDescription(rank.getRankUpMessage().replace("<name>", member.getAsMention()).replace("<rolename>", rank.getRoleName()) +
                        "\n\nYou have " + NumberFormat.getIntegerInstance().format(user.getXp()) + " total bastard points.\n\n" +
                        "The next level is level " +
                        (rank == nextRank
                                ? " LOL NVM YOU'RE THE TOP LEVEL."
                                : nextRank.getLevel() + ": " + nextRank.getRoleName() + ".") +
                        "\n You have " + NumberFormat.getIntegerInstance().format((nextRank.getLevel() * Rank.LVL_MULTIPLIER) - user.getXp()) + " points to go." +
                        "\n\nBest of Luck in the Bastard Games!")
                .build();

        event.reply(messageEmbed);
    }

    private void leaderboard(CommandEvent event) {
        List<DiscordUser> bastardUsers = discordUserRepository.findAll();

        bastardUsers.sort((u1, u2) -> Long.compare(u2.getXp(), u1.getXp())); // Descending sort

        AtomicInteger place = new AtomicInteger(0);
        StringBuilder description = new StringBuilder();
        bastardUsers.stream().limit(10).forEachOrdered(bastardUser -> {
            description.append(LEADERBOARD_MEDALS[place.getAndIncrement()])
                    .append(": <@!")
                    .append(bastardUser.getId())
                    .append("> - ")
                    .append(NumberFormat.getIntegerInstance().format(bastardUser.getXp()))
                    .append('\n');
        });

        MessageEmbed messageEmbed = new EmbedBuilder()
                .setTitle("Leaderboard")
                .setDescription(description.toString())
                .build();

        event.reply(messageEmbed);
    }

    private MessageEmbed simpleEmbed(String title, String format, Object... args) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(String.format(format, args))
                .build();
    }

    private void upvote(CommandEvent event) {
        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.isEmpty(mentionedMembers) || mentionedMembers.size() > 1) {
            event.replyError("Please mention one user to upvote. Example `!!bastard up @incogmeato`");
            return;
        }

        if (!hasRole(mentionedMembers.get(0), "18+")) {
            event.replyError(mentionedMembers.get(0).getAsMention() + " is not participating in the bastard games");
            return;
        }

        if (event.getMember().getId().equalsIgnoreCase(mentionedMembers.get(0).getId())) {
            event.replyError("You can't upvote yourself");
            return;
        }

        givePointsToMember(250, mentionedMembers.get(0));

        event.replySuccess("Successfully upvoted " + mentionedMembers.get(0).getAsMention());
    }

    private void downvote(CommandEvent event) {
        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.isEmpty(mentionedMembers) || mentionedMembers.size() > 1) {
            event.replyError("Please mention one user to downvote. Example `!!bastard down @incogmeato`");
            return;
        }

        if (!hasRole(mentionedMembers.get(0), "18+")) {
            event.replyError(mentionedMembers.get(0).getAsMention() + " is not participating in the bastard games");
            return;
        }

        if (event.getMember().getId().equalsIgnoreCase(mentionedMembers.get(0).getId())) {
            event.replyError("You can't downvote yourself");
            return;
        }

        takePointsFromMember(50, mentionedMembers.get(0));

        event.replySuccess("Successfully downvoted " + mentionedMembers.get(0).getAsMention());
    }

    private void give(CommandEvent event) {
        if (!hasRole(event.getMember(), "queens of the castle")) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        String msgContent = event.getMessage().getContentRaw();

        String stripped = msgContent.replace("!!bastard give", "").trim();
        String[] split = stripped.split("\\s+");

        if (split.length != 2) {
            event.replyError("Badly formatted command. Example `!!bastard give 100 @incogmeato`");
            return;
        }

        long pointsToGive;
        try {
            pointsToGive = Long.parseLong(split[0]);
        } catch (NumberFormatException e) {
            event.replyError("Failed to parse the number you provided.");
            return;
        }

        if (pointsToGive < 0) {
            event.replyError("Please provide a positive number");
            return;
        }

        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.isEmpty(mentionedMembers) || mentionedMembers.size() > 1) {
            event.replyError("Please specify only one user. Example `!!bastard give 100 @incogmeato`");
            return;
        }

        if (!hasRole(mentionedMembers.get(0), "18+")) {
            event.replyError(mentionedMembers.get(0).getAsMention() + " is not participating in the bastard games");
            return;
        }

        givePointsToMember(pointsToGive, mentionedMembers.get(0));

        event.replyFormatted("Added %s xp to %s", NumberFormat.getIntegerInstance().format(pointsToGive), mentionedMembers.get(0).getAsMention());
    }

    private void take(CommandEvent event) {
        if (!hasRole(event.getMember(), "taketh away")) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        String msgContent = event.getMessage().getContentRaw();

        String stripped = msgContent.replace("!!bastard take", "").trim();
        String[] split = stripped.split("\\s+");

        if (split.length != 2) {
            event.replyError("Badly formatted command. Example `!!bastard take 100 @incogmeato`");
            return;
        }

        long pointsToTake;
        try {
            pointsToTake = Long.parseLong(split[0]);
        } catch (NumberFormatException e) {
            event.replyError("Failed to parse the number you provided.");
            return;
        }

        if (pointsToTake < 0) {
            event.replyError("Please provide a positive number");
            return;
        }

        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.isEmpty(mentionedMembers) || mentionedMembers.size() > 1) {
            event.replyError("Please specify only one user. Example `!!bastard take 100 @incogmeato`");
            return;
        }

        if (!hasRole(mentionedMembers.get(0), "18+")) {
            event.replyError(mentionedMembers.get(0).getAsMention() + " is not participating in the bastard games");
            return;
        }

        takePointsFromMember(pointsToTake, mentionedMembers.get(0));

        event.replyFormatted("Removed %s xp from %s", NumberFormat.getIntegerInstance().format(pointsToTake), mentionedMembers.get(0).getAsMention());
    }

    private void set(CommandEvent event) {
        if (!hasRole(event.getMember(), "taketh away")) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        String msgContent = event.getMessage().getContentRaw();

        String stripped = msgContent.replace("!!bastard set", "").trim();
        String[] split = stripped.split("\\s+");

        if (split.length != 2) {
            event.replyError("Badly formatted command. Example `!!bastard set 100 @incogmeato`");
            return;
        }

        long pointsToSet;
        try {
            pointsToSet = Long.parseLong(split[0]);
        } catch (NumberFormatException e) {
            event.replyError("Failed to parse the number you provided.");
            return;
        }

        if (pointsToSet < 0) {
            event.replyError("Please provide a positive number");
            return;
        }

        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.isEmpty(mentionedMembers) || mentionedMembers.size() > 1) {
            event.replyError("Please specify only one user. Example `!!bastard set 100 @incogmeato`");
            return;
        }

        if (!hasRole(mentionedMembers.get(0), "18+")) {
            event.replyError(mentionedMembers.get(0).getAsMention() + " is not participating in the bastard games");
            return;
        }

        setPointsForMember(pointsToSet, mentionedMembers.get(0));

        event.replyFormatted("Set xp to %s for %s", NumberFormat.getIntegerInstance().format(pointsToSet), mentionedMembers.get(0).getAsMention());
    }

    private void reset(CommandEvent event) {
        if (!hasRole(event.getMember(), "queens of the castle")) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        for (DiscordUser discordUser : discordUserRepository.findAll()) {
            discordUser.setXp(0);
            discordUser = discordUserRepository.save(discordUser);

            try {
                final DiscordUser finalDiscordUser = discordUser;
                event.getGuild().retrieveMemberById(discordUser.getId()).submit().whenComplete((member, err) -> {
                    if (member != null) {
                        assignRolesIfNeeded(member, finalDiscordUser);
                    } else {
                        event.replyError("Failed to reset roles for user with discord id: " + finalDiscordUser.getId());
                    }
                });
            } catch (Exception e) {
                event.replyError("Failed to reset roles for user with discord id: " + discordUser.getId());
            }
        }

        event.replySuccess("Reset the bastard games");
    }

    private Role assignRolesIfNeeded(Member member, DiscordUser user) {
        if (!hasRole(member, "18+")) {
            return null;
        }

        Rank newRank = Rank.byXp(user.getXp());
        Role newRole = member.getGuild().getRolesByName(newRank.getRoleName(), true).get(0);

        if (!hasRole(member, newRank)) {
            Set<String> allRoleNames = Rank.getAllRoleNames();
            Guild guild = member.getGuild();

            member.getRoles().stream().filter(r -> allRoleNames.contains(r.getName())).forEach(roleToRemove -> {
                guild.removeRoleFromMember(member, roleToRemove).complete();
            });

            guild.addRoleToMember(member, newRole).complete();

            if (newRank != Rank.CINNAMON_ROLL) {
                TextChannel announcementsChannel = member.getGuild().getTextChannelsByName("bastard-of-the-week", false).get(0);

                MessageEmbed messageEmbed = new EmbedBuilder()
                        .setImage(newRank.getRankUpImage())
                        .setTitle("Level Change!")
                        .setColor(newRole.getColor())
                        .setDescription(newRank.getRankUpMessage().replace("<name>", member.getAsMention()).replace("<rolename>", newRank.getRoleName()))
                        .build();

                announcementsChannel.sendMessage(messageEmbed).queue();
            }
        }

        return newRole;
    }

    private static boolean hasRole(Member member, Rank rank) {
        return hasRole(member, rank.getRoleName());
    }

    private static boolean hasRole(Member member, String role) {
        return member.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(role));
    }

    private static <T> T pickRandom(Collection<T> collection) {
        int index = ThreadLocalRandom.current().nextInt(collection.size());
        Iterator<T> iterator = collection.iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

}
