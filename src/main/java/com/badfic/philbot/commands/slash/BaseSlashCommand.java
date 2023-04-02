package com.badfic.philbot.commands.slash;

import com.badfic.philbot.commands.Rank;
import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.DiscordUserRepository;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.SwampyGamesConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.SlashCommand;
import io.honeybadger.reporter.HoneybadgerReporter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

public abstract class BaseSlashCommand extends SlashCommand {

    @Setter(onMethod_ = {@Autowired, @Qualifier("philJda"), @Lazy})
    protected JDA philJda;

    @Setter(onMethod_ = {@Autowired})
    protected DiscordUserRepository discordUserRepository;

    @Setter(onMethod_ = {@Autowired})
    protected SwampyGamesConfigRepository swampyGamesConfigRepository;

    @Setter(onMethod_ = {@Autowired})
    protected ObjectMapper objectMapper;

    @Setter(onMethod_ = {@Autowired})
    protected BaseConfig baseConfig;

    @Setter(onMethod_ = {@Autowired})
    protected HoneybadgerReporter honeybadgerReporter;

    @Setter(onMethod_ = {@Autowired})
    protected RestTemplate restTemplate;

    @Setter(onMethod_ = {@Autowired})
    protected OkHttpClient okHttpClient;

    @Setter(onMethod_ = {@Autowired})
    protected ThreadPoolTaskExecutor threadPoolTaskExecutor;

    protected DiscordUser getDiscordUserByMember(Member member) {
        String userId = member.getId();
        Optional<DiscordUser> optionalUserEntity = discordUserRepository.findById(userId);

        if (optionalUserEntity.isEmpty()) {
            DiscordUser newUser = new DiscordUser();
            newUser.setId(userId);
            optionalUserEntity = Optional.of(discordUserRepository.save(newUser));
        }

        return optionalUserEntity.get();
    }

    protected CompletableFuture<?> givePointsToMember(long pointsToGive, Member member, DiscordUser user, PointsStat pointsStat) {
        if (isNotParticipating(member)) {
            return CompletableFuture.completedFuture(null);
        }

        long existingXp = user.getXp();
        long newXp = Math.max(0, user.getXp() + pointsToGive);

        user.setXp(newXp);
        long pointsStatsCurrent = pointsStat.getter().applyAsLong(user);
        pointsStatsCurrent += pointsToGive;
        pointsStat.setter().accept(user, pointsStatsCurrent);
        discordUserRepository.save(user);

        Rank rankZero = Rank.byXp(0);
        Rank existingRank = Rank.byXp(existingXp);
        Rank newRank = Rank.byXp(newXp);

        if (newRank != rankZero && existingRank != newRank) {
            TextChannel announcementsChannel = member.getGuild().getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);

            MessageEmbed messageEmbed = Constants.simpleEmbed("Level " + newRank.getLevel() + '!',
                    newRank.getRankUpMessage().replace("<name>", member.getAsMention()).replace("<rolename>", newRank.getRoleName()),
                    newRank.getRankUpImage(), newRank.getColor());

            return announcementsChannel.sendMessageEmbeds(messageEmbed).submit();
        }

        return CompletableFuture.completedFuture(null);
    }

    protected CompletableFuture<?> givePointsToMember(long pointsToGive, Member member, PointsStat pointsStat) {
        if (isNotParticipating(member)) {
            return CompletableFuture.completedFuture(null);
        }

        return givePointsToMember(pointsToGive, member, getDiscordUserByMember(member), pointsStat);
    }

    protected CompletableFuture<?> takePointsFromMember(long pointsToTake, Member member, PointsStat pointsStat) {
        return givePointsToMember(-pointsToTake, member, pointsStat);
    }

    protected boolean isNotParticipating(Member member) {
        return member.getRoles().stream().noneMatch(r -> {
            String roleName = r.getName();
            return Constants.CHAOS_CHILDREN_ROLE.equalsIgnoreCase(roleName) || Constants.EIGHTEEN_PLUS_ROLE.equalsIgnoreCase(roleName);
        });
    }

    protected boolean hasRole(Member member, String role) {
        return member.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(role));
    }

    protected SwampyGamesConfig getSwampyGamesConfig() {
        return swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID).orElse(null);
    }

}
