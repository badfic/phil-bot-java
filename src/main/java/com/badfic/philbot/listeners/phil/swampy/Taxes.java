package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Taxes extends BaseSwampy implements PhilMarker {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final Pair<Integer, Integer> TAX_PERCENTAGE_MIN_MAX = ImmutablePair.of(5, 16);
    public static final long PERCENT_CHANCE_TAXES_DOESNT_HAPPEN = 30;
    public static final String TAXES = "https://cdn.discordapp.com/attachments/323666308107599872/761472008734244864/martha_taxes.png";
    public static final String PERSON_WHO_STOPS_TAXES = "https://cdn.discordapp.com/attachments/323666308107599872/761473370604568606/snoop_no_taxes.png";

    public Taxes() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "taxes";
    }

    @Override
    public void execute(CommandEvent event) {
        doTaxes(true);
    }

    @Scheduled(cron = "0 9 2 * * ?", zone = "GMT")
    public void taxes() {
        doTaxes(false);
    }

    private void doTaxes(boolean force) {
        if (!force && ThreadLocalRandom.current().nextInt(100) < PERCENT_CHANCE_TAXES_DOESNT_HAPPEN) {
            MessageEmbed message = new EmbedBuilder()
                    .setTitle("No taxes today!")
                    .setDescription("Snoop Dogg caught Martha Stewart before she could take taxes from the swamp.")
                    .setImage(PERSON_WHO_STOPS_TAXES)
                    .setColor(Constants.HALOWEEN_ORANGE)
                    .build();

            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
            return;
        }

        List<DiscordUser> allUsers = discordUserRepository.findAll();
        allUsers.sort((u1, u2) -> Long.compare(u2.getXp(), u1.getXp())); // Descending sort

        long totalTaxes = 0;
        StringBuilder description = new StringBuilder();
        for (DiscordUser user : allUsers) {
            if (user.getXp() > TAX_OR_ROBINHOOD_MINIMUM_POINT_THRESHOLD) {
                try {
                    long taxRate = ThreadLocalRandom.current().nextInt(TAX_PERCENTAGE_MIN_MAX.getLeft(), TAX_PERCENTAGE_MIN_MAX.getRight());
                    long taxes = BigDecimal.valueOf(user.getXp()).multiply(ONE_HUNDREDTH).multiply(BigDecimal.valueOf(taxRate)).longValue();
                    totalTaxes += taxes;
                    Member memberById = philJda.getGuilds().get(0).retrieveMemberById(user.getId()).complete();
                    if (memberById != null && !memberById.getUser().isBot() && hasRole(memberById, Constants.EIGHTEEN_PLUS_ROLE)) {
                        takePointsFromMember(taxes, memberById);

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
                }
            }
        }

        MessageEmbed message = new EmbedBuilder()
                .setTitle("Tax time! " + NumberFormat.getIntegerInstance().format(totalTaxes) + " points in taxes have been paid to Martha Stewart")
                .setDescription(description.toString())
                .setImage(TAXES)
                .setColor(Constants.HALOWEEN_ORANGE)
                .build();

        philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                .get(0)
                .sendMessage(message)
                .queue();
    }

}
