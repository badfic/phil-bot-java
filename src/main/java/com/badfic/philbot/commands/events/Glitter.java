package com.badfic.philbot.commands.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
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
public class Glitter extends BaseNormalCommand {

    public Glitter() {
        name = "glitter";
        requiredRole = Constants.ADMIN_ROLE;
    }

    @Override
    public void execute(CommandEvent event) {
        glitter();
    }

    @Scheduled(cron = "${swampy.schedule.events.glitter}", zone = "${swampy.schedule.timezone}")
    public void glitter() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        int glitterEventPoints = swampyGamesConfig.getGlitterEventPoints();
        String glitterName = swampyGamesConfig.getGlitterName();

        List<DiscordUser> allUsers = discordUserRepository.findAll();
        Guild guild = philJda.getGuildById(baseConfig.guildId);

        MutableLong totalPointsGiven = new MutableLong(0);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder(glitterName + """
                 has infiltrated the swamp,
                giving ✨ GLITTER ✨ to every active swampling!

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
                    futures.add(givePointsToMember(glitterEventPoints, memberById, PointsStat.STONKS));
                    totalPointsGiven.add(glitterEventPoints);
                    description
                            .append(NumberFormat.getIntegerInstance().format(glitterEventPoints))
                            .append(" ✨ for <@")
                            .append(user.getId())
                            .append(">\n");
                }
            } catch (Exception e) {
                log.error("Failed to glitter [user={}]", user.getId(), e);
            }
        }

        description.append('\n')
                .append(glitterName)
                .append(" gave a total of ")
                .append(NumberFormat.getIntegerInstance().format(totalPointsGiven.getValue()))
                .append(" ✨ to the swamplings!");

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessageEmbeds(Constants.simpleEmbed("✨ GLITTER ✨", description.toString(), swampyGamesConfig.getGlitterImg()))
                    .queue();
        });
    }
}
