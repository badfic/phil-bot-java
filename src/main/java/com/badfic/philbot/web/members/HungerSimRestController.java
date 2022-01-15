package com.badfic.philbot.web.members;

import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.DiscordUserRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
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

    @Resource
    private HungerSimService hungerSimService;

    @Resource
    private GameRepository gameRepository;

    @Resource
    private PlayerRepository playerRepository;

    @Resource
    private DiscordUserRepository discordUserRepository;

    @Resource
    private PronounRepository pronounRepository;

    @Resource
    private OutcomeRepository outcomeRepository;

    @Resource
    private RoundRepository roundRepository;

    @Resource
    private RoundOutcomeRepository roundOutcomeRepository;

    /** PRONOUNS **/

    @GetMapping(value = "/hunger-sim/pronoun", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Pronoun> getPronouns(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        return pronounRepository.findAll();
    }

    @PostMapping(value = "/hunger-sim/pronoun", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Pronoun newPronoun(HttpServletRequest httpServletRequest, @RequestBody PronounDto pronoun) throws Exception {
        checkSession(httpServletRequest, true);

        if (Stream.of(pronoun.subject, pronoun.object, pronoun.possessive, pronoun.self).anyMatch(StringUtils::isBlank)) {
            throw new IllegalArgumentException("subject, object, possessive, and self must not be empty");
        }

        return pronounRepository.save(new Pronoun(pronoun.subject, pronoun.object, pronoun.possessive, pronoun.self));
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
                    .orElseThrow(() -> new IllegalArgumentException("discordId not recognized"));

            try {
                return playerRepository.save(new Player(discordUser, pronouns));
            } catch (DataIntegrityViolationException e) {
                throw new IllegalArgumentException("Failed to save player, it's likely a player with this discordId already exists", e);
            }
        } else if (StringUtils.isNotBlank(player.name)) {
            try {
                return playerRepository.save(new Player(player.name, pronouns));
            } catch (DataIntegrityViolationException e) {
                throw new IllegalArgumentException("Failed to save player, it's likely a player with this name already exists", e);
            }
        }

        throw new IllegalArgumentException("Unable to save player, please set either name or discordId to a non-null value");
    }

    @PutMapping(value = "/hunger-sim/player/{playerId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Player updatePlayer(HttpServletRequest httpServletRequest, @PathVariable("playerId") Long playerId, @RequestBody PlayerDto player) throws Exception {
        checkSession(httpServletRequest, true);

        Player playerEntity = playerRepository.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Player by that ID not found"));

        Pronoun pronouns = pronounRepository.findById(Objects.requireNonNull(player.pronounId)).orElseThrow(() -> new IllegalArgumentException("Pronouns not found by id"));
        playerEntity.setPronoun(pronouns);

        if (player.discordId != null) {
            DiscordUser discordUser = discordUserRepository.findById(String.valueOf(player.discordId))
                    .orElseThrow(() -> new IllegalArgumentException("discordId not recognized"));

            playerEntity.setDiscordUser(discordUser);
        } else if (StringUtils.isNotBlank(player.name)) {
            playerEntity.setName(player.name);
        } else {
            throw new IllegalArgumentException("Unable to update player, please set either name or discorId");
        }

        return playerRepository.save(playerEntity);
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
        return outcomeRepository.findAll();
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

        return outcomeRepository.save(new Outcome(
                outcome.outcomeText, outcome.numPlayers, outcome.player1Hp, outcome.player2Hp, outcome.player3Hp, outcome.player4Hp));
    }

    @PutMapping(value = "/hunger-sim/outcome/{outcomeId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Outcome updateOutcome(HttpServletRequest httpServletRequest, @PathVariable("outcomeId") Long outcomeId, @RequestBody OutcomeDto outcome)
            throws Exception {
        checkSession(httpServletRequest, true);

        validateOutcome(outcome);

        Outcome outcomeEntity = outcomeRepository.findById(outcomeId).orElseThrow(() -> new IllegalArgumentException("Outcome by that ID does not exist"));

        outcomeEntity.setNumPlayers(outcome.numPlayers);
        outcomeEntity.setOutcomeText(outcome.outcomeText);
        outcomeEntity.setPlayer1Hp(outcome.player1Hp);
        outcomeEntity.setPlayer2Hp(outcome.player2Hp);
        outcomeEntity.setPlayer3Hp(outcome.player3Hp);
        outcomeEntity.setPlayer4Hp(outcome.player4Hp);

        return outcomeRepository.save(outcomeEntity);
    }

    @DeleteMapping(value = "/hunger-sim/outcome/{outcomeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteOutcome(HttpServletRequest httpServletRequest, @PathVariable("outcomeId") Long outcomeId) throws Exception {
        checkSession(httpServletRequest, true);
        Outcome outcome = outcomeRepository.findById(outcomeId).orElseThrow(() -> new IllegalArgumentException("Outcome by that ID does not exist"));
        roundOutcomeRepository.deleteByOutcome(outcome);
        outcomeRepository.delete(outcome);
    }

    /** ROUNDS **/

    @GetMapping(value = "/hunger-sim/round", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Round> getRounds(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        List<Round> rounds = roundRepository.findAll();

        for (Round round : rounds) {
            List<Outcome> outcomes = roundOutcomeRepository.findByRound(round).stream().map(RoundOutcome::getOutcome).toList();
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

        Round savedRound = roundRepository.save(new Round(round.name, round.description, round.openingRound));

        List<Outcome> outcomes = outcomeRepository.findAllById(round.outcomeIds.stream().map(Long::parseLong).toList());
        for (Outcome outcome : outcomes) {
            roundOutcomeRepository.save(new RoundOutcome(savedRound, outcome));
        }

        return roundRepository.findById(savedRound.getId()).orElseThrow(() -> new IllegalArgumentException("Failed to save round"));
    }

    @PutMapping(value = "/hunger-sim/round/{roundId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Round updateRound(HttpServletRequest httpServletRequest, @PathVariable("roundId") Long roundId, @RequestBody RoundDto round) throws Exception {
        checkSession(httpServletRequest, true);

        if (CollectionUtils.isEmpty(round.outcomeIds)) {
            throw new IllegalArgumentException("A round must include at least one outcome");
        }

        Round existingRound = roundRepository.findById(roundId).orElseThrow(() -> new IllegalArgumentException("Could not found round with that ID"));

        existingRound.setName(round.name);
        existingRound.setDescription(round.description);
        existingRound.setOpeningRound(round.openingRound);

        Set<Long> outcomeIds = round.outcomeIds.stream().map(Long::parseLong).collect(Collectors.toSet());
        for (RoundOutcome roundOutcome : roundOutcomeRepository.findByRound(existingRound)) {
            if (!outcomeIds.contains(roundOutcome.getOutcome().getId())) {
                roundOutcomeRepository.delete(roundOutcome);
            }
        }

        for (Long outcomeId : outcomeIds) {
            Outcome outcome = outcomeRepository.findById(outcomeId)
                    .orElseThrow(() -> new IllegalArgumentException("Outcome id " + outcomeId + " does not exist"));

            if (!roundOutcomeRepository.existsByRoundAndOutcome(existingRound, outcome)) {
                roundOutcomeRepository.save(new RoundOutcome(existingRound, outcome));
            }
        }

        return roundRepository.save(existingRound);
    }

    @DeleteMapping(value = "/hunger-sim/round/{roundId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteRound(HttpServletRequest httpServletRequest, @PathVariable("roundId") Long roundId) throws Exception {
        checkSession(httpServletRequest, true);

        Round round = roundRepository.findById(roundId).orElseThrow(() -> new IllegalArgumentException("Round by that ID does not exist"));
        roundOutcomeRepository.deleteByRound(round);
        roundRepository.delete(round);
    }

    /** GAME **/

    @PostMapping(value = "/hunger-sim/game", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Game newGame(HttpServletRequest httpServletRequest, @RequestBody GameDto game) throws Exception {
        checkSession(httpServletRequest, true);

        Game gameEntity = gameRepository.findById(Game.SINGLETON_ID).orElse(new Game());

        List<Round> openingRound = roundRepository.findByOpeningRound(true);
        if (CollectionUtils.isEmpty(openingRound)) {
            throw new IllegalArgumentException("You must make one round and mark it as \"Opening Round\"");
        }

        if (game.playerIds.size() < 2) {
            throw new IllegalArgumentException("You can't start a game with less than 2 players");
        }

        List<Player> players = playerRepository.findAllById(game.playerIds);

        for (Player player : players) {
            player.setHp(10);
        }

        players = playerRepository.saveAll(players);

        gameEntity.setId(Game.SINGLETON_ID);
        gameEntity.setName(game.name);
        gameEntity.setRound(openingRound.get(0));
        gameEntity.setCurrentOutcomes(Collections.singletonList("Game has not started"));
        gameEntity.setPlayers(players);

        return gameRepository.save(gameEntity);
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

        if (outcome.player1Hp < -10 || outcome.player1Hp > 10) {
            throw new IllegalArgumentException("player1Hp must be between -10 and 10 inclusive");
        }
        if (outcome.player2Hp < -10 || outcome.player2Hp > 10) {
            throw new IllegalArgumentException("player2Hp must be between -10 and 10 inclusive");
        }
        if (outcome.player3Hp < -10 || outcome.player3Hp > 10) {
            throw new IllegalArgumentException("player3Hp must be between -10 and 10 inclusive");
        }
        if (outcome.player4Hp < -10 || outcome.player4Hp > 10) {
            throw new IllegalArgumentException("player4Hp must be between -10 and 10 inclusive");
        }
    }

}
