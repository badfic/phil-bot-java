package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.hungersim.Game;
import com.badfic.philbot.data.hungersim.GameRepository;
import com.badfic.philbot.data.hungersim.Outcome;
import com.badfic.philbot.data.hungersim.OutcomeRepository;
import com.badfic.philbot.data.hungersim.Player;
import com.badfic.philbot.data.hungersim.PlayerRepository;
import com.badfic.philbot.data.hungersim.PronounRepository;
import com.badfic.philbot.data.hungersim.Round;
import com.badfic.philbot.data.hungersim.RoundOutcome;
import com.badfic.philbot.data.hungersim.RoundOutcomeRepository;
import com.badfic.philbot.data.hungersim.RoundRepository;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Component;

@Component
public class HungerSimService extends BaseService {

    private final GameRepository gameRepository;
    private final RoundRepository roundRepository;
    private final RoundOutcomeRepository roundOutcomeRepository;
    private final OutcomeRepository outcomeRepository;
    private final PlayerRepository playerRepository;
    private final PronounRepository pronounRepository;

    public HungerSimService(GameRepository gameRepository, RoundRepository roundRepository, RoundOutcomeRepository roundOutcomeRepository,
                            OutcomeRepository outcomeRepository, PlayerRepository playerRepository, PronounRepository pronounRepository) {
        this.gameRepository = gameRepository;
        this.roundRepository = roundRepository;
        this.roundOutcomeRepository = roundOutcomeRepository;
        this.outcomeRepository = outcomeRepository;
        this.playerRepository = playerRepository;
        this.pronounRepository = pronounRepository;
    }

    public synchronized Game runNextStep() {
        Game game = gameRepository.findById(Constants.DATA_SINGLETON_ID)
                .orElseThrow(() -> new IllegalArgumentException("You must start a new game before running a step"));

        List<Player> players = playerRepository.findByGame(Constants.DATA_SINGLETON_ID);
        game.setPlayers(players);

        Deque<Player> alivePlayers = game.getPlayers()
                .stream()
                .filter(p -> p.getHp() > 0)
                .collect(Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), list -> {
                    Collections.shuffle(list);
                    return list.stream();
                }))
                .collect(Collectors.toCollection(ArrayDeque::new));

        if (alivePlayers.size() <= 1) {
            if (alivePlayers.size() <= 0) {
                game.setCurrentOutcomes(new String[] {"Everybody died!"});
                return gameRepository.save(game);
            }

            Player winner = alivePlayers.pop();
            winner.setEffectiveNameViaJda(philJda);
            game.setCurrentOutcomes(new String[] {"<b>" + StringEscapeUtils.escapeHtml4(winner.getEffectiveName()) + "</b> has won!"});
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
        List<Long> outcomes = roundOutcomeRepository.findByRound(round.getId()).stream().map(RoundOutcome::getOutcome).toList();
        List<String> appliedOutcomes = new ArrayList<>();

        while (!activePlayers.isEmpty()) {
            List<Player> outcomePlayerSet = new ArrayList<>();
            Long outcomeId = Constants.pickRandom(outcomes);

            Outcome outcome = outcomeRepository.findById(outcomeId).orElseThrow();

            while (outcome.getNumPlayers() > activePlayers.size()) {
                outcomeId = Constants.pickRandom(outcomes);
                outcome = outcomeRepository.findById(outcomeId).orElseThrow();
            }

            for (int i = 0; i < outcome.getNumPlayers(); i++) {
                outcomePlayerSet.add(activePlayers.pop());
            }

            switch (outcome.getNumPlayers()) {
                case 1 -> appliedOutcomes.add(outcome.apply(outcomePlayerSet.get(0), philJda, pronounRepository));
                case 2 -> appliedOutcomes.add(outcome.apply(outcomePlayerSet.get(0), outcomePlayerSet.get(1), philJda, pronounRepository));
                case 3 -> appliedOutcomes.add(outcome.apply(outcomePlayerSet.get(0), outcomePlayerSet.get(1), outcomePlayerSet.get(2), philJda,
                        pronounRepository));
                case 4 -> appliedOutcomes.add(outcome.apply(outcomePlayerSet.get(0), outcomePlayerSet.get(1), outcomePlayerSet.get(2), outcomePlayerSet.get(3),
                        philJda, pronounRepository));
                default -> throw new IllegalArgumentException("Found Outcome that somehow had more than 4 players");
            }

            playerRepository.saveAll(outcomePlayerSet);
        }

        game.setRound(round.getId());
        game.setCurrentOutcomes(appliedOutcomes.toArray(String[]::new));
        game.setRoundCounter(game.getRoundCounter() + 1);
        return gameRepository.save(game);
    }

}
