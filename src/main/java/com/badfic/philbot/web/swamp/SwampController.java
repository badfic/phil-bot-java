package com.badfic.philbot.web.swamp;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.service.DailyMarvelMemeService;
import com.badfic.philbot.service.DailyRiverdaleMemeService;
import com.badfic.philbot.service.HowWeBecameCursedService;
import com.badfic.philbot.service.HungerGamesWinnersService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SwampController {

    @Autowired
    private BaseConfig baseConfig;

    @Autowired
    private HungerGamesWinnersService hungerGamesWinnersService;

    @Autowired
    private HowWeBecameCursedService howWeBecameCursedService;

    @Autowired
    private DailyMarvelMemeService dailyMarvelMemeService;

    @Autowired
    private DailyRiverdaleMemeService dailyRiverdaleMemeService;

    @GetMapping(value = {
            "/chat-popout",
            "/commands",
            "/games-config",
            "/leaderboard",
            "/live",
            "/logout",
            "/meme-commands",
            "/nsfw-quotes",
            "/quotes",
            "/ranks",
            "/reminders",
            "/trivia"
    })
    public ResponseEntity<String> getOldCommandsPage(HttpServletRequest httpServletRequest) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, baseConfig.hostname + "/members" + httpServletRequest.getRequestURI())
                .build();
    }

    @GetMapping(value = {"/", "/index.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getHome() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Home — The Swamp");
        return new ModelAndView("swamp-home", props);
    }

    @GetMapping(value = "/the-swampys.html", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getSwampys() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "The Swampys — The Swamp");
        return new ModelAndView("the-swampys", props);
    }

    @GetMapping(value = "/daily-riverdale-memes.html", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRiverdaleMemes() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Riverdale Memes — The Swamp");
        props.put("messages", dailyRiverdaleMemeService.getMessages());
        return new ModelAndView("riverdale-memes", props);
    }

    @GetMapping(value = "/daily-marvel-memes.html", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getMarvelMemes() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Marvel Memes — The Swamp");
        props.put("messages", dailyMarvelMemeService.getMessages());
        return new ModelAndView("marvel-memes", props);
    }

    @GetMapping(value = "/cursed-timeline.html", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getCursedTimeline() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Cursed Timeline — The Swamp");
        props.put("messages", howWeBecameCursedService.getMessages());
        return new ModelAndView("cursed-timeline", props);
    }

    @GetMapping(value = "/hunger-games-winners.html", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getHungerGames() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Cursed Timeline — The Swamp");
        props.put("messages", hungerGamesWinnersService.getMessages());
        return new ModelAndView("hunger-games", props);
    }

    @GetMapping(value = "/about-the-swamp.html", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getAbout() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "About — The Swamp");
        return new ModelAndView("about-the-swamp", props);
    }

}
