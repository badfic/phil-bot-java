package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.hungersim.Game;
import com.badfic.philbot.data.hungersim.GameRepository;
import com.badfic.philbot.data.hungersim.Outcome;
import com.badfic.philbot.data.hungersim.Player;
import com.badfic.philbot.data.hungersim.PlayerRepository;
import com.badfic.philbot.data.hungersim.Round;
import com.badfic.philbot.data.hungersim.RoundOutcome;
import com.badfic.philbot.data.hungersim.RoundOutcomeRepository;
import com.badfic.philbot.data.hungersim.RoundRepository;
import com.google.common.html.HtmlEscapers;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class HungerSimService extends BaseService {

    @Resource
    private GameRepository gameRepository;

    @Resource
    private RoundRepository roundRepository;

    @Resource
    private RoundOutcomeRepository roundOutcomeRepository;

    @Resource
    private PlayerRepository playerRepository;

    public synchronized Game runNextStep() {
        Game game = gameRepository.findById(Game.SINGLETON_ID)
                .orElseThrow(() -> new IllegalArgumentException("You must start a new game before running a step"));

        Deque<Player> alivePlayers = game.getPlayers()
                .stream()
                .filter(p -> p.getHp() > 0)
                .collect(Collectors.toCollection(ArrayDeque::new));

        if (alivePlayers.size() <= 1) {
            if (alivePlayers.size() <= 0) {
                game.setCurrentOutcomes(Collections.singletonList("Everybody died!"));
                return gameRepository.save(game);
            }

            Player winner = alivePlayers.pop();
            winner.setEffectiveNameViaJda(philJda);
            game.setCurrentOutcomes(Collections.singletonList("<b>" + HtmlEscapers.htmlEscaper().escape(winner.getEffectiveName()) + "</b> has won!"));
            return gameRepository.save(game);
        }

        // If this is the beginning of the game
        if (game.getRoundCounter() == 0) {
            if (game.getPlayers().size() < 2) {
                throw new IllegalArgumentException("You can't start a game with less than 2 players");
            }

            // DB Constraint guarantees there's only one opening round = true
            Round openingRound = roundRepository.findByOpeningRound(true).get(0);

            return runRoundAndGetResult(game, alivePlayers, openingRound);
        }

        Round round = Constants.pickRandom(roundRepository.findByOpeningRound(false));
        return runRoundAndGetResult(game, alivePlayers, round);
    }

    private Game runRoundAndGetResult(Game game, Deque<Player> activePlayers, Round round) {
        List<Outcome> outcomes = roundOutcomeRepository.findByRound(round).stream().map(RoundOutcome::getOutcome).toList();
        List<String> appliedOutcomes = new ArrayList<>();

        while (!activePlayers.isEmpty()) {
            List<Player> outcomePlayerSet = new ArrayList<>();
            Outcome outcome = Constants.pickRandom(outcomes);

            while (outcome.getNumPlayers() > activePlayers.size()) {
                outcome = Constants.pickRandom(outcomes);
            }

            for (int i = 0; i < outcome.getNumPlayers(); i++) {
                outcomePlayerSet.add(activePlayers.pop());
            }

            switch (outcome.getNumPlayers()) {
                case 1:
                    appliedOutcomes.add(outcome.apply(outcomePlayerSet.get(0), philJda));
                    break;
                case 2:
                    appliedOutcomes.add(outcome.apply(outcomePlayerSet.get(0), outcomePlayerSet.get(1), philJda));
                    break;
                case 3:
                    appliedOutcomes.add(outcome.apply(outcomePlayerSet.get(0), outcomePlayerSet.get(1), outcomePlayerSet.get(2), philJda));
                    break;
                case 4:
                    appliedOutcomes.add(outcome.apply(outcomePlayerSet.get(0), outcomePlayerSet.get(1), outcomePlayerSet.get(2), outcomePlayerSet.get(3),
                            philJda));
                    break;
                default:
                    throw new IllegalArgumentException("Found Outcome that somehow had more than 4 players");
            }

            playerRepository.saveAll(outcomePlayerSet);
        }

        game.setRound(round);
        game.setCurrentOutcomes(appliedOutcomes);
        game.setRoundCounter(game.getRoundCounter() + 1);
        return gameRepository.save(game);
    }

}
