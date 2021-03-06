package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.google.common.collect.ImmutableSet;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Stonks extends BaseSwampy {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // TODO: Put in SwampyGamesConfig table
    private static final Set<String> STONKS = ImmutableSet.of(
            "https://cdn.discordapp.com/attachments/741053845098201099/771916094091952138/image0.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/771917366660694036/image0.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/771917366904750160/image1.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/780575961958711326/image0.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/780576653298237440/image0.jpg",
            "https://cdn.discordapp.com/attachments/783445794568142879/805580386172141608/7fv2my960qd61.png"
    );

    public Stonks() {
        name = "stonks";
        requiredRole = Constants.ADMIN_ROLE;
        help = "!!stonks\nTrigger a stonks";
    }

    @Override
    protected void execute(CommandEvent event) {
        stonks();
    }

    @Scheduled(cron = "0 36 2 * * ?", zone = "GMT")
    public void stonks() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        List<DiscordUser> allUsers = discordUserRepository.findAll();

        MutableLong totalPointsGiven = new MutableLong(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder("Stonky? has infiltrated the swamp, giving \uD83D\uDCC8 \uD83D\uDCC8 \uD83D\uDCC8 " +
                "to all of the not-top-10 bastards!\n" +
                "There's a very complicated formula involving how many taxes were paid, but the min per user is 500 and the max is " +
                NumberFormat.getIntegerInstance().format(swampyGamesConfig.getStonksMaxPoints()) +
                "\n\n");

        List<DiscordUser> filteredUsers = allUsers.stream()
                .sorted((u1, u2) -> Long.compare(u2.getXp(), u1.getXp()))
                .filter(u -> u.getXp() > SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD && u.getUpdateTime().isAfter(LocalDateTime.now().minusHours(22)))
                .filter(u -> {
                    Member m = philJda.getGuilds().get(0).getMemberById(u.getId());
                    return m != null && !m.getUser().isBot() && hasRole(m, Constants.EIGHTEEN_PLUS_ROLE);
                })
                .skip(10)
                .collect(Collectors.toList());

        for (DiscordUser user : filteredUsers) {
            try {
                long mostRecentTaxes = swampyGamesConfig.getMostRecentTaxes();
                long pointsToGive = Math.min(swampyGamesConfig.getStonksMaxPoints(), Math.max(500, mostRecentTaxes / filteredUsers.size()));

                Member memberById = philJda.getGuilds().get(0).getMemberById(user.getId());
                if (memberById != null) {
                    futures.add(givePointsToMember(pointsToGive, memberById, PointsStat.STONKS));
                    totalPointsGiven.add(pointsToGive);
                    description
                            .append(NumberFormat.getIntegerInstance().format(pointsToGive))
                            .append(" \uD83D\uDCC8")
                            .append(" for <@!")
                            .append(user.getId())
                            .append(">\n");
                }
            } catch (Exception e) {
                logger.error("Failed to stonks user [id={}]", user.getId(), e);
                honeybadgerReporter.reportError(e, "Failed to stonks user: " + user.getId());
            }
        }

        description.append("\nStonky? gave a total of ")
                .append(NumberFormat.getIntegerInstance().format(totalPointsGiven.getValue()))
                .append(" \uD83D\uDCC8 to the bastards!");

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(Constants.simpleEmbed("GAMESTONKS", description.toString(), Constants.pickRandom(STONKS)))
                    .queue();
        });
    }
}
