package com.badfic.philbot;

import com.badfic.philbot.commands.Rank;
import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.DiscordUserRepository;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.SwampyGamesConfigDal;
import com.badfic.philbot.service.RandomNumberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

public interface BaseCommand {

    DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("MMM d yyyy HH:mm");

    JDA getPhilJda();

    DiscordUserRepository getDiscordUserRepository();

    SwampyGamesConfigDal getSwampyGamesConfigDal();

    ObjectMapper getObjectMapper();

    JdbcAggregateTemplate getJdbcAggregateTemplate();

    BaseConfig getBaseConfig();

    RestTemplate getRestTemplate();

    ExecutorService getExecutorService();

    ThreadPoolTaskScheduler getTaskScheduler();

    RandomNumberService getRandomNumberService();

    default DiscordUser getDiscordUserByMember(final Member member) {
        final var userId = member.getId();
        final var discordUserRepository = getDiscordUserRepository();
        var optionalUserEntity = discordUserRepository.findById(userId);

        if (optionalUserEntity.isEmpty()) {
            final var newUser = new DiscordUser();
            newUser.setId(userId);

            final var now = LocalDateTime.now();
            final var yesterday = now.minusDays(1);

            newUser.setUpdateTime(now);
            newUser.setLastSlots(yesterday);
            newUser.setLastMessageBonus(yesterday);
            newUser.setLastVote(yesterday);
            newUser.setAcceptedBoost(yesterday);
            newUser.setAcceptedMapTrivia(yesterday);

            optionalUserEntity = Optional.of(getJdbcAggregateTemplate().insert(newUser));
        }

        return optionalUserEntity.get();
    }

    default CompletableFuture<?> givePointsToMember(final long pointsToGive, final Member member, final DiscordUser user, final PointsStat pointsStat) {
        if (isNotParticipating(member)) {
            return CompletableFuture.completedFuture(null);
        }

        final var existingXp = user.getXp();
        final var newXp = Math.max(0, user.getXp() + pointsToGive);

        user.setXp(newXp);
        user.setUpdateTime(LocalDateTime.now());
        var pointsStatsCurrent = pointsStat.getter.applyAsLong(user);
        pointsStatsCurrent += pointsToGive;
        pointsStat.setter.accept(user, pointsStatsCurrent);
        getDiscordUserRepository().save(user);

        final var rankZero = Rank.byXp(0);
        final var existingRank = Rank.byXp(existingXp);
        final var newRank = Rank.byXp(newXp);

        if (newRank != rankZero && existingRank != newRank) {
            final var swampysChannel = member.getGuild().getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).getFirst();

            final var messageEmbed = Constants.simpleEmbed("Level " + newRank.getLevel() + " | " + newRank.getRoleName(),
                    newRank.getRankUpMessage().replace("<name>", member.getAsMention()).replace("<rolename>", newRank.getRoleName()),
                    null, null, newRank.getColor(), newRank.getRankUpImage());

            return swampysChannel.sendMessageEmbeds(messageEmbed).submit();
        }

        return CompletableFuture.completedFuture(null);
    }

    default CompletableFuture<?> givePointsToMember(final long pointsToGive, final Member member, final PointsStat pointsStat) {
        return givePointsToMember(pointsToGive, member, getDiscordUserByMember(member), pointsStat);
    }

    default CompletableFuture<?> takePointsFromMember(final long pointsToTake, final Member member, final PointsStat pointsStat) {
        return givePointsToMember(-pointsToTake, member, pointsStat);
    }

    default CompletableFuture<?> takePointsFromMember(final long pointsToTake, final Member member, final DiscordUser discordUser, final PointsStat pointsStat) {
        return givePointsToMember(-pointsToTake, member, discordUser, pointsStat);
    }

    default boolean isNotParticipating(final Member member) {
        final var roles = member.getRoles();

        for (final var role : roles) {
            final var roleName = role.getName();

            if (Constants.CHAOS_CHILDREN_ROLE.equals(roleName) || Constants.EIGHTEEN_PLUS_ROLE.equals(roleName)) {
                return false;
            }
        }

        return true;
    }

    default boolean hasRole(final Member member, final String role) {
        return member.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(role));
    }

    default SwampyGamesConfig getSwampyGamesConfig()  {
        return getSwampyGamesConfigDal().get();
    }

    default SwampyGamesConfig saveSwampyGamesConfig(final SwampyGamesConfig swampyGamesConfig) {
        return getSwampyGamesConfigDal().update(swampyGamesConfig);
    }

    default void scheduleTask(final Runnable task, final LocalDateTime executionTime) {
        getTaskScheduler().schedule(task, executionTime.toInstant(ZoneOffset.UTC));
    }

}
