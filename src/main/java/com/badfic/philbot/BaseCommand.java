package com.badfic.philbot;

import com.badfic.philbot.commands.Rank;
import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.DiscordUserRepository;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.SwampyGamesConfigDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import okhttp3.OkHttpClient;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

public interface BaseCommand {

    DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("MMM d yyyy HH:mm");

    // sweepstakes, taxes, robinhood
    BigDecimal ONE_HUNDREDTH = new BigDecimal("0.01");
    long TAX_OR_ROBINHOOD_MINIMUM_POINT_THRESHOLD = 999;
    long SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD = 999;

    JDA getPhilJda();

    DiscordUserRepository getDiscordUserRepository();

    SwampyGamesConfigDao getSwampyGamesConfigDao();

    ObjectMapper getObjectMapper();

    BaseConfig getBaseConfig();

    RestTemplate getRestTemplate();

    OkHttpClient getOkHttpClient();

    ThreadPoolTaskExecutor getThreadPoolTaskExecutor();

    default DiscordUser getDiscordUserByMember(Member member) {
        String userId = member.getId();
        DiscordUserRepository discordUserRepository = getDiscordUserRepository();
        Optional<DiscordUser> optionalUserEntity = discordUserRepository.findById(userId);

        if (optionalUserEntity.isEmpty()) {
            DiscordUser newUser = new DiscordUser();
            newUser.setId(userId);
            optionalUserEntity = Optional.of(discordUserRepository.save(newUser));
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
            TextChannel swampysChannel = member.getGuild().getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);

            MessageEmbed messageEmbed = Constants.simpleEmbed("Level " + newRank.getLevel() + '!',
                    newRank.getRankUpMessage().replace("<name>", member.getAsMention()).replace("<rolename>", newRank.getRoleName()),
                    newRank.getRankUpImage(), newRank.getColor());

            return swampysChannel.sendMessageEmbeds(messageEmbed).submit();
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
        List<Role> roles = member.getRoles();

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < roles.size(); i++) {
            String roleName = roles.get(i).getName();

            if (Constants.CHAOS_CHILDREN_ROLE.equals(roleName) || Constants.EIGHTEEN_PLUS_ROLE.equals(roleName)) {
                return false;
            }
        }

        return true;
    }

    default boolean hasRole(Member member, String role) {
        List<Role> roles = member.getRoles();

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i).getName().equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    default SwampyGamesConfig getSwampyGamesConfig()  {
        return getSwampyGamesConfigDao().getSwampyGamesConfig();
    }

    default SwampyGamesConfig saveSwampyGamesConfig(SwampyGamesConfig swampyGamesConfig) {
        return getSwampyGamesConfigDao().saveSwampyGamesConfig(swampyGamesConfig);
    }

}
