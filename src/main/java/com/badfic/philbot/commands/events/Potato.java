package com.badfic.philbot.commands.events;

import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.mutable.MutableLong;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Potato extends BaseNormalCommand {

    public Potato() {
        name = "potato";
        aliases = new String[] {"potatoes"};
        requiredRole = Constants.ADMIN_ROLE;
    }

    @Override
    protected void execute(CommandEvent event) {
        potato();
    }

    @Scheduled(cron = "${swampy.schedule.events.potato}", zone = "${swampy.schedule.timezone}")
    public void potato() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        int potatoEventPoints = swampyGamesConfig.getPotatoEventPoints();
        String potatoName = swampyGamesConfig.getPotatoName();

        List<DiscordUser> allUsers = discordUserRepository.findAll();
        Guild guild = philJda.getGuildById(baseConfig.guildId);

        MutableLong totalPointsGiven = new MutableLong(0);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder(potatoName + """
                 has infiltrated the swamp,
                giving \uD83E\uDD54 \uD83E\uDD54 \uD83E\uDD54 to every active swampling!

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
                    futures.add(givePointsToMember(potatoEventPoints, memberById, PointsStat.STONKS));
                    totalPointsGiven.add(potatoEventPoints);
                    description
                            .append(NumberFormat.getIntegerInstance().format(potatoEventPoints))
                            .append(" \uD83E\uDD54 for <@!")
                            .append(user.getId())
                            .append(">\n");
                }
            } catch (Exception e) {
                log.error("Failed to potato [user={}]", user.getId(), e);
            }
        }

        description.append('\n')
                .append(potatoName)
                .append(" gave a total of ")
                .append(NumberFormat.getIntegerInstance().format(totalPointsGiven.getValue()))
                .append(" \uD83E\uDD54 to the swamplings!");

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessageEmbeds(Constants.simpleEmbed("Potatoes!", description.toString(), swampyGamesConfig.getPotatoImg()))
                    .queue();
        });
    }
}
