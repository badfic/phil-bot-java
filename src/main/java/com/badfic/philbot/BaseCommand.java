package com.badfic.philbot;

import com.badfic.philbot.commands.Rank;
import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.DiscordUserRepository;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.SwampyGamesConfigDal;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

public interface BaseCommand {

    DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("MMM d yyyy HH:mm");

    // sweepstakes, taxes, robinhood
    BigDecimal ONE_HUNDREDTH = new BigDecimal("0.01");
    long TAX_OR_ROBINHOOD_MINIMUM_POINT_THRESHOLD = 999;
    long SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD = 999;

    GatewayDiscordClient getGatewayDiscordClient();

    DiscordUserRepository getDiscordUserRepository();

    SwampyGamesConfigDal getSwampyGamesConfigDal();

    ObjectMapper getObjectMapper();

    JdbcAggregateTemplate getJdbcAggregateTemplate();

    BaseConfig getBaseConfig();

    RestTemplate getRestTemplate();

    ThreadPoolTaskExecutor getThreadPoolTaskExecutor();

    default DiscordUser getDiscordUserByMember(Member member) {
        String userId = member.getId().asString();
        DiscordUserRepository discordUserRepository = getDiscordUserRepository();
        Optional<DiscordUser> optionalUserEntity = discordUserRepository.findById(userId);

        if (optionalUserEntity.isEmpty()) {
            DiscordUser newUser = new DiscordUser();
            newUser.setId(userId);
            optionalUserEntity = Optional.of(getJdbcAggregateTemplate().insert(newUser));
        }

        return optionalUserEntity.get();
    }

    default CompletableFuture<?> givePointsToMember(long pointsToGive, Member member, DiscordUser user, PointsStat pointsStat) {
        if (isNotParticipating(member)) {
            return CompletableFuture.completedFuture(null);
        }

        long existingXp = user.getXp();
        long newXp = Math.max(0, user.getXp() + pointsToGive);

        user.setXp(newXp);
        long pointsStatsCurrent = pointsStat.getter().applyAsLong(user);
        pointsStatsCurrent += pointsToGive;
        pointsStat.setter().accept(user, pointsStatsCurrent);
        getDiscordUserRepository().save(user);

        Rank rankZero = Rank.byXp(0);
        Rank existingRank = Rank.byXp(existingXp);
        Rank newRank = Rank.byXp(newXp);

        if (newRank != rankZero && existingRank != newRank) {
            CompletableFuture<Message> future = new CompletableFuture<>();

            getGatewayDiscordClient().getGuildChannels(Snowflake.of(getBaseConfig().guildId)).subscribe(channel -> {
                if (Constants.SWAMPYS_CHANNEL.equals(channel.getName())) {
                    EmbedCreateSpec embedCreateSpec = Constants.simpleEmbed("Level " + newRank.getLevel() + '!',
                            newRank.getRankUpMessage().replace("<name>", member.getMention()).replace("<rolename>", newRank.getRoleName()),
                            newRank.getRankUpImage(), newRank.getColor());

                    ((GuildMessageChannel) channel).createMessage(embedCreateSpec).subscribe(future::complete, future::completeExceptionally);
                }
            });

            return future;
        }

        return CompletableFuture.completedFuture(null);
    }

    default CompletableFuture<?> givePointsToMember(long pointsToGive, Member member, PointsStat pointsStat) {
        return givePointsToMember(pointsToGive, member, getDiscordUserByMember(member), pointsStat);
    }

    default CompletableFuture<?> takePointsFromMember(long pointsToTake, Member member, PointsStat pointsStat) {
        return givePointsToMember(-pointsToTake, member, pointsStat);
    }

    default CompletableFuture<?> takePointsFromMember(long pointsToTake, Member member, DiscordUser discordUser, PointsStat pointsStat) {
        return givePointsToMember(-pointsToTake, member, discordUser, pointsStat);
    }

    default boolean isNotParticipating(Member member) {
        return Boolean.TRUE.equals(member.getRoles()
                .map(Role::getName)
                .any(roleName -> Constants.CHAOS_CHILDREN_ROLE.equals(roleName) || Constants.EIGHTEEN_PLUS_ROLE.equals(roleName))
                .block());
    }

    default boolean hasRole(Member member, String role) {
        return Boolean.TRUE.equals(member.getRoles()
                .map(Role::getName)
                .any(roleName -> roleName.equalsIgnoreCase(role))
                .block());
    }

    default SwampyGamesConfig getSwampyGamesConfig()  {
        return getSwampyGamesConfigDal().get();
    }

    default SwampyGamesConfig saveSwampyGamesConfig(SwampyGamesConfig swampyGamesConfig) {
        return getSwampyGamesConfigDal().update(swampyGamesConfig);
    }

}
