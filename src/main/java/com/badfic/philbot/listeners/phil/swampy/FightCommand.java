package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.jagrosh.jdautilities.command.CommandEvent;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.stereotype.Component;

@Component
public class FightCommand extends BaseSwampy {

    private List<FightOutcome> fightOutcomes;

    public FightCommand() {
        name = "fight";
        help = "!!fight @user: fight a user, get a random outcome. fun for the whole family";
    }

    @PostConstruct
    public void init() throws Exception {
        fightOutcomes = Arrays.asList(objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("fight-outcomes.json"), FightOutcome[].class));
    }

    @Override
    protected void execute(CommandEvent event) {
        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.size(mentionedMembers) != 1) {
            event.replyError("Please mention a user to fight");
            return;
        }

        Member aggressor = event.getMember();
        Member victim = mentionedMembers.get(0);

        FightOutcome fightOutcome = Constants.pickRandom(fightOutcomes);

        String text = fightOutcome.getText();
        text = RegExUtils.replaceAll(text, "<AGGRESSOR>", aggressor.getEffectiveName());
        text = RegExUtils.replaceAll(text, "<VICTIM>", victim.getEffectiveName());
        String finalText = text;

        if (fightOutcome.getWinner() == FightWinner.AGGRESSOR) {
            allocatePoints(event, aggressor, victim, fightOutcome, finalText);
        } else {
            allocatePoints(event, victim, aggressor, fightOutcome, finalText);
        }
    }

    private void allocatePoints(CommandEvent event, Member winner, Member loser, FightOutcome fightOutcome, String finalText) {
        CompletableFuture.allOf(
                givePointsToMember(fightOutcome.getPointsToWinner(), winner, PointsStat.FIGHT),
                takePointsFromMember(fightOutcome.getPointsFromLoser(), loser, PointsStat.FIGHT))
        .thenRun(() -> {
            String footer = winner.getEffectiveName() + " won " + fightOutcome.getPointsToWinner() + " points.\n" +
                    loser.getEffectiveName() + " lost " + fightOutcome.getPointsFromLoser() + " points.";

            event.reply(Constants.simpleEmbed(winner.getEffectiveName() + " won", finalText, null, footer, null, winner.getEffectiveAvatarUrl()));
        });
    }

    public enum FightWinner {
        AGGRESSOR, VICTIM
    }

    public static class FightOutcome {
        private String text;
        private FightWinner winner;
        private int pointsToWinner;
        private int pointsFromLoser;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public FightWinner getWinner() {
            return winner;
        }

        public void setWinner(FightWinner winner) {
            this.winner = winner;
        }

        public int getPointsToWinner() {
            return pointsToWinner;
        }

        public void setPointsToWinner(int pointsToWinner) {
            this.pointsToWinner = pointsToWinner;
        }

        public int getPointsFromLoser() {
            return pointsFromLoser;
        }

        public void setPointsFromLoser(int pointsFromLoser) {
            this.pointsFromLoser = pointsFromLoser;
        }
    }
}
