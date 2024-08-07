package com.badfic.philbot.web.swamp;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.service.DailyLegendsMemeService;
import com.badfic.philbot.service.DailyMarvelMemeService;
import com.badfic.philbot.service.DailyRiverdaleMemeService;
import com.badfic.philbot.service.HowWeBecameCursedService;
import com.badfic.philbot.service.HungerGamesWinnersService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class SwampController {

    private final BaseConfig baseConfig;
    private final HungerGamesWinnersService hungerGamesWinnersService;
    private final HowWeBecameCursedService howWeBecameCursedService;
    private final DailyMarvelMemeService dailyMarvelMemeService;
    private final DailyRiverdaleMemeService dailyRiverdaleMemeService;
    private final DailyLegendsMemeService dailyLegendsMemeService;

    @GetMapping(value = {
            "/chat-popout",
            "/commands",
            "/games-config",
            "/leaderboard",
            "/logout",
            "/meme-commands",
            "/nsfw-quotes",
            "/quotes",
            "/ranks",
            "/reminders",
            "/trivia"
    })
    public ResponseEntity<String> getOldCommandsPage(final HttpServletRequest httpServletRequest) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, baseConfig.hostname + "/members" + httpServletRequest.getRequestURI())
                .build();
    }

    @GetMapping(value = {"/", "/index.html", "/index"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getHome() {
        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Home — The Swamp");
        return new ModelAndView("swamp-home", props);
    }

    @GetMapping(value = {"/the-swampys.html", "/the-swampys"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getSwampys() {
        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "The Swampys — The Swamp");
        return new ModelAndView("the-swampys", props);
    }

    @GetMapping(value = {"/daily-riverdale-memes.html", "/daily-riverdale-memes"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRiverdaleMemes() {
        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Riverdale Memes — The Swamp");
        props.put("messages", dailyRiverdaleMemeService.getMessages());
        return new ModelAndView("riverdale-memes", props);
    }

    @GetMapping(value = {"/daily-marvel-memes.html", "/daily-marvel-memes"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getMarvelMemes() {
        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Marvel Memes — The Swamp");
        props.put("messages", dailyMarvelMemeService.getMessages());
        return new ModelAndView("marvel-memes", props);
    }

    @GetMapping(value = {"/daily-legends-memes.html", "/daily-legends-memes"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getLegendsMemes() {
        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Legends of Tomorrow Memes — The Swamp");
        props.put("messages", dailyLegendsMemeService.getMessages());
        return new ModelAndView("legends-memes", props);
    }

    @GetMapping(value = {"/cursed-timeline.html", "/cursed-timeline"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getCursedTimeline() {
        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Cursed Timeline — The Swamp");
        props.put("messages", howWeBecameCursedService.getMessages());
        return new ModelAndView("cursed-timeline", props);
    }

    @GetMapping(value = {"/hunger-games-winners.html", "/hunger-games-winners"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getHungerGames() {
        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Cursed Timeline — The Swamp");
        props.put("messages", hungerGamesWinnersService.getMessages());
        return new ModelAndView("hunger-games", props);
    }

    @GetMapping(value = {"/about-the-swamp.html", "/about-the-swamp"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getAbout() {
        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "About — The Swamp");
        return new ModelAndView("about-the-swamp", props);
    }

}
