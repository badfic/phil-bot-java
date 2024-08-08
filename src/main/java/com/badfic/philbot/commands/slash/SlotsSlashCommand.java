package com.badfic.philbot.commands.slash;

import com.badfic.philbot.commands.bang.SwampyCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.PointsStat;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;

@Service
class SlotsSlashCommand extends BaseSlashCommand {
    SlotsSlashCommand() {
        name = "slots";
        help = "Play slots to win swampy points!";
    }

    @Override
    public void execute(final SlashCommandInteractionEvent event) {
        final var interactionHook = event.deferReply().submit();

        final var member = event.getMember();
        final var discordUser = getDiscordUserByMember(member);

        final var swampyGamesConfig = getSwampyGamesConfig();

        final var now = LocalDateTime.now();
        final var nextSlotsTime = discordUser.getLastSlots().plus(swampyGamesConfig.getSlotsTimeoutMinutes(), ChronoUnit.MINUTES);
        if (now.isBefore(nextSlotsTime)) {
            final var duration = Duration.between(now, nextSlotsTime);

            if (duration.getSeconds() < 60) {
                replyToInteractionHook(event, interactionHook, "You must wait " + (duration.getSeconds() + 1) + " seconds before playing slots again");
            } else {
                replyToInteractionHook(event, interactionHook, "You must wait " + (duration.toMinutes() + 1) + " minutes before playing slots again");
            }
            return;
        }

        discordUser.setLastSlots(now);

        final var slotsEmojis = Arrays.stream(swampyGamesConfig.getSlotsEmoji()).collect(Collectors.toSet());
        final var size = slotsEmojis.size();

        final var oddsClosEnough = (1.0 / size) * (1.0 / size) * 300.0;
        final var oddsWinnerWinner = (1.0 / size) * (1.0 / size) * (1.0 / size) * 100.0;
        final var footer = String.format("Odds of a WINNER WINNER: %.3f%%\nOdds of a CLOSE ENOUGH: %.3f%%", oddsWinnerWinner, oddsClosEnough);

        final var one = Constants.pickRandom(slotsEmojis);
        final var two = Constants.pickRandom(slotsEmojis);
        final var three = Constants.pickRandom(slotsEmojis);

        final var winnerWinnerMessage = Constants.simpleEmbed(SwampyCommand.SLOT_MACHINE + " WINNER WINNER!! " + SwampyCommand.SLOT_MACHINE,
                String.format("%s\n%s%s%s \nYou won " + swampyGamesConfig.getSlotsWinPoints() + " points!", member.getAsMention(), one, two, three),
                null, footer);

        final var closeEnoughMessage = Constants.simpleEmbed(SwampyCommand.SLOT_MACHINE + " CLOSE ENOUGH! " + SwampyCommand.SLOT_MACHINE,
                String.format("%s\n%s%s%s \nYou got 2 out of 3! You won " + swampyGamesConfig.getSlotsTwoOfThreePoints() + " points!",
                        member.getAsMention(), one, two, three),
                null, footer);

        final var lossMessage = Constants.simpleEmbed(SwampyCommand.SLOT_MACHINE + " Better luck next time! " + SwampyCommand.SLOT_MACHINE,
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
