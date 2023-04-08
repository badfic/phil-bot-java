package com.badfic.philbot.commands.slash;

import com.badfic.philbot.commands.swampy.SwampyCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.PointsStat;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Service;

@Service
public class SlotsSlashCommand extends BaseSlashCommand {

    public SlotsSlashCommand() {
        name = "slots";
        help = "Play slots to win swampy points!";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        CompletableFuture<InteractionHook> interactionHook = event.deferReply().submit();
        
        Member member = event.getMember();
        DiscordUser discordUser = getDiscordUserByMember(member);

        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextSlotsTime = discordUser.getLastSlots().plus(swampyGamesConfig.getSlotsTimeoutMinutes(), ChronoUnit.MINUTES);
        if (now.isBefore(nextSlotsTime)) {
            Duration duration = Duration.between(now, nextSlotsTime);

            if (duration.getSeconds() < 60) {
                replyToInteractionHook(event, interactionHook, "You must wait " + (duration.getSeconds() + 1) + " seconds before playing slots again");
            } else {
                replyToInteractionHook(event, interactionHook, "You must wait " + (duration.toMinutes() + 1) + " minutes before playing slots again");
            }
            return;
        }

        discordUser.setLastSlots(now);

        Set<String> slotsEmojis = swampyGamesConfig.getSlotsEmoji();
        int size = slotsEmojis.size();

        double oddsClosEnough = (1.0 / size) * (1.0 / size) * 300.0;
        double oddsWinnerWinner = (1.0 / size) * (1.0 / size) * (1.0 / size) * 100.0;
        String footer = String.format("Odds of a WINNER WINNER: %.3f%%\nOdds of a CLOSE ENOUGH: %.3f%%", oddsWinnerWinner, oddsClosEnough);

        String one = Constants.pickRandom(slotsEmojis);
        String two = Constants.pickRandom(slotsEmojis);
        String three = Constants.pickRandom(slotsEmojis);

        MessageEmbed winnerWinnerMessage = Constants.simpleEmbed(SwampyCommand.SLOT_MACHINE + " WINNER WINNER!! " + SwampyCommand.SLOT_MACHINE,
                String.format("%s\n%s%s%s \nYou won " + swampyGamesConfig.getSlotsWinPoints() + " points!", member.getAsMention(), one, two, three),
                null, footer);

        MessageEmbed closeEnoughMessage = Constants.simpleEmbed(SwampyCommand.SLOT_MACHINE + " CLOSE ENOUGH! " + SwampyCommand.SLOT_MACHINE,
                String.format("%s\n%s%s%s \nYou got 2 out of 3! You won " + swampyGamesConfig.getSlotsTwoOfThreePoints() + " points!",
                        member.getAsMention(), one, two, three),
                null, footer);

        MessageEmbed lossMessage = Constants.simpleEmbed(SwampyCommand.SLOT_MACHINE + " Better luck next time! " + SwampyCommand.SLOT_MACHINE,
                String.format("%s\n%s%s%s", member.getAsMention(), one, two, three), null, footer);

        if (one.equals(two) && two.equals(three)) {
            givePointsToMember(swampyGamesConfig.getSlotsWinPoints(), member, discordUser, PointsStat.SLOTS_WINNER_WINNER).thenRun(() -> {
                replyToInteractionHook(event, interactionHook, winnerWinnerMessage);

                philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).stream().findAny().ifPresent(swampysChannel -> {
                    swampysChannel.sendMessageEmbeds(Constants.simpleEmbedThumbnail("SLOTS WINNER! " + one + one + one,
                            member.getAsMention()
                                    + " just won "
                                    + NumberFormat.getIntegerInstance().format(swampyGamesConfig.getSlotsWinPoints())
                                    + " points from swampy slots! You too can win, play over in #bot-space",
                            "https://cdn.discordapp.com/attachments/707453916882665552/864307474970443816/tenor.gif",
                            member.getEffectiveAvatarUrl())).queue();
                });
            });
        } else if (one.equals(two) || one.equals(three) || two.equals(three)) {
            givePointsToMember(swampyGamesConfig.getSlotsTwoOfThreePoints(), member, discordUser, PointsStat.SLOTS_CLOSE_ENOUGH).thenRun(() -> {
                replyToInteractionHook(event, interactionHook, closeEnoughMessage);
            });
        } else {
            givePointsToMember(1, member, discordUser, PointsStat.SLOTS_LOSSES).thenRun(() -> {
                replyToInteractionHook(event, interactionHook, lossMessage);
            });
        }
    }
}
