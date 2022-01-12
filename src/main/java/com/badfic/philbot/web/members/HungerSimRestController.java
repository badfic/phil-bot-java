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
import com.badfic.philbot.data.hungersim.RoundRepository;
import com.badfic.philbot.service.HungerSimService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    public record RoundDto(String name, String description, Boolean openingRound, List<Long> outcomeIds) {}

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
    public void newPronoun(HttpServletRequest httpServletRequest, @PathVariable("pronounId") Long pronounId) throws Exception {
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
        return playerRepository.findAll();
    }

    @PostMapping(value = "/hunger-sim/player", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Player newPlayer(HttpServletRequest httpServletRequest, @RequestBody PlayerDto player) throws Exception {
        checkSession(httpServletRequest, true);

        Pronoun pronouns = pronounRepository.findById(player.pronounId).orElseThrow(() -> new IllegalArgumentException("Pronouns not found by id"));

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

        Pronoun pronouns = pronounRepository.findById(player.pronounId).orElseThrow(() -> new IllegalArgumentException("Pronouns not found by id"));
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
        outcomeRepository.delete(outcome);
    }

    /** ROUNDS **/

    @GetMapping(value = "/hunger-sim/round", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Round> getRounds(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        return roundRepository.findAll();
    }

    @PostMapping(value = "/hunger-sim/round", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Round newRound(HttpServletRequest httpServletRequest, @RequestBody RoundDto round) throws Exception {
        checkSession(httpServletRequest, true);

        List<Outcome> outcomes = outcomeRepository.findAllById(round.outcomeIds);

        return roundRepository.save(new Round(round.name, round.description, round.openingRound, outcomes));
    }

    @PutMapping(value = "/hunger-sim/round/{roundId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Round updateRound(HttpServletRequest httpServletRequest, @PathVariable("roundId") Long roundId, @RequestBody RoundDto round) throws Exception {
        checkSession(httpServletRequest, true);

        Round existingRound = roundRepository.findById(roundId).orElseThrow(() -> new IllegalArgumentException("Could not found round with that ID"));

        existingRound.setName(round.name);
        existingRound.setDescription(round.description);
        existingRound.setOpeningRound(round.openingRound);

        List<Outcome> outcomes = outcomeRepository.findAllById(round.outcomeIds);
        existingRound.setOutcomes(outcomes);

        return roundRepository.save(existingRound);
    }

    @DeleteMapping(value = "/hunger-sim/round/{roundId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteRound(HttpServletRequest httpServletRequest, @PathVariable("roundId") Long roundId) throws Exception {
        checkSession(httpServletRequest, true);

        Round round = roundRepository.findById(roundId).orElseThrow(() -> new IllegalArgumentException("Round by that ID does not exist"));
        roundRepository.delete(round);
    }

    /** GAME **/

    @PostMapping(value = "/hunger-sim/game", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Game newGame(HttpServletRequest httpServletRequest, @RequestBody GameDto game) throws Exception {
        checkSession(httpServletRequest, true);

        if (game.playerIds.size() < 2) {
            throw new IllegalArgumentException("You can't start a game with less than 2 players");
        }

        gameRepository.deleteAll();

        List<Player> players = playerRepository.findAllById(game.playerIds);

        for (Player player : players) {
            player.setHp(10);
        }

        playerRepository.saveAll(players);

        return gameRepository.save(new Game(game.name, players));
    }

    @GetMapping(value = "/hunger-sim/game", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game getGame(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        return hungerSimService.getGame();
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
    }

}
