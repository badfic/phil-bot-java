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
public class Stonks extends BaseNormalCommand {
    // TODO: Put in SwampyGamesConfig table
    private static final String[] STONKS = {
            "https://cdn.discordapp.com/attachments/794506942906761226/882825319722143784/stonky_bellala.png",
            "https://cdn.discordapp.com/attachments/794506942906761226/882825323308269598/stonky_betty.png",
            "https://cdn.discordapp.com/attachments/718561729105363075/945846713254576239/IMG_1832.png",
            "https://cdn.discordapp.com/attachments/741030569307275436/902294248597098557/IMG_2446.jpg"
    };

    public Stonks() {
        name = "stonks";
        requiredRole = Constants.ADMIN_ROLE;
        help = "!!stonks\nTrigger a stonks";
    }

    @Override
    protected void execute(CommandEvent event) {
        stonks();
    }

    @Scheduled(cron = "${swampy.schedule.events.stonks}", zone = "${swampy.schedule.timezone}")
    public void stonks() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        List<DiscordUser> allUsers = discordUserRepository.findAll();
        Guild guild = philJda.getGuildById(baseConfig.guildId);

        MutableLong totalPointsGiven = new MutableLong(0);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder("Stonky? has infiltrated the swamp, giving \uD83D\uDCC8 \uD83D\uDCC8 \uD83D\uDCC8 " +
                "to all of the not-top-5 bastards!\n" +
                "There's a very complicated formula involving how many taxes were paid, but the min per user is 500 and the max is " +
                NumberFormat.getIntegerInstance().format(swampyGamesConfig.getStonksMaxPoints()) +
                "\n\n");

        List<DiscordUser> filteredUsers = allUsers.stream()
                .sorted((u1, u2) -> Long.compare(u2.getXp(), u1.getXp()))
                .filter(u -> u.getXp() > SWEEP_OR_TAX_WINNER_ORGANIC_POINT_THRESHOLD && u.getUpdateTime().isAfter(LocalDateTime.now().minusHours(22)))
                .filter(u -> {
                    Member m = guild.getMemberById(u.getId());
                    return m != null && hasRole(m, Constants.EIGHTEEN_PLUS_ROLE);
                })
                .skip(5)
                .collect(Collectors.toList());

        for (DiscordUser user : filteredUsers) {
            try {
                long mostRecentTaxes = swampyGamesConfig.getMostRecentTaxes();
                long pointsToGive = Math.min(swampyGamesConfig.getStonksMaxPoints(), Math.max(500, mostRecentTaxes / filteredUsers.size()));

                Member memberById = guild.getMemberById(user.getId());
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
                log.error("Failed to stonks user [id={}]", user.getId(), e);
            }
        }

        description.append("\nStonky? gave a total of ")
                .append(NumberFormat.getIntegerInstance().format(totalPointsGiven.getValue()))
                .append(" \uD83D\uDCC8 to the bastards!");

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessageEmbeds(Constants.simpleEmbed("GAMESTONKS", description.toString(), Constants.pickRandom(STONKS)))
                    .queue();
        });
    }
}
