package com.badfic.philbot.web.members;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.hungersim.Game;
import com.badfic.philbot.data.hungersim.GameRepository;
import com.badfic.philbot.data.hungersim.Outcome;
import com.badfic.philbot.data.hungersim.OutcomeRepository;
import com.badfic.philbot.data.hungersim.Player;
import com.badfic.philbot.data.hungersim.PlayerRepository;
import com.badfic.philbot.data.hungersim.Pronoun;
import com.badfic.philbot.data.hungersim.PronounRepository;
import com.badfic.philbot.data.hungersim.Round;
import com.badfic.philbot.data.hungersim.RoundOutcome;
import com.badfic.philbot.data.hungersim.RoundOutcomeRepository;
import com.badfic.philbot.data.hungersim.RoundRepository;
import com.badfic.philbot.service.HungerSimService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HungerSimRestController extends BaseMembersController {

    public record GameDto(String name, Set<Long> playerIds) {}
    public record OutcomeDto(String outcomeText, Integer numPlayers, Integer player1Hp, Integer player2Hp, Integer player3Hp, Integer player4Hp) {}
    public record PlayerDto(String name, Long discordId, Long pronounId) {}
    public record PlayerDisplayDto(Long id, Pronoun pronoun, String name, String discordUser, String effectiveName) {}
    public record PronounDto(String subject, String object, String possessive, String self) {}
    public record RoundDto(String name, String description, Boolean openingRound, List<String> outcomeIds) {}

    private final JdbcAggregateTemplate jdbcAggregateTemplate;
    private final HungerSimService hungerSimService;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final PronounRepository pronounRepository;
    private final OutcomeRepository outcomeRepository;
    private final RoundRepository roundRepository;
    private final RoundOutcomeRepository roundOutcomeRepository;

    /** PRONOUNS **/

    @GetMapping(value = "/hunger-sim/pronoun", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Pronoun> getPronouns(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        final var pronouns = pronounRepository.findAll();
        pronouns.sort(comparingReverse(Pronoun::getId));
        return pronouns;
    }

    @PostMapping(value = "/hunger-sim/pronoun", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Pronoun newPronoun(final HttpServletRequest httpServletRequest, @RequestBody final PronounDto pronoun) throws Exception {
        checkSession(httpServletRequest, true);

        if (Stream.of(pronoun.subject, pronoun.object, pronoun.possessive, pronoun.self).anyMatch(StringUtils::isBlank)) {
            throw new IllegalArgumentException("subject, object, possessive, and self must not be empty");
        }

        try {
            return jdbcAggregateTemplate.insert(new Pronoun(pronoun.subject, pronoun.object, pronoun.possessive, pronoun.self));
        } catch (final DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Failed to save pronoun, it's likely a pronoun with these values already exists", e);
        }
    }

    @PutMapping(value = "/hunger-sim/pronoun/{pronounId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Pronoun editPronoun(final HttpServletRequest httpServletRequest, @PathVariable("pronounId") final Long pronounId, @RequestBody final PronounDto pronoun)
            throws Exception {
        checkSession(httpServletRequest, true);

        if (Stream.of(pronoun.subject, pronoun.object, pronoun.possessive, pronoun.self).anyMatch(StringUtils::isBlank)) {
            throw new IllegalArgumentException("subject, object, possessive, and self must not be empty");
        }

        final var existingPronoun = pronounRepository.findById(pronounId).orElseThrow(() -> new IllegalArgumentException("pronoun by that id not found"));

        existingPronoun.setSubject(pronoun.subject);
        existingPronoun.setObject(pronoun.object);
        existingPronoun.setPossessive(pronoun.possessive);
        existingPronoun.setSelf(pronoun.self);

        return pronounRepository.save(existingPronoun);
    }

    @DeleteMapping(value = "/hunger-sim/pronoun/{pronounId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deletePronoun(final HttpServletRequest httpServletRequest, @PathVariable("pronounId") final Long pronounId) throws Exception {
        checkSession(httpServletRequest, true);

        final var pronoun = pronounRepository.findById(pronounId).orElseThrow(() -> new IllegalArgumentException("Could not find Pronoun with that ID"));

        try {
            pronounRepository.delete(pronoun);
        } catch (final DataIntegrityViolationException e) {
            throw new IllegalArgumentException("You can't delete a pronoun that still has player(s) attached to it. Delete the player(s) first.", e);
        }
    }

    /** PLAYERS **/

    @GetMapping(value = "/hunger-sim/player", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PlayerDisplayDto> getPlayers(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        final var players = playerRepository.findAll();
        players.sort(comparingReverse(Player::getId));

        final var pronounsMap = pronounRepository.findAll().stream().collect(Collectors.toMap(Pronoun::getId, Function.identity()));

        return players.stream().map(p -> {
            p.setEffectiveNameViaJda(philJda);
            Pronoun pronoun = pronounsMap.get(p.getPronoun());

            return new PlayerDisplayDto(p.getId(), pronoun, p.getName(), p.getDiscordUser(), p.getEffectiveName());
        }).toList();
    }

    @PostMapping(value = "/hunger-sim/player", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Player newPlayer(final HttpServletRequest httpServletRequest, @RequestBody final PlayerDto player) throws Exception {
        checkSession(httpServletRequest, true);

        final var pronouns = pronounRepository.findById(Objects.requireNonNull(player.pronounId))
                .orElseThrow(() -> new IllegalArgumentException("Pronouns not found by id"));

        if (player.discordId != null) {
            final var discordUser = discordUserRepository.findById(String.valueOf(player.discordId))
                    .orElseThrow(() -> new IllegalArgumentException("discordId not recognized as a member of this server"));

            try {
                return jdbcAggregateTemplate.insert(new Player(discordUser, pronouns));
            } catch (final DataIntegrityViolationException e) {
                throw new IllegalArgumentException("Failed to save player, it's likely a player with this discordId already exists", e);
            }
        } else if (StringUtils.isNotBlank(player.name)) {
            try {
                return jdbcAggregateTemplate.insert(new Player(player.name, pronouns));
            } catch (final DataIntegrityViolationException e) {
                throw new IllegalArgumentException("Failed to save player, it's likely a player with this name already exists", e);
            }
        }

        throw new IllegalArgumentException("Unable to save player, please set either name or discordId to a non-null value");
    }

    @DeleteMapping(value = "/hunger-sim/player/{playerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deletePlayer(final HttpServletRequest httpServletRequest, @PathVariable("playerId") final Long playerId) throws Exception {
        checkSession(httpServletRequest, true);

        final var player = playerRepository.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Player by that ID not found"));
        playerRepository.delete(player);
    }

    /** OUTCOMES **/

    @GetMapping(value = "/hunger-sim/outcome", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Outcome> getOutcomes(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        final var outcomes = outcomeRepository.findAll();
        outcomes.sort(comparingReverse(Outcome::getId));
        return outcomes;
    }

    @GetMapping(value = "/hunger-sim/outcome/variables", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<Integer, List<String>> getOutcomeVariables(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        return Map.of(
                1, List.of("{player1}", "{player1_subject}", "{player1_object}", "{player1_possessive}", "{player1_self}"),
                2, List.of("{player2}", "{player2_subject}", "{player2_object}", "{player2_possessive}", "{player2_self}"),
                3, List.of("{player3}", "{player3_subject}", "{player3_object}", "{player3_possessive}", "{player3_self}"),
                4, List.of("{player4}", "{player4_subject}", "{player4_object}", "{player4_possessive}", "{player4_self}")
        );
    }

    @PostMapping(value = "/hunger-sim/outcome", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Outcome newOutcome(final HttpServletRequest httpServletRequest, @RequestBody final OutcomeDto outcome) throws Exception {
        checkSession(httpServletRequest, true);

        validateOutcome(outcome);

        try {
            return jdbcAggregateTemplate.insert(new Outcome(
                    outcome.outcomeText, outcome.numPlayers, outcome.player1Hp, outcome.player2Hp, outcome.player3Hp, outcome.player4Hp));
        } catch (final DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Failed to save outcome, it's likely an outcome with this outcomeText already exists", e);
        }
    }

    @DeleteMapping(value = "/hunger-sim/outcome/{outcomeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteOutcome(final HttpServletRequest httpServletRequest, @PathVariable("outcomeId") final Long outcomeId) throws Exception {
        checkSession(httpServletRequest, true);
        final var outcome = outcomeRepository.findById(outcomeId).orElseThrow(() -> new IllegalArgumentException("Outcome by that ID does not exist"));
        final var roundOutcomes = roundOutcomeRepository.findByOutcome(outcome.getId());

        for (final var roundOutcome : roundOutcomes) {
            final var roundId = roundOutcome.getRound();

            final var outcomes = roundOutcomeRepository.findByRound(roundId).stream().map(RoundOutcome::getOutcome).toList();

            if (outcomes.size() <= 1) {
                throw new IllegalArgumentException(String.format(
                        "Cannot delete this outcome as it is the sole outcome of RoundId=%d. Please delete RoundId=%d first.", roundId, roundId));
            }
        }

        roundOutcomeRepository.deleteAll(roundOutcomes);
        outcomeRepository.delete(outcome);
    }

    /** ROUNDS **/

    @GetMapping(value = "/hunger-sim/round", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Round> getRounds(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        final var rounds = roundRepository.findAll();
        rounds.sort(comparingReverse(Round::getId));

        for (final var round : rounds) {
            final var outcomeIds = roundOutcomeRepository.findByRound(round.getId()).stream().map(RoundOutcome::getOutcome).toList();
            final var outcomes = outcomeRepository.findAllById(outcomeIds);

            round.setOutcomes(outcomes);
        }

        return rounds;
    }

    @PostMapping(value = "/hunger-sim/round", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Round newRound(final HttpServletRequest httpServletRequest, @RequestBody final RoundDto round) throws Exception {
        checkSession(httpServletRequest, true);

        if (CollectionUtils.isEmpty(round.outcomeIds)) {
            throw new IllegalArgumentException("A round must include at least one outcome");
        }

        try {
            final var savedRound = jdbcAggregateTemplate.insert(new Round(round.name, round.description, round.openingRound));

            final var outcomes = outcomeRepository.findAllById(round.outcomeIds.stream().map(Long::parseLong).toList());
            for (final var outcome : outcomes) {
                jdbcAggregateTemplate.insert(new RoundOutcome(savedRound, outcome));
            }

            return roundRepository.findById(savedRound.getId()).orElseThrow(() -> new IllegalArgumentException("Failed to save round"));
        } catch (final DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Failed to save round, it's likely a round with this same name or OpeningRound=YES already exists", e);
        }
    }

    @PutMapping(value = "/hunger-sim/round/{roundId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Round updateRound(final HttpServletRequest httpServletRequest, @PathVariable("roundId") final Long roundId, @RequestBody final RoundDto round) throws Exception {
        checkSession(httpServletRequest, true);

        if (CollectionUtils.isEmpty(round.outcomeIds)) {
            throw new IllegalArgumentException("A round must include at least one outcome");
        }

        final var existingRound = roundRepository.findById(roundId).orElseThrow(() -> new IllegalArgumentException("Could not found round with that ID"));

        try {
            existingRound.setName(round.name);
            existingRound.setDescription(round.description);
            existingRound.setOpeningRound(round.openingRound);

            final var outcomeIds = round.outcomeIds.stream().map(Long::parseLong).collect(Collectors.toSet());
            for (final var roundOutcome : roundOutcomeRepository.findByRound(roundId)) {
                if (!outcomeIds.contains(roundOutcome.getOutcome())) {
                    roundOutcomeRepository.delete(roundOutcome);
                }
            }

            for (final var outcomeId : outcomeIds) {
                final var outcome = outcomeRepository.findById(outcomeId)
                        .orElseThrow(() -> new IllegalArgumentException("Outcome id " + outcomeId + " does not exist"));

                if (!roundOutcomeRepository.existsByRoundAndOutcome(roundId, outcomeId)) {
                    jdbcAggregateTemplate.insert(new RoundOutcome(existingRound, outcome));
                }
            }

            return roundRepository.save(existingRound);
        } catch (final DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Failed to update round, it's likely a round with this same name or OpeningRound=YES already exists", e);
        }
    }

    @DeleteMapping(value = "/hunger-sim/round/{roundId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteRound(final HttpServletRequest httpServletRequest, @PathVariable("roundId") final Long roundId) throws Exception {
        checkSession(httpServletRequest, true);

        final var round = roundRepository.findById(roundId).orElseThrow(() -> new IllegalArgumentException("Round by that ID does not exist"));
        roundOutcomeRepository.deleteAll(roundOutcomeRepository.findByRound(roundId));
        roundRepository.delete(round);
    }

    /** GAME **/

    @PostMapping(value = "/hunger-sim/game", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Game newGame(final HttpServletRequest httpServletRequest, @RequestBody final GameDto game) throws Exception {
        checkSession(httpServletRequest, true);

        final var openingRound = roundRepository.findByOpeningRound(true);
        if (CollectionUtils.size(openingRound) != 1) {
            throw new IllegalArgumentException("You must make only one round and mark it as \"Opening Round\"");
        }

        if (CollectionUtils.isEmpty(roundRepository.findByOpeningRound(false))) {
            throw new IllegalArgumentException("You must make at least one additional round not labeled \"Opening Round\"");
        }

        if (game.playerIds.size() < 2) {
            throw new IllegalArgumentException("You can't start a game with less than 2 players");
        }

        final var optionalGame = gameRepository.findById(Constants.DATA_SINGLETON_ID);

        var gameEntity = optionalGame.orElseGet(Game::new);

        gameEntity.setId(Constants.DATA_SINGLETON_ID);
        gameEntity.setName(game.name);
        gameEntity.setRound(openingRound.getFirst().getId());
        gameEntity.setRoundCounter(0);

        if (optionalGame.isPresent()) {
            gameEntity = gameRepository.save(gameEntity);
        } else {
            gameEntity = jdbcAggregateTemplate.insert(gameEntity);
        }

        // Reset all players to "not in a game"
        final var allPlayers = playerRepository.findAll();
        for (final var player : allPlayers) {
            player.setGame(null);
        }
        playerRepository.saveAll(allPlayers);

        // Set only the specific players in this game to 10HP and Game.SINGLETON_ID
        var players = playerRepository.findAllById(game.playerIds);
        for (final var player : players) {
            player.setHp(10);
            player.setGame(Constants.DATA_SINGLETON_ID);
        }
        players = playerRepository.saveAll(players);

        for (final var player : players) {
            player.setEffectiveNameViaJda(philJda);
        }

        final var outcomes = new ArrayList<String>();
        outcomes.add("Here are our tributes!");
        for (final var player : players) {
            outcomes.add(player.getEffectiveName());
        }

        gameEntity.setCurrentOutcomes(outcomes.toArray(String[]::new));
        gameEntity.setPlayers(players);

        return gameRepository.save(gameEntity);
    }

    @GetMapping(value = "/hunger-sim/game", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game getGame(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        final var game = gameRepository.findById(Constants.DATA_SINGLETON_ID).orElseThrow(() -> new IllegalArgumentException("Unable to load game"));

        game.setPlayers(playerRepository.findByGame(game.getId()));

        game.getPlayers().forEach(p -> p.setEffectiveNameViaJda(philJda));

        return game;
    }

    @PostMapping(value = "/hunger-sim/game/step", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game runStep(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        return hungerSimService.runNextStep();
    }

    private void validateOutcome(final OutcomeDto outcome) {
        if (outcome.numPlayers < 1 || outcome.numPlayers > 4) {
            throw new IllegalArgumentException("numPlayers must be 1, 2, 3, or 4");
        }

        for (var i = 1; i < (outcome.numPlayers + 1); i++) {
            if (!StringUtils.contains(outcome.outcomeText, "{player" + i + '}')) {
                throw new IllegalArgumentException("Outcome must mention {player" + i + '}');
            }
        }

        if (outcome.player1Hp != null && (outcome.player1Hp < -10 || outcome.player1Hp > 10)) {
            throw new IllegalArgumentException("player1Hp must be between -10 and 10 inclusive");
        }
        if (outcome.player2Hp != null && (outcome.player2Hp < -10 || outcome.player2Hp > 10)) {
            throw new IllegalArgumentException("player2Hp must be between -10 and 10 inclusive");
        }
        if (outcome.player3Hp != null && (outcome.player3Hp < -10 || outcome.player3Hp > 10)) {
            throw new IllegalArgumentException("player3Hp must be between -10 and 10 inclusive");
        }
        if (outcome.player4Hp != null && (outcome.player4Hp < -10 || outcome.player4Hp > 10)) {
            throw new IllegalArgumentException("player4Hp must be between -10 and 10 inclusive");
        }
    }

    private static <T, U extends Comparable<? super U>> Comparator<T> comparingReverse(final Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (Comparator<T> & Serializable) (c1, c2) -> keyExtractor.apply(c2).compareTo(keyExtractor.apply(c1));
    }

}
