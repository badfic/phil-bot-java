package com.badfic.philbot.web.members;

import com.badfic.philbot.data.DiscordUser;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
public class HungerSimRestController extends BaseMembersController {

    public record GameDto(String name, Set<Long> playerIds) {}
    public record OutcomeDto(String outcomeText, Integer numPlayers, Integer player1Hp, Integer player2Hp, Integer player3Hp, Integer player4Hp) {}
    public record PlayerDto(String name, Long discordId, Long pronounId) {}
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

    public HungerSimRestController(JdbcAggregateTemplate jdbcAggregateTemplate, HungerSimService hungerSimService, GameRepository gameRepository,
                                   PlayerRepository playerRepository, PronounRepository pronounRepository, OutcomeRepository outcomeRepository,
                                   RoundRepository roundRepository, RoundOutcomeRepository roundOutcomeRepository) {
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
        this.hungerSimService = hungerSimService;
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.pronounRepository = pronounRepository;
        this.outcomeRepository = outcomeRepository;
        this.roundRepository = roundRepository;
        this.roundOutcomeRepository = roundOutcomeRepository;
    }

    /** PRONOUNS **/

    @GetMapping(value = "/hunger-sim/pronoun", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Pronoun> getPronouns(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        List<Pronoun> pronouns = pronounRepository.findAll();
        pronouns.sort(Comparator.comparing(Pronoun::getId));
        return pronouns;
    }

    @PostMapping(value = "/hunger-sim/pronoun", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Pronoun newPronoun(HttpServletRequest httpServletRequest, @RequestBody PronounDto pronoun) throws Exception {
        checkSession(httpServletRequest, true);

        if (Stream.of(pronoun.subject, pronoun.object, pronoun.possessive, pronoun.self).anyMatch(StringUtils::isBlank)) {
            throw new IllegalArgumentException("subject, object, possessive, and self must not be empty");
        }

        try {
            return jdbcAggregateTemplate.insert(new Pronoun(pronoun.subject, pronoun.object, pronoun.possessive, pronoun.self));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Failed to save pronoun, it's likely a pronoun with these values already exists", e);
        }
    }

    @PutMapping(value = "/hunger-sim/pronoun/{pronounId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Pronoun editPronoun(HttpServletRequest httpServletRequest, @PathVariable("pronounId") Long pronounId, @RequestBody PronounDto pronoun)
            throws Exception {
        checkSession(httpServletRequest, true);

        if (Stream.of(pronoun.subject, pronoun.object, pronoun.possessive, pronoun.self).anyMatch(StringUtils::isBlank)) {
            throw new IllegalArgumentException("subject, object, possessive, and self must not be empty");
        }

        Pronoun existingPronoun = pronounRepository.findById(pronounId).orElseThrow(() -> new IllegalArgumentException("pronoun by that id not found"));

        existingPronoun.setSubject(pronoun.subject);
        existingPronoun.setObject(pronoun.object);
        existingPronoun.setPossessive(pronoun.possessive);
        existingPronoun.setSelf(pronoun.self);

        return pronounRepository.save(existingPronoun);
    }

    @DeleteMapping(value = "/hunger-sim/pronoun/{pronounId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deletePronoun(HttpServletRequest httpServletRequest, @PathVariable("pronounId") Long pronounId) throws Exception {
        checkSession(httpServletRequest, true);

        Pronoun pronoun = pronounRepository.findById(pronounId).orElseThrow(() -> new IllegalArgumentException("Could not find Pronoun with that ID"));

        try {
            pronounRepository.delete(pronoun);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("You can't delete a pronoun that still has player(s) attached to it. Delete the player(s) first.", e);
        }
    }

    /** PLAYERS **/

    @GetMapping(value = "/hunger-sim/player", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Player> getPlayers(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        List<Player> players = playerRepository.findAll();
        players.sort(Comparator.comparing(Player::getId));
        players.forEach(p -> p.setEffectiveNameViaJda(philJda));
        return players;
    }

    @PostMapping(value = "/hunger-sim/player", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Player newPlayer(HttpServletRequest httpServletRequest, @RequestBody PlayerDto player) throws Exception {
        checkSession(httpServletRequest, true);

        Pronoun pronouns = pronounRepository.findById(Objects.requireNonNull(player.pronounId))
                .orElseThrow(() -> new IllegalArgumentException("Pronouns not found by id"));

        if (player.discordId != null) {
            DiscordUser discordUser = discordUserRepository.findById(String.valueOf(player.discordId))
                    .orElseThrow(() -> new IllegalArgumentException("discordId not recognized as a member of this server"));

            try {
                return jdbcAggregateTemplate.insert(new Player(discordUser, pronouns));
            } catch (DataIntegrityViolationException e) {
                throw new IllegalArgumentException("Failed to save player, it's likely a player with this discordId already exists", e);
            }
        } else if (StringUtils.isNotBlank(player.name)) {
            try {
                return jdbcAggregateTemplate.insert(new Player(player.name, pronouns));
            } catch (DataIntegrityViolationException e) {
                throw new IllegalArgumentException("Failed to save player, it's likely a player with this name already exists", e);
            }
        }

        throw new IllegalArgumentException("Unable to save player, please set either name or discordId to a non-null value");
    }

    @DeleteMapping(value = "/hunger-sim/player/{playerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deletePlayer(HttpServletRequest httpServletRequest, @PathVariable("playerId") Long playerId) throws Exception {
        checkSession(httpServletRequest, true);

        Player player = playerRepository.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Player by that ID not found"));
        playerRepository.delete(player);
    }

    /** OUTCOMES **/

    @GetMapping(value = "/hunger-sim/outcome", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Outcome> getOutcomes(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        List<Outcome> outcomes = outcomeRepository.findAll();
        outcomes.sort(Comparator.comparing(Outcome::getId));
        return outcomes;
    }

    @GetMapping(value = "/hunger-sim/outcome/variables", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<Integer, List<String>> getOutcomeVariables(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        return Outcome.VARIABLES;
    }

    @PostMapping(value = "/hunger-sim/outcome", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Outcome newOutcome(HttpServletRequest httpServletRequest, @RequestBody OutcomeDto outcome) throws Exception {
        checkSession(httpServletRequest, true);

        validateOutcome(outcome);

        try {
            return jdbcAggregateTemplate.insert(new Outcome(
                    outcome.outcomeText, outcome.numPlayers, outcome.player1Hp, outcome.player2Hp, outcome.player3Hp, outcome.player4Hp));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Failed to save outcome, it's likely an outcome with this outcomeText already exists", e);
        }
    }

    @DeleteMapping(value = "/hunger-sim/outcome/{outcomeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteOutcome(HttpServletRequest httpServletRequest, @PathVariable("outcomeId") Long outcomeId) throws Exception {
        checkSession(httpServletRequest, true);
        Outcome outcome = outcomeRepository.findById(outcomeId).orElseThrow(() -> new IllegalArgumentException("Outcome by that ID does not exist"));
        List<RoundOutcome> roundOutcomes = roundOutcomeRepository.findByOutcome(outcome.getId());

        for (RoundOutcome roundOutcome : roundOutcomes) {
            Long roundId = roundOutcome.getRound();

            List<Long> outcomes = roundOutcomeRepository.findByRound(roundId).stream().map(RoundOutcome::getOutcome).toList();

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
    public List<Round> getRounds(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        List<Round> rounds = roundRepository.findAll();
        rounds.sort(Comparator.comparing(Round::getId));

        for (Round round : rounds) {
            List<Long> outcomeIds = roundOutcomeRepository.findByRound(round.getId()).stream().map(RoundOutcome::getOutcome).toList();
            List<Outcome> outcomes = outcomeRepository.findAllById(outcomeIds);

            round.setOutcomes(outcomes);
        }

        return rounds;
    }

    @PostMapping(value = "/hunger-sim/round", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Round newRound(HttpServletRequest httpServletRequest, @RequestBody RoundDto round) throws Exception {
        checkSession(httpServletRequest, true);

        if (CollectionUtils.isEmpty(round.outcomeIds)) {
            throw new IllegalArgumentException("A round must include at least one outcome");
        }

        try {
            Round savedRound = jdbcAggregateTemplate.insert(new Round(round.name, round.description, round.openingRound));

            List<Outcome> outcomes = outcomeRepository.findAllById(round.outcomeIds.stream().map(Long::parseLong).toList());
            for (Outcome outcome : outcomes) {
                jdbcAggregateTemplate.insert(new RoundOutcome(savedRound, outcome));
            }

            return roundRepository.findById(savedRound.getId()).orElseThrow(() -> new IllegalArgumentException("Failed to save round"));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Failed to save round, it's likely a round with this same name or OpeningRound=YES already exists", e);
        }
    }

    @PutMapping(value = "/hunger-sim/round/{roundId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Round updateRound(HttpServletRequest httpServletRequest, @PathVariable("roundId") Long roundId, @RequestBody RoundDto round) throws Exception {
        checkSession(httpServletRequest, true);

        if (CollectionUtils.isEmpty(round.outcomeIds)) {
            throw new IllegalArgumentException("A round must include at least one outcome");
        }

        Round existingRound = roundRepository.findById(roundId).orElseThrow(() -> new IllegalArgumentException("Could not found round with that ID"));

        try {
            existingRound.setName(round.name);
            existingRound.setDescription(round.description);
            existingRound.setOpeningRound(round.openingRound);

            Set<Long> outcomeIds = round.outcomeIds.stream().map(Long::parseLong).collect(Collectors.toSet());
            for (RoundOutcome roundOutcome : roundOutcomeRepository.findByRound(roundId)) {
                if (!outcomeIds.contains(roundOutcome.getOutcome())) {
                    roundOutcomeRepository.delete(roundOutcome);
                }
            }

            for (Long outcomeId : outcomeIds) {
                Outcome outcome = outcomeRepository.findById(outcomeId)
                        .orElseThrow(() -> new IllegalArgumentException("Outcome id " + outcomeId + " does not exist"));

                if (!roundOutcomeRepository.existsByRoundAndOutcome(roundId, outcomeId)) {
                    jdbcAggregateTemplate.insert(new RoundOutcome(existingRound, outcome));
                }
            }

            return roundRepository.save(existingRound);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Failed to update round, it's likely a round with this same name or OpeningRound=YES already exists", e);
        }
    }

    @DeleteMapping(value = "/hunger-sim/round/{roundId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteRound(HttpServletRequest httpServletRequest, @PathVariable("roundId") Long roundId) throws Exception {
        checkSession(httpServletRequest, true);

        Round round = roundRepository.findById(roundId).orElseThrow(() -> new IllegalArgumentException("Round by that ID does not exist"));
        roundOutcomeRepository.deleteAll(roundOutcomeRepository.findByRound(roundId));
        roundRepository.delete(round);
    }

    /** GAME **/

    @PostMapping(value = "/hunger-sim/game", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Game newGame(HttpServletRequest httpServletRequest, @RequestBody GameDto game) throws Exception {
        checkSession(httpServletRequest, true);

        List<Round> openingRound = roundRepository.findByOpeningRound(true);
        if (CollectionUtils.isEmpty(openingRound)) {
            throw new IllegalArgumentException("You must make one round and mark it as \"Opening Round\"");
        }

        if (CollectionUtils.isEmpty(roundRepository.findByOpeningRound(false))) {
            throw new IllegalArgumentException("You must make one additional round beyond the \"Opening Round\"");
        }

        if (game.playerIds.size() < 2) {
            throw new IllegalArgumentException("You can't start a game with less than 2 players");
        }

        List<Player> players = playerRepository.findAllById(game.playerIds);

        for (Player player : players) {
            player.setHp(10);
        }

        players = playerRepository.saveAll(players);

        for (Player player : players) {
            player.setEffectiveNameViaJda(philJda);
        }

        List<String> outcomes = new ArrayList<>();
        outcomes.add("Here are our tributes!");
        outcomes.addAll(players.stream().map(Player::getEffectiveName).toList());

        Optional<Game> optionalGame = gameRepository.findById(Game.SINGLETON_ID);

        Game gameEntity = optionalGame.orElseGet(Game::new);

        gameEntity.setId(Game.SINGLETON_ID);
        gameEntity.setName(game.name);
        gameEntity.setRound(openingRound.get(0).getId());
        gameEntity.setRoundCounter(0);
        gameEntity.setCurrentOutcomes(outcomes);
        gameEntity.setPlayers(players);

        if (optionalGame.isPresent()) {
            return gameRepository.save(gameEntity);
        } else {
            return jdbcAggregateTemplate.insert(gameEntity);
        }
    }

    @GetMapping(value = "/hunger-sim/game", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game getGame(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        Game game = gameRepository.findById(Game.SINGLETON_ID).orElseThrow(() -> new IllegalArgumentException("Unable to load game"));

        if (game == null) {
            return null;
        }

        game.getPlayers().forEach(p -> p.setEffectiveNameViaJda(philJda));

        return game;
    }

    @PostMapping(value = "/hunger-sim/game/step", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game runStep(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        return hungerSimService.runNextStep();
    }

    private void validateOutcome(OutcomeDto outcome) {
        if (outcome.numPlayers < 1 || outcome.numPlayers > 4) {
            throw new IllegalArgumentException("numPlayers must be 1, 2, 3, or 4");
        }

        for (int i = 1; i < (outcome.numPlayers + 1); i++) {
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

}
