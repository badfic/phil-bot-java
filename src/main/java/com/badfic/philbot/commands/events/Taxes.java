package com.badfic.philbot.commands.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Taxes extends BaseNormalCommand {

    public Taxes() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "taxes";
        help = "!!taxes\nManually trigger taxes";
    }

    @Override
    public void execute(CommandEvent event) {
        doTaxes(true);
    }

    @Scheduled(cron = "${swampy.schedule.events.taxes}", zone = "${swampy.schedule.timezone}")
    public void taxes() {
        doTaxes(false);
    }

    private void doTaxes(boolean force) {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        if (!force && ThreadLocalRandom.current().nextInt(100) < swampyGamesConfig.getPercentChanceTaxesDoesNotHappen()) {
            MessageEmbed message = Constants.simpleEmbed(
                    swampyGamesConfig.getNoTaxesPhrase(), swampyGamesConfig.getNoTaxesPerson() + " caught "
                            + swampyGamesConfig.getTaxesPerson() + " before they could take taxes from the swamp.",
                    swampyGamesConfig.getNoTaxesImage());

            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessageEmbeds(message)
                    .queue();
            return;
        }

        List<DiscordUser> allUsers = discordUserRepository.findAll();
        allUsers.sort((u1, u2) -> Long.compare(u2.getXp(), u1.getXp())); // Descending sort

        long totalTaxes = 0;
        List<CompletableFuture<?>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder();
        Guild guild = philJda.getGuildById(baseConfig.guildId);
        for (DiscordUser user : allUsers) {
            if (user.getXp() > TAX_OR_ROBINHOOD_MINIMUM_POINT_THRESHOLD && user.getUpdateTime().isAfter(LocalDateTime.now().minusHours(5))) {
                try {
                    long taxRate = ThreadLocalRandom.current().nextInt(swampyGamesConfig.getTaxesMinimumPercent(), swampyGamesConfig.getTaxesMaximumPercent());
                    taxRate = Math.max(1, taxRate); // Always make sure it's at least 1 percent.
                    long taxes = BigDecimal.valueOf(user.getXp()).multiply(ONE_HUNDREDTH).multiply(BigDecimal.valueOf(taxRate)).longValue();
                    Member memberById = guild.getMemberById(user.getId());
                    if (memberById != null
                            && !isNotParticipating(memberById)
                            && hasRole(memberById, Constants.EIGHTEEN_PLUS_ROLE)) {
                        futures.add(takePointsFromMember(taxes, memberById, PointsStat.TAXES));
                        totalTaxes += taxes;

                        description
                                .append("Collected ")
                                .append(NumberFormat.getIntegerInstance().format(taxes))
                                .append(" points (")
                                .append(taxRate)
                                .append("%) from <@")
                                .append(user.getId())
                                .append(">\n");
                    }
                } catch (Exception e) {
                    log.error("Failed to tax user [id={}]", user.getId(), e);
                }
            }
        }

        swampyGamesConfig.setMostRecentTaxes(totalTaxes);
        swampyGamesConfig = saveSwampyGamesConfig(swampyGamesConfig);

        MessageEmbed message = Constants.simpleEmbed("Tax time! " + NumberFormat.getIntegerInstance().format(totalTaxes)
                        + " points in taxes have been paid to " + swampyGamesConfig.getTaxesPerson(),
                description.toString(), swampyGamesConfig.getTaxesImage());

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessageEmbeds(message)
                    .queue();
        });
    }

}
