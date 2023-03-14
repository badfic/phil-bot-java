package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
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
public class Carrot extends BaseSwampy {

    public Carrot() {
        name = "carrot";
        aliases = new String[] {"carrots"};
        requiredRole = Constants.ADMIN_ROLE;
    }

    @Override
    protected void execute(CommandEvent event) {
        carrot();
    }

    @Scheduled(cron = "${swampy.schedule.events.carrot}", zone = "${swampy.schedule.timezone}")
    public void carrot() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        int carrotEventPoints = swampyGamesConfig.getCarrotEventPoints();
        String carrotName = swampyGamesConfig.getCarrotName();

        List<DiscordUser> allUsers = discordUserRepository.findAll();
        Guild guild = philJda.getGuildById(baseConfig.guildId);

        MutableLong totalPointsGiven = new MutableLong(0);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder(carrotName + """
                 has infiltrated the swamp,
                giving \uD83E\uDD55 \uD83E\uDD55 \uD83E\uDD55 to every active swampling!

                """);

        List<DiscordUser> filteredUsers = allUsers.stream()
                .sorted((u1, u2) -> Long.compare(u2.getXp(), u1.getXp()))
                .filter(u -> u.getXp() > SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD && u.getUpdateTime().isAfter(LocalDateTime.now().minusHours(2)))
                .filter(u -> {
                    Member m = guild.getMemberById(u.getId());
                    return m != null;
                })
                .collect(Collectors.toList());

        for (DiscordUser user : filteredUsers) {
            try {
                Member memberById = guild.getMemberById(user.getId());
                if (memberById != null) {
                    futures.add(givePointsToMember(carrotEventPoints, memberById, PointsStat.STONKS));
                    totalPointsGiven.add(carrotEventPoints);
                    description
                            .append(NumberFormat.getIntegerInstance().format(carrotEventPoints))
                            .append(" \uD83E\uDD55 for <@!")
                            .append(user.getId())
                            .append(">\n");
                }
            } catch (Exception e) {
                honeybadgerReporter.reportError(e, "Failed to carrot user: " + user.getId());
            }
        }

        description.append('\n')
                .append(carrotName)
                .append(" gave a total of ")
                .append(NumberFormat.getIntegerInstance().format(totalPointsGiven.getValue()))
                .append(" \uD83E\uDD55 to the swamplings!");

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessageEmbeds(Constants.simpleEmbed("CARROTS!", description.toString(), swampyGamesConfig.getCarrotImg()))
                    .queue();
        });
    }
}
