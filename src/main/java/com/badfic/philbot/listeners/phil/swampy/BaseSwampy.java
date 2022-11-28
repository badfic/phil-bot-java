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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;

public abstract class BaseSwampy extends Command {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    protected static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("MMM d yyyy HH:mm");

    // sweepstakes, taxes, robinhood
    public static final BigDecimal ONE_HUNDREDTH = new BigDecimal("0.01");
    public static final long TAX_OR_ROBINHOOD_MINIMUM_POINT_THRESHOLD = 999;
    public static final long SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD = 999;

    @Autowired
    @Qualifier("philJda")
    @Lazy
    protected JDA philJda;

    @Autowired
    @Qualifier("antoniaJda")
    @Lazy
    protected JDA antoniaJda;

    @Autowired
    protected DiscordUserRepository discordUserRepository;

    @Autowired
    protected SwampyGamesConfigRepository swampyGamesConfigRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected BaseConfig baseConfig;

    @Autowired
    protected HoneybadgerReporter honeybadgerReporter;

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected OkHttpClient okHttpClient;

    @Autowired
    protected ExecutorService userTriggeredTasksExecutor;

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
