package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.google.common.collect.ImmutableSet;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Stonks extends BaseSwampy implements PhilMarker {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // TODO: Put in SwampyGamesConfig table
    private static final Set<String> STONKS = ImmutableSet.of(
            "https://cdn.discordapp.com/attachments/741053845098201099/771916094091952138/image0.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/771917366660694036/image0.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/771917366904750160/image1.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/780575961958711326/image0.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/780576653298237440/image0.jpg"
    );

    public Stonks() {
        name = "stonks";
        requiredRole = Constants.ADMIN_ROLE;
        help = "!!stonks\nTrigger a stonks for the losing house";
    }

    @Override
    protected void execute(CommandEvent event) {
        stonks();
    }

    @Scheduled(cron = "0 36 2 * * ?", zone = "GMT")
    public void stonks() {
        Optional<SwampyGamesConfig> optionalConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID);
        if (!optionalConfig.isPresent()) {
            return;
        }
        SwampyGamesConfig swampyGamesConfig = optionalConfig.get();

        List<DiscordUser> swampyUsers = discordUserRepository.findAll();
        long totalEighteenPlus = 0;
        long dryBastards = 0;
        long dryCinnamons = 0;
        long swampyBastards = 0;
        long swampyCinnamons = 0;

        for (DiscordUser swampyUser : swampyUsers) {
            Member memberById = philJda.getGuilds().get(0).getMemberById(swampyUser.getId());

            if (memberById != null && !isNotParticipating(memberById)) {
                if (hasRole(memberById, Constants.EIGHTEEN_PLUS_ROLE)) {
                    totalEighteenPlus++;
                }

                if (hasRole(memberById, Constants.DRY_BASTARDS_ROLE)) {
                    dryBastards += swampyUser.getXp();
                }
                if (hasRole(memberById, Constants.DRY_CINNAMON_ROLE)) {
                    dryCinnamons += swampyUser.getXp();
                }
                if (hasRole(memberById, Constants.SWAMPY_BASTARDS_ROLE)) {
                    swampyBastards += swampyUser.getXp();
                }
                if (hasRole(memberById, Constants.SWAMPY_CINNAMON_ROLE)) {
                    swampyCinnamons += swampyUser.getXp();
                }
            }
        }

        String losingRole;
        if (dryBastards <= dryCinnamons && dryBastards <= swampyBastards && dryBastards <= swampyCinnamons) {
            losingRole = Constants.DRY_BASTARDS_ROLE;
        } else if (dryCinnamons <= dryBastards && dryCinnamons <= swampyBastards && dryCinnamons <= swampyCinnamons) {
            losingRole = Constants.DRY_CINNAMON_ROLE;
        } else if (swampyBastards <= dryBastards && swampyBastards <= dryCinnamons && swampyBastards <= swampyCinnamons) {
            losingRole = Constants.SWAMPY_BASTARDS_ROLE;
        } else {
            losingRole = Constants.SWAMPY_CINNAMON_ROLE;
        }

        StringBuilder description = new StringBuilder("Stonky? has infiltrated the swamp, giving \uD83D\uDCC8 \uD83D\uDCC8 \uD83D\uDCC8 " +
                "to the bastards of the house with the least points!\n" +
                "There's a very complicated formula involving how many taxes were paid, but the min per user is 500 and the max is 4,000\n\n");

        MutableLong totalPointsGiven = new MutableLong(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (DiscordUser user : swampyUsers) {
            Member memberById = philJda.getGuilds().get(0).getMemberById(user.getId());

            if (memberById != null && !isNotParticipating(memberById) && hasRole(memberById, losingRole) && hasRole(memberById, Constants.EIGHTEEN_PLUS_ROLE)) {
                try {
                    long mostRecentTaxes = swampyGamesConfig.getMostRecentTaxes();
                    long pointsToGive = Math.min(swampyGamesConfig.getStonksMaxPoints(), Math.max(500, mostRecentTaxes / totalEighteenPlus));

                    futures.add(givePointsToMember(pointsToGive, memberById));
                    totalPointsGiven.add(pointsToGive);
                    description
                            .append(NumberFormat.getIntegerInstance().format(pointsToGive))
                            .append(" \uD83D\uDCC8")
                            .append(" for <@!")
                            .append(user.getId())
                            .append(">\n");
                } catch (Exception e) {
                    logger.error("Failed to stonks user [id={}]", user.getId(), e);
                    honeybadgerReporter.reportError(e, "Failed to stonks user: " + user.getId());
                }
            }
        }

        description.append("\nStonky? gave a total of ")
                .append(NumberFormat.getIntegerInstance().format(totalPointsGiven.getValue()))
                .append(" \uD83D\uDCC8 to the ")
                .append(losingRole)
                .append('!');

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(Constants.simpleEmbed("STONKS", description.toString(), Constants.pickRandom(STONKS)))
                    .queue();
        });
    }
}
