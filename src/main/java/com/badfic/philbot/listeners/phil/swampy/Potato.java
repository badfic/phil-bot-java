package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.mutable.MutableLong;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Potato extends BaseSwampy {

    private static final int POINTS_TO_GIVE = 1000;
    private static final String IMAGE = "https://cdn.discordapp.com/attachments/794506942906761226/882828802001096754/potato_man.png";

    public Potato() {
        name = "potato";
        aliases = new String[] {"potatoes"};
        requiredRole = Constants.ADMIN_ROLE;
    }

    @Override
    protected void execute(CommandEvent event) {
        potato();
    }

    @Scheduled(cron = "0 5 13 * * ?", zone = "America/Los_Angeles")
    public void potato() {
        List<DiscordUser> allUsers = discordUserRepository.findAll();
        Guild guild = philJda.getGuilds().get(0);

        MutableLong totalPointsGiven = new MutableLong(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder("""
                Tommy Lee Sebastian has infiltrated the swamp,
                giving \uD83E\uDD54 \uD83E\uDD54 \uD83E\uDD54 to every active swampling!

                """);

        List<DiscordUser> filteredUsers = allUsers.stream()
                .sorted((u1, u2) -> Long.compare(u2.getXp(), u1.getXp()))
                .filter(u -> u.getXp() > SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD && u.getUpdateTime().isAfter(LocalDateTime.now().minusHours(2)))
                .filter(u -> {
                    Member m = guild.getMemberById(u.getId());
                    return m != null && !m.getUser().isBot();
                })
                .collect(Collectors.toList());

        for (DiscordUser user : filteredUsers) {
            try {
                Member memberById = guild.getMemberById(user.getId());
                if (memberById != null) {
                    futures.add(givePointsToMember(POINTS_TO_GIVE, memberById, PointsStat.STONKS));
                    totalPointsGiven.add(POINTS_TO_GIVE);
                    description
                            .append(NumberFormat.getIntegerInstance().format(POINTS_TO_GIVE))
                            .append(" \uD83E\uDD54")
                            .append(" for <@!")
                            .append(user.getId())
                            .append(">\n");
                }
            } catch (Exception e) {
                honeybadgerReporter.reportError(e, "Failed to potato user: " + user.getId());
            }
        }

        description.append("\nTommy Lee Sebastian gave a total of ")
                .append(NumberFormat.getIntegerInstance().format(totalPointsGiven.getValue()))
                .append(" \uD83E\uDD54 to the swamplings!");

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessageEmbeds(Constants.simpleEmbed("GET MASHED!", description.toString(), IMAGE))
                    .queue();
        });
    }
}
