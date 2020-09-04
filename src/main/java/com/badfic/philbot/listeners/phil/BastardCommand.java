package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.Rank;
import com.badfic.philbot.repository.DiscordUserRepository;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BastardCommand extends Command implements PhilMarker {

    private static volatile boolean AWAITING_RESET_CONFIRMATION = false;

    private static final long NORMAL_MSG_POINTS = 7;
    private final DiscordUserRepository discordUserRepository;

    @Autowired
    public BastardCommand(DiscordUserRepository discordUserRepository) {
        name = "bastard";
        help = "`!!bastard`\n" +
                "\t`!!bastard rank` show your bastard rank\n" +
                "\t`!!bastard up @incogmeato` upvote a user for the bastard games\n" +
                "\t`!!bastard down @incogmeato` downvote a user for the bastard games\n" +
                "\t`!!bastard flip` Flip a coin. Heads you win, tails you lose\n" +
                "\t`!!bastard steal @incogmeato` Attempt to steal some points from incogmeato\n" +
                "\t`!!bastard give 120 @incogmeato` give 120 points to incogmeato (mods only)\n" +
                "\t`!!bastard take 120 @incogmeato` remove 120 points from incogmeato (mods only)\n" +
                "\t`!!bastard reset` reset everyone back to level 0\n (mods only)";
        this.discordUserRepository = discordUserRepository;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getAuthor().isBot()) {
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
        } else if (msgContent.startsWith("!!bastard steal")) {
            List<Member> mentionedUsers = event.getMessage().getMentionedMembers();

            if (CollectionUtils.isEmpty(mentionedUsers) || mentionedUsers.size() > 1) {
                event.replyError("Please only mention one user. Example `!!bastard steal @incogmeato`");
                return;
            }

            Member mentionedMember = mentionedUsers.get(0);
            int randomNumber;

            if ((randomNumber = ThreadLocalRandom.current().nextInt(100)) < 20) {
                event.reply(simpleEmbed("Steal", "You attempt to steal points from %s and fail miserably, you pay them %s points to forget this ever happened.", mentionedMember, randomNumber));
                takePointsFromMember(randomNumber, event.getMember());
                givePointsToMember(randomNumber, mentionedMember);
            } else if ((randomNumber = ThreadLocalRandom.current().nextInt(100)) < 20) {
                event.reply(simpleEmbed("Steal", "You successfully stole %s points from %s", randomNumber, mentionedMember));
                takePointsFromMember(randomNumber, mentionedMember);
                givePointsToMember(randomNumber, event.getMember());
            } else if (ThreadLocalRandom.current().nextInt(100) < 50) {
                event.reply(simpleEmbed("Steal", "You failed to steal from %s but escaped unnoticed. Take 2 points for your troubles.", mentionedMember));
                givePointsToMember(2, event.getMember());
            } else {
                randomNumber = ThreadLocalRandom.current().nextInt(20);
                event.reply(simpleEmbed("Steal", "I steal %s points, from both of you! Phil wins!", randomNumber));
                takePointsFromMember(randomNumber, event.getMember());
                takePointsFromMember(randomNumber, mentionedMember);
            }
        } else if (msgContent.startsWith("!!bastard fight")) {
            event.reply(simpleEmbed("Fight", "Fights not implemented yet, come back soon. Here's 1 bastard point for your troubles"));
            givePointsToMember(1, event.getMember());
        } else if (msgContent.startsWith("!!bastard gamble")) {
            event.reply(simpleEmbed("Bet", "Bets not implemented yet, come back soon. Here's 1 bastard point for your troubles"));
            givePointsToMember(1, event.getMember());
        } else if (msgContent.startsWith("!!bastard roll")) {
            event.reply(simpleEmbed("Roll", "Rolls not implemented yet, come back soon. Here's 1 bastard point for your troubles"));
            givePointsToMember(1, event.getMember());
        } else if (msgContent.startsWith("!!bastard flip")) {
            int randomNumber = ThreadLocalRandom.current().nextInt(100);

            if (randomNumber < 15) {
                takePointsFromMember(4, event.getMember());
                event.reply(simpleEmbed("Flip", "I don't feel like flipping a coin, I'm taking 4 bastard points from you \uD83D\uDCA9"));
            } else if (randomNumber % 2 == 0) {
                givePointsToMember(2, event.getMember());
                event.reply(simpleEmbed("Flip", "I flipped a coin and it landed on heads, here's 2 bastard points \uD83D\uDCB0"));
            } else {
                takePointsFromMember(4, event.getMember());
                event.reply(simpleEmbed("Flip", "I flipped a coin and it landed on tails, you lost 4 bastard points \uD83D\uDE2C"));
            }
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
            pointsToGive += CollectionUtils.isNotEmpty(message.getAttachments()) ? 10 : 0;
            pointsToGive += CollectionUtils.isNotEmpty(message.getEmbeds()) ? 10 : 0;
            pointsToGive += CollectionUtils.isNotEmpty(message.getEmotes()) ? 10 : 0;
            pointsToGive += message.isTTS() ? 10 : 0;

            givePointsToMember(pointsToGive, event.getMember());
        }
    }

    public void givePointsToMember(long pointsToGive, Member member) {
        String userId = member.getId();
        Optional<DiscordUser> optionalUserEntity = discordUserRepository.findById(userId);

        if (!optionalUserEntity.isPresent()) {
            DiscordUser newUser = new DiscordUser();
            newUser.setId(userId);
            optionalUserEntity = Optional.of(discordUserRepository.save(newUser));
        }

        DiscordUser user = optionalUserEntity.get();
        user.setXp(user.getXp() + pointsToGive);
        user = discordUserRepository.save(user);
        assignRolesIfNeeded(member, user);
    }

    public void takePointsFromMember(long pointsToTake, Member member) {
        String userId = member.getId();
        Optional<DiscordUser> optionalUserEntity = discordUserRepository.findById(userId);

        if (!optionalUserEntity.isPresent()) {
            DiscordUser newUser = new DiscordUser();
            newUser.setId(userId);
            optionalUserEntity = Optional.of(discordUserRepository.save(newUser));
        }

        DiscordUser user = optionalUserEntity.get();
        user.setXp(Math.max(0, user.getXp() - pointsToTake));
        user = discordUserRepository.save(user);
        assignRolesIfNeeded(member, user);
    }

    private void showRank(CommandEvent event) {
        Member member = event.getMember();
        if (CollectionUtils.isNotEmpty(event.getMessage().getMentionedUsers())) {
            member = event.getMessage().getMentionedMembers().get(0);
        }

        Optional<DiscordUser> optionalUserEntity = discordUserRepository.findById(member.getId());

        if (!optionalUserEntity.isPresent()) {
            DiscordUser newUser = new DiscordUser();
            newUser.setId(member.getId());
            optionalUserEntity = Optional.of(discordUserRepository.save(newUser));
        }

        DiscordUser user = optionalUserEntity.get();
        assignRolesIfNeeded(member, user);

        Rank[] allRanks = Rank.values();
        Rank rank = Rank.byXp(user.getXp());
        Rank nextRank = (rank.ordinal() > allRanks.length - 1) ? rank : allRanks[rank.ordinal() + 1];

        MessageEmbed messageEmbed = new EmbedBuilder()
                .setImage(rank.getRankUpImage())
                .setTitle("Your Rank")
                .setDescription("Hey, " + member.getAsMention() + "! You are level " + rank.getLevel() + ": " + rank.getRoleName() + ".\n" +
                        "You have " + user.getXp() + " total bastard points.\n\n" +
                        "The next level is " +
                        (rank == nextRank
                                ? " LOL NVM YOU'RE THE TOP LEVEL."
                                : nextRank.getLevel() + ": " + nextRank.getRoleName() + ".") +
                        "\n You need to reach " + (nextRank.getLevel() * Rank.LVL_MULTIPLIER) + " points to get there." +
                        "\n\nBest of Luck in the Bastard Games!")
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

        givePointsToMember(100, mentionedMembers.get(0));

        event.replySuccess("Successfully upvoted " + mentionedMembers.get(0).getAsMention());
    }

    private void downvote(CommandEvent event) {
        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.isEmpty(mentionedMembers) || mentionedMembers.size() > 1) {
            event.replyError("Please mention one user to downvote. Example `!!bastard down @incogmeato`");
            return;
        }

        takePointsFromMember(20, mentionedMembers.get(0));

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

        givePointsToMember(pointsToGive, mentionedMembers.get(0));

        event.replyFormatted("Gave %d xp to %s", pointsToGive, mentionedMembers.get(0).getAsMention());
    }

    private void take(CommandEvent event) {
        if (!hasRole(event.getMember(), "queens of the castle")) {
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

        takePointsFromMember(pointsToTake, mentionedMembers.get(0));

        event.replyFormatted("Removed %d xp from %s", pointsToTake, mentionedMembers.get(0).getAsMention());
    }

    private void reset(CommandEvent event) {
        if (!hasRole(event.getMember(), "queens of the castle")) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        for (DiscordUser discordUser : discordUserRepository.findAll()) {
            discordUser.setXp(0);
            discordUser = discordUserRepository.save(discordUser);
            assignRolesIfNeeded(event.getGuild().getMemberById(discordUser.getId()), discordUser);
        }

        event.replySuccess("Reset the bastard games");
    }

    private void assignRolesIfNeeded(Member member, DiscordUser user) {
        Rank newRank = Rank.byXp(user.getXp());

        if (!hasRole(member, newRank)) {
            Set<String> allRoleNames = Rank.getAllRoleNames();
            Guild guild = member.getGuild();

            member.getRoles().stream().filter(r -> allRoleNames.contains(r.getName())).forEach(roleToRemove -> {
                guild.removeRoleFromMember(member, roleToRemove).complete();
            });

            guild.addRoleToMember(member, guild.getRolesByName(newRank.getRoleName(), true).get(0)).complete();

            if (newRank != Rank.CINNAMON_ROLL) {
                TextChannel announcementsChannel = member.getGuild().getTextChannelsByName("bot-space", false).get(0);

                MessageEmbed messageEmbed = new EmbedBuilder()
                        .setImage(newRank.getRankUpImage())
                        .setTitle("Level Changed!")
                        .setDescription("Congratulations, " + member.getAsMention() + "! You are now level " + newRank.getLevel() + ": " + newRank.getRoleName())
                        .build();

                announcementsChannel.sendMessage(messageEmbed).queue();
            }
        }
    }

    private static boolean hasRole(Member member, Rank rank) {
        return hasRole(member, rank.getRoleName());
    }

    private static boolean hasRole(Member member, String role) {
        return member.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(role));
    }

}
