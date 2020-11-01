package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.DiscordUserRepository;
import com.badfic.philbot.data.phil.Rank;
import com.badfic.philbot.data.phil.SwampyGamesConfigRepository;
import com.jagrosh.jdautilities.command.Command;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Resource;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;

public abstract class BaseSwampy extends Command {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // sweepstakes, taxes, robinhood
    public static final BigDecimal ONE_HUNDREDTH = new BigDecimal("0.01");
    public static final long TAX_OR_ROBINHOOD_MINIMUM_POINT_THRESHOLD = 99;
    public static final long SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD = 999;

    // slots
    public static final long SLOTS_WIN_POINTS = 10_000;
    public static final long SLOTS_TWO_OUT_OF_THREE_POINTS = 50;

    // emoji
    public static final String[] LEADERBOARD_MEDALS = {
            "\uD83E\uDD47", "\uD83E\uDD48", "\uD83E\uDD49",
            "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40"
    };
    public static final String SLOT_MACHINE = "\uD83C\uDFB0";
    public static final Set<String> SPOOKY_SLOTS = new HashSet<>(Arrays.asList(
            "\uD83C\uDF83", "\uD83D\uDC7B", "\uD83D\uDC80", "\uD83C\uDF42", "\uD83C\uDF15",
            "\uD83E\uDDDB", "\uD83E\uDDDF", "\uD83D\uDD77️", "\uD83E\uDD87", "\uD83C\uDF6C"
    ));
    public static final Set<String> SLOTS = new HashSet<>(Arrays.asList(
            "\uD83E\uDD5D", "\uD83C\uDF53", "\uD83C\uDF4B", "\uD83E\uDD6D", "\uD83C\uDF51",
            "\uD83C\uDF48", "\uD83C\uDF4A", "\uD83C\uDF4D", "\uD83C\uDF50", "\uD83C\uDF47"
    ));

    @Resource(name = "philJda")
    @Lazy
    protected JDA philJda;

    @Resource
    protected DiscordUserRepository discordUserRepository;

    @Resource
    protected SwampyGamesConfigRepository swampyGamesConfigRepository;

    protected DiscordUser getDiscordUserByMember(Member member) {
        String userId = member.getId();
        Optional<DiscordUser> optionalUserEntity = discordUserRepository.findById(userId);

        if (!optionalUserEntity.isPresent()) {
            DiscordUser newUser = new DiscordUser();
            newUser.setId(userId);
            optionalUserEntity = Optional.of(discordUserRepository.save(newUser));
        }

        return optionalUserEntity.get();
    }

    protected CompletableFuture<Void> givePointsToMember(long pointsToGive, Member member, DiscordUser user) {
        if (isNotParticipating(member)) {
            return CompletableFuture.completedFuture(null);
        }

        user.setXp(user.getXp() + pointsToGive);
        user = discordUserRepository.save(user);
        return assignRolesIfNeeded(member, user).getRight();
    }

    protected CompletableFuture<Void> givePointsToMember(long pointsToGive, Member member) {
        if (isNotParticipating(member)) {
            return CompletableFuture.completedFuture(null);
        }

        return givePointsToMember(pointsToGive, member, getDiscordUserByMember(member));
    }

    protected CompletableFuture<Void> takePointsFromMember(long pointsToTake, Member member) {
        if (isNotParticipating(member)) {
            return CompletableFuture.completedFuture(null);
        }

        DiscordUser user = getDiscordUserByMember(member);
        user.setXp(Math.max(0, user.getXp() - pointsToTake));
        user = discordUserRepository.save(user);
        return assignRolesIfNeeded(member, user).getRight();
    }

    protected Pair<Role, CompletableFuture<Void>> assignRolesIfNeeded(Member member, DiscordUser user) {
        if (isNotParticipating(member)) {
            return ImmutablePair.of(null, CompletableFuture.completedFuture(null));
        }

        Rank newRank = Rank.byXp(user.getXp());
        Role newRole = member.getGuild().getRolesByName(newRank.getRoleName(), true).get(0);

        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        if (!hasRole(member, newRank)) {
            Set<String> allRoleNames = Rank.getAllRoleNames();
            Guild guild = member.getGuild();

            for (Role r : member.getRoles()) {
                if (allRoleNames.contains(r.getName())) {
                    try {
                        future = future.thenCompose(c -> guild.removeRoleFromMember(member, r).submit());
                    } catch (Exception e) {
                        logger.error("Failed to remove role {} from member {}", r.getName(), member.getEffectiveName(), e);
                    }
                }
            }

            try {
                future = future.thenCompose(c -> guild.addRoleToMember(member, newRole).submit());
            } catch (Exception e) {
                logger.error("Failed to add new role {} to member {}", newRole.getName(), member.getEffectiveName(), e);
            }

            if (newRank != Rank.CINNAMON_ROLL) {
                TextChannel announcementsChannel = member.getGuild().getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);

                MessageEmbed messageEmbed = new EmbedBuilder()
                        .setImage(newRank.getRankUpImage())
                        .setTitle("Level " + newRank.getLevel() + '!')
                        .setColor(newRole.getColor())
                        .setDescription(newRank.getRankUpMessage().replace("<name>", member.getAsMention()).replace("<rolename>", newRank.getRoleName()))
                        .build();

                future = future.thenRun(() -> announcementsChannel.sendMessage(messageEmbed).queue());
            }
        }

        return ImmutablePair.of(newRole, future);
    }

    protected MessageEmbed simpleEmbed(String title, String description) {
        return simpleEmbed(title, description, null);
    }

    protected MessageEmbed simpleEmbed(String title, String description, String image) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setImage(image)
                .setColor(Constants.COLOR_OF_THE_MONTH)
                .build();
    }

    protected boolean isNotParticipating(Member member) {
        return member.getUser().isBot() || !(hasRole(member, Constants.CHAOS_CHILDREN_ROLE) || hasRole(member, Constants.EIGHTEEN_PLUS_ROLE));
    }

    protected boolean hasRole(Member member, Rank rank) {
        return hasRole(member, rank.getRoleName());
    }

    protected boolean hasRole(Member member, String role) {
        return member.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(role));
    }

    protected <T> T pickRandom(Collection<T> collection) {
        int index = ThreadLocalRandom.current().nextInt(collection.size());
        Iterator<T> iterator = collection.iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

}
