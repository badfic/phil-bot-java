package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Robinhood extends BaseSwampy {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public Robinhood() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "robinhood";
        help = "!!robinhood\nTrigger a robinhood";
    }

    @Override
    public void execute(CommandEvent event) {
        doRobinhood(true);
    }

    @Scheduled(cron = "0 9 2 * * ?", zone = "GMT")
    public void robinhood() {
        doRobinhood(false);
    }

    private void doRobinhood(boolean force) {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        if (swampyGamesConfig == null) {
            return;
        }

        if (!force && ThreadLocalRandom.current().nextInt(100) < swampyGamesConfig.getPercentChanceRobinhoodNotHappen()) {
            MessageEmbed message = Constants.simpleEmbed(swampyGamesConfig.getRobinhoodStopperPhrase(),
                    swampyGamesConfig.getRobinhoodStopperPerson() + " caught " + swampyGamesConfig.getRobinhoodPerson()
                            + " while they were trying to return taxes to the swamp.",
                    swampyGamesConfig.getRobinhoodStoppedImg());

            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
            return;
        }

        List<DiscordUser> allUsers = discordUserRepository.findAll();
        allUsers.sort((u1, u2) -> Long.compare(u2.getXp(), u1.getXp())); // Descending sort

        long totalRecovered = 0;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder();
        for (DiscordUser user : allUsers) {
            if (user.getXp() > TAX_OR_ROBINHOOD_MINIMUM_POINT_THRESHOLD && user.getUpdateTime().isAfter(LocalDateTime.now().minusHours(22))) {
                try {
                    long taxRateRecoveryAmountPercentage = ThreadLocalRandom.current()
                            .nextInt(swampyGamesConfig.getRobinhoodMinPercent(), swampyGamesConfig.getRobinhoodMaxPercent());
                    if (user.getFamily() != null && CollectionUtils.isNotEmpty(user.getFamily().getSpouses())) {
                        taxRateRecoveryAmountPercentage -= 2;
                    }

                    taxRateRecoveryAmountPercentage = Math.max(1, taxRateRecoveryAmountPercentage); // Always make sure it's at least 1 percent.
                    long recoveredTaxes = BigDecimal.valueOf(user.getXp()).multiply(ONE_HUNDREDTH).multiply(BigDecimal.valueOf(taxRateRecoveryAmountPercentage)).longValue();
                    Member memberById = philJda.getGuilds().get(0).getMemberById(user.getId());
                    if (memberById != null && !isNotParticipating(memberById) && hasRole(memberById, Constants.EIGHTEEN_PLUS_ROLE)) {
                        futures.add(givePointsToMember(recoveredTaxes, memberById, PointsStat.ROBINHOOD));
                        totalRecovered += recoveredTaxes;

                        description
                                .append("Recovered ")
                                .append(NumberFormat.getIntegerInstance().format(recoveredTaxes))
                                .append(" points (")
                                .append(taxRateRecoveryAmountPercentage)
                                .append("%) to <@!")
                                .append(user.getId())
                                .append(">\n");
                    }
                } catch (Exception e) {
                    logger.error("Failed to robinhood user [id={}]", user.getId(), e);
                    honeybadgerReporter.reportError(e, "Failed to robinhood user: " + user.getId());
                }
            }
        }

        String footer = swampyGamesConfig.getRobinhoodPerson() +
                " recovered a total of " +
                NumberFormat.getIntegerInstance().format(totalRecovered) +
                " points and gave them back to the swamp!";

        String title = "Robinhood! The following taxes have been returned";
        MessageEmbed message = Constants.simpleEmbed(title, description.toString(), swampyGamesConfig.getRobinhoodImg(), footer);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
        });
    }

}
