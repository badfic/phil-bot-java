package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.DiscordUserRepository;
import com.badfic.philbot.data.phil.Rank;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.data.phil.SwampyGamesConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.Command;
import io.honeybadger.reporter.HoneybadgerReporter;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Resource;
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
import org.springframework.web.client.RestTemplate;

public abstract class BaseSwampy extends Command {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    protected static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("MMM d yyyy HH:mm");

    // sweepstakes, taxes, robinhood
    public static final BigDecimal ONE_HUNDREDTH = new BigDecimal("0.01");
    public static final long TAX_OR_ROBINHOOD_MINIMUM_POINT_THRESHOLD = 999;
    public static final long SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD = 999;

    @Resource(name = "philJda")
    @Lazy
    protected JDA philJda;

    @Resource
    protected DiscordUserRepository discordUserRepository;

    @Resource
    protected SwampyGamesConfigRepository swampyGamesConfigRepository;

    @Resource
    protected ObjectMapper objectMapper;

    @Resource
    protected BaseConfig baseConfig;

    @Resource
    protected HoneybadgerReporter honeybadgerReporter;

    @Resource
    protected RestTemplate restTemplate;

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

            if (newRank.ordinal() != 0) {
                TextChannel announcementsChannel = member.getGuild().getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);

                MessageEmbed messageEmbed = Constants.simpleEmbed("Level " + newRank.getLevel() + '!',
                        newRank.getRankUpMessage().replace("<name>", member.getAsMention()).replace("<rolename>", newRank.getRoleName()),
                        newRank.getRankUpImage(), newRole.getColor());

                future = future.thenRun(() -> announcementsChannel.sendMessage(messageEmbed).queue());
            }
        }

        return ImmutablePair.of(newRole, future);
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

    protected SwampyGamesConfig getSwampyGamesConfig() {
        return swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID).orElse(null);
    }

}
