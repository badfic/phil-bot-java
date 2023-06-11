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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Robinhood extends BaseNormalCommand {
    public Robinhood() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "robinhood";
        help = "!!robinhood\nTrigger a robinhood";
    }

    @Override
    public void execute(CommandEvent event) {
        doRobinhood(true);
    }

    @Scheduled(cron = "${swampy.schedule.events.robinhood}", zone = "${swampy.schedule.timezone}")
    public void robinhood() {
        doRobinhood(false);
    }

    private void doRobinhood(boolean force) {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        Guild guild = philJda.getGuildById(baseConfig.guildId);

        if (!force && ThreadLocalRandom.current().nextInt(100) < swampyGamesConfig.getPercentChanceRefundDoesNotHappen()) {
            MessageEmbed message = Constants.simpleEmbed(swampyGamesConfig.getNoRefundPhrase(),
                    swampyGamesConfig.getNoRefundPerson() + " caught " + swampyGamesConfig.getRefundPerson()
                            + " while they were trying to return taxes to the swamp.",
                    swampyGamesConfig.getNoRefundImage());

            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessageEmbeds(message)
                    .queue();
            return;
        }

        List<DiscordUser> allUsers = discordUserRepository.findAll();
        allUsers.sort((u1, u2) -> Long.compare(u2.getXp(), u1.getXp())); // Descending sort

        long totalRecovered = 0;
        List<CompletableFuture<?>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder();
        for (DiscordUser user : allUsers) {
            if (user.getXp() > TAX_OR_ROBINHOOD_MINIMUM_POINT_THRESHOLD && user.getUpdateTime().isAfter(LocalDateTime.now().minusHours(5))) {
                try {
                    long taxRateRecoveryAmountPercentage = ThreadLocalRandom.current()
                            .nextInt(swampyGamesConfig.getRefundMinimumPercent(), swampyGamesConfig.getRefundMaximumPercent());
                    if (user.getFamily() != null && CollectionUtils.isNotEmpty(user.getFamily().getSpouses())) {
                        taxRateRecoveryAmountPercentage -= 2;
                    }

                    taxRateRecoveryAmountPercentage = Math.max(1, taxRateRecoveryAmountPercentage); // Always make sure it's at least 1 percent.
                    long recoveredTaxes = BigDecimal.valueOf(user.getXp()).multiply(ONE_HUNDREDTH).multiply(BigDecimal.valueOf(taxRateRecoveryAmountPercentage)).longValue();
                    Member memberById = guild.getMemberById(user.getId());
                    if (memberById != null
                            && !memberById.getUser().isBot()
                            && !isNotParticipating(memberById)
                            && hasRole(memberById, Constants.EIGHTEEN_PLUS_ROLE)) {
                        futures.add(givePointsToMember(recoveredTaxes, memberById, PointsStat.ROBINHOOD));
                        totalRecovered += recoveredTaxes;

                        description
                                .append("Recovered ")
                                .append(NumberFormat.getIntegerInstance().format(recoveredTaxes))
                                .append(" points (")
                                .append(taxRateRecoveryAmountPercentage)
                                .append("%) to <@")
                                .append(user.getId())
                                .append(">\n");
                    }
                } catch (Exception e) {
                    log.error("Failed to robinhood user [id={}]", user.getId(), e);
                }
            }
        }

        String footer = swampyGamesConfig.getRefundPerson() +
                " recovered a total of " +
                NumberFormat.getIntegerInstance().format(totalRecovered) +
                " points and gave them back to the swamp!";

        String title = "Robinhood! The following taxes have been returned";
        MessageEmbed message = Constants.simpleEmbed(title, description.toString(), swampyGamesConfig.getRefundImage(), footer);

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessageEmbeds(message)
                    .queue();
        });
    }

}
