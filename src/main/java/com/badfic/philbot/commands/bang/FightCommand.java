package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.PointsStat;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class FightCommand extends BaseBangCommand {
    FightCommand() {
        name = "fight";
        help = "!!fight @user: fight a user, get a random outcome. fun for the whole family";
    }

    @Override
    public void execute(CommandEvent event) {
        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();

        if (CollectionUtils.size(mentionedMembers) != 1) {
            event.replyError("Please mention a user to fight");
            return;
        }

        Member aggressor = event.getMember();
        Member victim = mentionedMembers.getFirst();

        FightOutcome[] fightOutcomes;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("fight-outcomes.json")) {
            fightOutcomes = objectMapper.readValue(stream, FightOutcome[].class);
        } catch (Exception e) {
            log.error("Failed to load fight outcomes", e);
            event.replyError("Could not load fight outcomes, nobody wins!");
            return;
        }

        FightOutcome fightOutcome = Constants.pickRandom(fightOutcomes);

        String text = fightOutcome.text();
        text = RegExUtils.replaceAll(text, "<AGGRESSOR>", aggressor.getEffectiveName());
        text = RegExUtils.replaceAll(text, "<VICTIM>", victim.getEffectiveName());
        String finalText = text;

        if (fightOutcome.winner() == FightWinner.AGGRESSOR) {
            allocatePoints(event, aggressor, victim, fightOutcome, finalText);
        } else {
            allocatePoints(event, victim, aggressor, fightOutcome, finalText);
        }
    }

    private void allocatePoints(CommandEvent event, Member winner, Member loser, FightOutcome fightOutcome, String finalText) {
        CompletableFuture.allOf(
                givePointsToMember(fightOutcome.pointsToWinner(), winner, PointsStat.FIGHT),
                takePointsFromMember(fightOutcome.pointsFromLoser(), loser, PointsStat.FIGHT))
        .thenRun(() -> {
            String footer = winner.getEffectiveName() + " won " + fightOutcome.pointsToWinner() + " points.\n" +
                    loser.getEffectiveName() + " lost " + fightOutcome.pointsFromLoser() + " points.";

            event.reply(Constants.simpleEmbed(winner.getEffectiveName() + " won", finalText, null, footer, null, winner.getEffectiveAvatarUrl()));
        });
    }

    private enum FightWinner {
        AGGRESSOR, VICTIM
    }

    private record FightOutcome(String text, FightWinner winner, int pointsToWinner, int pointsFromLoser) {}
}
