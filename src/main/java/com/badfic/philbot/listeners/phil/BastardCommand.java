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

    private static final long NORMAL_MSG_POINTS = 7;
    private final DiscordUserRepository discordUserRepository;

    @Autowired
    public BastardCommand(DiscordUserRepository discordUserRepository) {
        name = "bastard";
        help = "!!bastard\n" +
                "\t`!!bastard up @incogmeato` upvote a user for the bastard games\n" +
                "\t`!!bastard give 120 @incogmeato` give incogmeato 120 points (admin only)\n" +
                "\t`!!bastard reset` reset everyone back to level 0\n (admin only)";
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
        } else if (msgContent.startsWith("!!bastard give")) {
            give(event);
        } else if (msgContent.startsWith("!!bastard take")) {
            givePointsToMember(100, event.getMember());
            event.reply("I haven't implemented `take` yet, but you're a true bastard for trying to steal. Take an upvote, ya bastard.");
        } else if (msgContent.equals("!!bastard reset")) {
            reset(event);
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

    private void upvote(CommandEvent event) {
        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.isEmpty(mentionedMembers)) {
            event.reply("Please mention a user to upvote. Example `!!bastard up @incogmeato`");
            return;
        }

        givePointsToMember(100, mentionedMembers.get(0));

        event.reply("upvoted " + mentionedMembers.get(0).getAsMention());
    }

    private void give(CommandEvent event) {
        if (!hasRole(event.getMember(), "queens of the castle")) {
            event.reply("You do not have permission to use this command");
            return;
        }

        String msgContent = event.getMessage().getContentRaw();

        String stripped = msgContent.replace("!!bastard give", "").trim();
        String[] split = stripped.split("\\s+");

        if (split.length != 2) {
            event.reply("Badly formatted command. Example `!!bastard give 100 @incogmeato`");
            return;
        }

        long pointsToGive;
        try {
            pointsToGive = Long.parseLong(split[0]);
        } catch (NumberFormatException e) {
            event.reply("Failed to parse the number you provided.");
            return;
        }

        if (pointsToGive < 0) {
            event.reply("Please provide a positive number");
            return;
        }

        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.isEmpty(mentionedMembers) || mentionedMembers.size() > 1) {
            event.reply("Please specify only one user. Example `!!bastard give 100 @incogmeato`");
            return;
        }

        givePointsToMember(pointsToGive, mentionedMembers.get(0));

        event.replyFormatted("Gave %d xp to %s", pointsToGive, mentionedMembers.get(0).getAsMention());
    }

    private void reset(CommandEvent event) {
        if (!hasRole(event.getMember(), "queens of the castle")) {
            event.reply("You do not have permission to use this command");
            return;
        }

        for (DiscordUser discordUser : discordUserRepository.findAll()) {
            discordUser.setXp(0);
            discordUser = discordUserRepository.save(discordUser);
            assignRolesIfNeeded(event.getGuild().getMemberById(discordUser.getId()), discordUser);
        }

        event.reply("Reset the bastard games");
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
                TextChannel announcementsChannel = member.getGuild().getTextChannelsByName("test-channel", false).get(0);

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
