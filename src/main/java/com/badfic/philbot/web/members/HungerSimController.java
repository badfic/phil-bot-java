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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HungerSimController extends BaseMembersController {

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

    @GetMapping(value = "/hunger-sim", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Games Config");
        addCommonProps(httpServletRequest, props);

        return new ModelAndView("hunger-sim", props);
    }

    @PostMapping(value = "/hunger-sim/game", produces = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/hunger-sim/outcome", produces = MediaType.APPLICATION_JSON_VALUE)
    public Outcome newOutcome(HttpServletRequest httpServletRequest, @RequestBody OutcomeDto outcome) throws Exception {
        checkSession(httpServletRequest, true);
        return outcomeRepository.save(new Outcome(
                outcome.outcomeText, outcome.numPlayers, outcome.player1Hp, outcome.player2Hp, outcome.player3Hp, outcome.player4Hp));
    }

    @GetMapping(value = "/hunger=sim/outcome", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Outcome> getOutcomes(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        return outcomeRepository.findAll();
    }

    @PostMapping(value = "/hunger-sim/player", produces = MediaType.APPLICATION_JSON_VALUE)
    public Player newPlayer(HttpServletRequest httpServletRequest, PlayerDto player) throws Exception {
        checkSession(httpServletRequest, true);

        Pronoun pronouns = pronounRepository.findById(player.pronounId).orElseThrow(() -> new IllegalArgumentException("Pronouns not found by id"));

        if (player.discordId != null) {
            DiscordUser discordUser = discordUserRepository.findById(String.valueOf(player.discordId))
                    .orElseThrow(() -> new IllegalArgumentException("discordId not recognized"));

            return playerRepository.save(new Player(discordUser, pronouns));
        } else if (StringUtils.isNotBlank(player.name)) {
            return playerRepository.save(new Player(player.name, pronouns));
        }

        throw new IllegalArgumentException("Unable to save player, please set either name or discordId to a non-null value");
    }

    @GetMapping(value = "/hunger-sim/player", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Player> getPlayers(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        return playerRepository.findAll();
    }

    @PostMapping(value = "/hunger-sim/pronoun", produces = MediaType.APPLICATION_JSON_VALUE)
    public Pronoun newPronoun(HttpServletRequest httpServletRequest, PronounDto pronoun) throws Exception {
        checkSession(httpServletRequest, true);

        return pronounRepository.save(new Pronoun(pronoun.subject, pronoun.object, pronoun.possessive, pronoun.self));
    }

    @GetMapping(value = "/hunger-sim/pronoun", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Pronoun> getPronouns(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);
        return pronounRepository.findAll();
    }

    @PostMapping(value = "/hunger-sim/round", produces = MediaType.APPLICATION_JSON_VALUE)
    public Round newRound(HttpServletRequest httpServletRequest, RoundDto round) throws Exception {
        checkSession(httpServletRequest, true);

        List<Outcome> outcomes = outcomeRepository.findAllById(round.outcomeIds);

        return roundRepository.save(new Round(round.name, round.description, round.openingRound, outcomes));
    }

    @PutMapping(value = "/hunger-sim/round/{roundId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Round updateRound(HttpServletRequest httpServletRequest, @PathVariable("roundId") Long roundId, RoundDto round) throws Exception {
        checkSession(httpServletRequest, true);

        Round existingRound = roundRepository.findById(roundId).orElseThrow(() -> new IllegalArgumentException("Could not found round with that ID"));

        existingRound.setName(round.name);
        existingRound.setDescription(round.description);
        existingRound.setOpeningRound(round.openingRound);

        List<Outcome> outcomes = outcomeRepository.findAllById(round.outcomeIds);
        existingRound.setOutcomes(outcomes);

        return roundRepository.save(existingRound);
    }

    public record GameDto(String name, Set<Long> playerIds) {}
    public record OutcomeDto(String outcomeText, Integer numPlayers, Integer player1Hp, Integer player2Hp, Integer player3Hp, Integer player4Hp) {}
    public record PlayerDto(String name, Long discordId, Long pronounId) {}
    public record PronounDto(String subject, String object, String possessive, String self) {}
    public record RoundDto(String name, String description, Boolean openingRound, List<Long> outcomeIds) {}

}
