package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GiveawayCommand extends BaseSwampy implements PhilMarker {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PRIZE_EMOJI = "\uD83C\uDF81";
    private static final String IMAGE = "https://cdn.discordapp.com/attachments/707453916882665552/788148747971985418/male-hand-holding-megaphone-with-giveaway-speech-bubble-loudspeaker-vector-id1197835447.png";

    public GiveawayCommand() {
        name = "giveaway";
        ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        doGiveaway();
    }

    @Scheduled(cron = "0 0 21,22 14,15,16,17,18,19,20,21,22,23,24,25 * ?")
    public void doGiveaway() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        if (swampyGamesConfig.getPastGiveawayWinners() == null) {
            swampyGamesConfig.setPastGiveawayWinners(new HashSet<>());
            swampyGamesConfig = swampyGamesConfigRepository.save(swampyGamesConfig);
        }

        if (swampyGamesConfig.getGiveawayMsgId() != 0) {
            final long messageId = swampyGamesConfig.getGiveawayMsgId();
            swampyGamesConfig.setGiveawayMsgId(0);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            philJda.getTextChannelsByName(Constants.GIVEAWAY_CHANNEL, false)
                    .get(0)
                    .retrieveMessageById(messageId)
                    .queue(msg -> {
                        msg.retrieveReactionUsers(PRIZE_EMOJI).queue(users -> {
                            SwampyGamesConfig swampyGamesConfigInner = getSwampyGamesConfig();
                            List<String> contestants = users.stream().map(ISnowflake::getId).collect(Collectors.toCollection(ArrayList::new));
                            contestants.remove(philJda.getSelfUser().getId());
                            Collections.shuffle(contestants);

                            StringBuilder description = new StringBuilder();

                            Member winningMember = null;
                            for (String userId : contestants) {
                                try {
                                    if (!swampyGamesConfigInner.getPastGiveawayWinners().contains(userId)) {
                                        Member memberById = philJda.getGuilds().get(0).getMemberById(userId);
                                        if (memberById != null
                                                && !isNotParticipating(memberById)
                                                && !hasRole(memberById, Constants.ADMIN_ROLE)
                                                && !hasRole(memberById, Constants.MOD_ROLE)) {
                                            winningMember = memberById;
                                        }
                                    }
                                } catch (Exception ignored) {}
                            }

                            if (winningMember == null) {
                                description.append("Unable to find a qualified winner today. Sorry :(");
                            } else {
                                logger.info("Giveaway winner selected [uid={}]", winningMember.getId());
                                swampyGamesConfigInner.getPastGiveawayWinners().add(winningMember.getId());
                                swampyGamesConfigRepository.save(swampyGamesConfigInner);

                                description.append("Congratulations ")
                                        .append(winningMember.getAsMention())
                                        .append(" you won today's giveaway. Please dm <@!")
                                        .append(baseConfig.ownerId)
                                        .append("> to claim your prize");
                            }

                            philJda.getTextChannelsByName(Constants.GIVEAWAY_CHANNEL, false)
                                    .get(0)
                                    .sendMessage(description.toString())
                                    .queue();
                        }, err -> {
                            if (err != null) {
                                logger.error("Error retrieving giveaway message reactions", err);
                                honeybadgerReporter.reportError(err, null, "Error retrieving giveaway message reactions");
                                MessageEmbed message = new EmbedBuilder()
                                        .setTitle("Giveaway Failed")
                                        .setDescription("Bug santiago, it failed somehow")
                                        .build();

                                philJda.getTextChannelsByName(Constants.GIVEAWAY_CHANNEL, false)
                                        .get(0)
                                        .sendMessage(message)
                                        .queue();
                            }
                        });
                    }, err -> {
                        if (err != null) {
                            logger.error("Error retrieving giveaway message", err);
                            honeybadgerReporter.reportError(err, null, "Error retrieving giveaway message");
                            MessageEmbed message = new EmbedBuilder()
                                    .setTitle("Giveaway Failed")
                                    .setDescription("Bug santiago, it failed somehow")
                                    .build();

                            philJda.getTextChannelsByName(Constants.GIVEAWAY_CHANNEL, false)
                                    .get(0)
                                    .sendMessage(message)
                                    .queue();
                        }
                    });

            return;
        }

        MessageEmbed message = Constants.simpleEmbed("Giveaway!",
                "Please react with " + PRIZE_EMOJI + " to be entered in today's giveaway.\n" +
                        "Today's prize is a $20 USD Amazon.com Gift Card\n\n" +
                        "Alternatives:\n" +
                        "• $20 USD equivalent Amazon.insert-your-country-here Gift Card\n\n" +
                        "• $20 USD equivalent in cryptocurrency (please choose one of the following: BTC/BCH/ETH/LTC)\n\n" +
                        "• $20 USD equivalent in Paypal/CashApp/Venmo\n\n" +
                        "• Login information to a US netflix account that is preloaded with a $15 USD netflix gift card",
                IMAGE,
                "Must be 13 years or older to enter. No Purchase Necessary. This is a sweepstakes, the winner will be chosen at random. " +
                        "You cannot win more than once. If you are declared winner by a logical/programming error and you have already won a previous " +
                        "sweepstakes or you are not 13 years of age or older you must forfeit your prize. " +
                        "This sweepstakes will run for 23 hours from the posting time of this message and the winner will be announced after. " +
                        "Any winner then has 24 hours to claim their prize or they must forfeit the prize.");

        philJda.getTextChannelsByName(Constants.GIVEAWAY_CHANNEL, false)
                .get(0)
                .sendMessage(message)
                .queue(msg -> {
                    msg.addReaction(PRIZE_EMOJI).queue();

                    SwampyGamesConfig swampyGamesConfigInner = getSwampyGamesConfig();
                    swampyGamesConfigInner.setGiveawayMsgId(msg.getIdLong());
                    swampyGamesConfigRepository.save(swampyGamesConfigInner);
                });
    }
}
