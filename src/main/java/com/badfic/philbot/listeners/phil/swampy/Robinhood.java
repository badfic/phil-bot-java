package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Robinhood extends BaseSwampy implements PhilMarker {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Pair<Integer, Integer> ROBINHOOD_PERCENTAGE_MIN_MAX = ImmutablePair.of(5, 16);
    private static final long PERCENT_CHANCE_ROBINHOOD_DOESNT_HAPPEN = 30;
    private static final String ROBINHOOD = "https://cdn.discordapp.com/attachments/323666308107599872/761475204702535680/oprah_refund_robinhood.png";
    private static final String PERSON_WHO_STOPS_ROBINHOOD = "https://cdn.discordapp.com/attachments/323666308107599872/761477965586366484/george_lopez_stop_oprah.png";

    public Robinhood() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "robinhood";
    }

    @Override
    public void execute(CommandEvent event) {
        doRobinhood(true);
    }

    @Scheduled(cron = "0 15 2 * * ?", zone = "GMT")
    public void robinhood() {
        doRobinhood(false);
    }

    private void doRobinhood(boolean force) {
        if (!force && ThreadLocalRandom.current().nextInt(100) < PERCENT_CHANCE_ROBINHOOD_DOESNT_HAPPEN) {
            MessageEmbed message = new EmbedBuilder()
                    .setTitle("Wapa!!!")
                    .setDescription("George Lopez caught Oprah while she was trying to return taxes to the swamp.")
                    .setImage(PERSON_WHO_STOPS_ROBINHOOD)
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

        long totalRecovered = 0;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        StringBuilder description = new StringBuilder();
        for (DiscordUser user : allUsers) {
            if (user.getXp() > TAX_OR_ROBINHOOD_MINIMUM_POINT_THRESHOLD) {
                try {
                    long taxRateRecoveryAmountPercentage = ThreadLocalRandom.current().nextInt(ROBINHOOD_PERCENTAGE_MIN_MAX.getLeft(), ROBINHOOD_PERCENTAGE_MIN_MAX.getRight());
                    if (user.getFamily() != null && CollectionUtils.isNotEmpty(user.getFamily().getSpouses())) {
                        taxRateRecoveryAmountPercentage -= 2;
                    }
                    long recoveredTaxes = BigDecimal.valueOf(user.getXp()).multiply(ONE_HUNDREDTH).multiply(BigDecimal.valueOf(taxRateRecoveryAmountPercentage)).longValue();
                    totalRecovered += recoveredTaxes;
                    Member memberById = philJda.getGuilds().get(0).getMemberById(user.getId());
                    if (memberById != null && !memberById.getUser().isBot() && hasRole(memberById, Constants.EIGHTEEN_PLUS_ROLE)) {
                        futures.add(givePointsToMember(recoveredTaxes, memberById));

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
                }
            }
        }

        description.append("\nI recovered a total of ")
                .append(NumberFormat.getIntegerInstance().format(totalRecovered))
                .append(" points and gave them back to the swamp!");

        String title = "Robinhood! The following taxes have been returned";
        MessageEmbed message = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description.toString())
                .setImage(ROBINHOOD)
                .setColor(Constants.HALOWEEN_ORANGE)
                .build();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
        });
    }

}
