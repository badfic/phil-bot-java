package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
public class Taxes extends BaseSwampy implements PhilMarker {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public Taxes() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "taxes";
        help = "!!taxes\nManually trigger taxes";
    }

    @Override
    public void execute(CommandEvent event) {
        doTaxes(true);
    }

    @Scheduled(cron = "0 15 2 * * ?", zone = "GMT")
    public void taxes() {
        doTaxes(false);
    }

    private void doTaxes(boolean force) {
        Optional<SwampyGamesConfig> optionalConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID);
        if (!optionalConfig.isPresent()) {
            return;
        }
        SwampyGamesConfig swampyGamesConfig = optionalConfig.get();

        if (!force && ThreadLocalRandom.current().nextInt(100) < swampyGamesConfig.getPercentChanceTaxesNotHappen()) {
            MessageEmbed message = Constants.simpleEmbed(
                    swampyGamesConfig.getTaxesStopperPhrase(), swampyGamesConfig.getTaxesStopperPerson() + " caught "
                            + swampyGamesConfig.getTaxesPerson() + " before they could take taxes from the swamp.",
                    swampyGamesConfig.getTaxesStoppedImg());

            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
            return;
        }

        List<DiscordUser> allUsers = discordUserRepository.findAll();
        allUsers.sort((u1, u2) -> Long.compare(u2.getXp(), u1.getXp())); // Descending sort

        long totalTaxes = 0;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder();
        for (DiscordUser user : allUsers) {
            if (user.getXp() > TAX_OR_ROBINHOOD_MINIMUM_POINT_THRESHOLD) {
                try {
                    long taxRate = ThreadLocalRandom.current().nextInt(swampyGamesConfig.getTaxesMinPercent(), swampyGamesConfig.getTaxesMaxPercent());
                    if (user.getFamily() != null && CollectionUtils.isNotEmpty(user.getFamily().getSpouses())) {
                        taxRate -= 2;
                    }
                    taxRate = Math.max(1, taxRate); // Always make sure it's at least 1 percent.
                    long taxes = BigDecimal.valueOf(user.getXp()).multiply(ONE_HUNDREDTH).multiply(BigDecimal.valueOf(taxRate)).longValue();
                    Member memberById = philJda.getGuilds().get(0).getMemberById(user.getId());
                    if (memberById != null && !isNotParticipating(memberById) && hasRole(memberById, Constants.EIGHTEEN_PLUS_ROLE)) {
                        futures.add(takePointsFromMember(taxes, memberById));
                        totalTaxes += taxes;

                        description
                                .append("Collected ")
                                .append(NumberFormat.getIntegerInstance().format(taxes))
                                .append(" points (")
                                .append(taxRate)
                                .append("%) from <@!")
                                .append(user.getId())
                                .append(">\n");
                    }
                } catch (Exception e) {
                    logger.error("Failed to tax user [id={}]", user.getId(), e);
                    honeybadgerReporter.reportError(e, "Failed to tax user: " + user.getId());
                }
            }
        }

        swampyGamesConfig.setMostRecentTaxes(totalTaxes);
        swampyGamesConfigRepository.save(swampyGamesConfig);

        MessageEmbed message = Constants.simpleEmbed("Tax time! " + NumberFormat.getIntegerInstance().format(totalTaxes)
                        + " points in taxes have been paid to " + swampyGamesConfig.getTaxesPerson(),
                description.toString(), swampyGamesConfig.getTaxesImg());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
        });
    }

}
