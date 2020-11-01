package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.mutable.MutableLong;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Stonks extends BaseSwampy implements PhilMarker {

    private static final Set<String> STONKS = new HashSet<>(Arrays.asList(
            "https://cdn.discordapp.com/attachments/741053845098201099/771913890731655188/image0.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/771913707403083836/image0.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/771914968701599744/image0.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/771916094091952138/image0.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/771917366660694036/image0.jpg",
            "https://cdn.discordapp.com/attachments/741053845098201099/771917366904750160/image1.jpg"
    ));
    private static final long THEORETICAL_MAX = 4_000L;

    public Stonks() {
        name = "stonks";
        requiredRole = Constants.ADMIN_ROLE;
    }

    @Override
    protected void execute(CommandEvent event) {
        stonks();
    }

    @Scheduled(cron = "0 37 3 * * ?", zone = "GMT")
    public void stonks() {
        Optional<SwampyGamesConfig> optionalConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID);
        if (!optionalConfig.isPresent()) {
            return;
        }
        SwampyGamesConfig swampyGamesConfig = optionalConfig.get();

        List<DiscordUser> allUsers = discordUserRepository.findAll();

        MutableLong totalBastards = new MutableLong(0);
        MutableLong totalPointsGiven = new MutableLong(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder("Stonky? has infiltrated the swamp, giving \uD83D\uDCC8 \uD83D\uDCC8 \uD83D\uDCC8 " +
                "to all of the not-top-10 bastards!\n" +
                "There's a very complicated formula involving how many taxes were paid, but the min per user is 500 and the max is 4,000\n\n");

        allUsers.stream()
                .sorted((u1, u2) -> Long.compare(u2.getXp(), u1.getXp()))
                .filter(u -> u.getXp() > SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD && u.getUpdateTime().isAfter(LocalDateTime.now().minusHours(24)))
                .filter(u -> {
                    Member m = philJda.getGuilds().get(0).getMemberById(u.getId());
                    return m != null && !m.getUser().isBot() && hasRole(m, Constants.EIGHTEEN_PLUS_ROLE);
                })
                .skip(10)
                .peek(u -> totalBastards.increment())
                .forEachOrdered(user -> {
                    try {
                        long mostRecentTaxes = swampyGamesConfig.getMostRecentTaxes();
                        long pointsToGive = Math.min(THEORETICAL_MAX, Math.max(500, mostRecentTaxes / totalBastards.getValue()));

                        Member memberById = philJda.getGuilds().get(0).getMemberById(user.getId());
                        if (memberById != null) {
                            futures.add(givePointsToMember(pointsToGive, memberById));
                            totalPointsGiven.add(pointsToGive);
                            description
                                    .append(NumberFormat.getIntegerInstance().format(pointsToGive))
                                    .append(" \uD83D\uDCC8")
                                    .append(" for <@!")
                                    .append(user.getId())
                                    .append(">\n");
                        }
                    } catch (Exception ignored) {}
                });

        description.append("\nStonky? gave a total of ")
                .append(NumberFormat.getIntegerInstance().format(totalPointsGiven.getValue()))
                .append(" \uD83D\uDCC8 to the bastards!");

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(simpleEmbed("STONKS", description.toString(), pickRandom(STONKS)))
                    .queue();
        });
    }
}
