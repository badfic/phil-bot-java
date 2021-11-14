package com.badfic.philbot.web.swamp;

import com.badfic.philbot.config.BaseConfig;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SwampController {

    @Resource
    private BaseConfig baseConfig;

    @GetMapping(value = "/commands")
    public ResponseEntity<String> getOldCommandsPage() {
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, baseConfig.hostname + "/members/commands").build();
    }

    @GetMapping(value = "/live")
    public ResponseEntity<String> getOldLivePage() {
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, baseConfig.hostname + "/members/live").build();
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

    @GetMapping(value = "/cursed-memes.html", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getCursedMemes() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Cursed Memes — The Swamp");
        return new ModelAndView("cursed-memes", props);
    }

    @GetMapping(value = "/daily-riverdale-memes.html", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRiverdaleMemes() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Riverdale Memes — The Swamp");
        return new ModelAndView("riverdale-memes", props);
    }

    @GetMapping(value = "/daily-marvel-memes.html", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getMarvelMemes() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Marvel Memes — The Swamp");
        return new ModelAndView("marvel-memes", props);
    }

    @GetMapping(value = "/cursed-timeline.html", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getCursedTimeline() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Cursed Timeline — The Swamp");
        return new ModelAndView("cursed-timeline", props);
    }

    @GetMapping(value = "/hunger-games-winners.html", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getHungerGames() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Cursed Timeline — The Swamp");
        return new ModelAndView("hunger-games", props);
    }

    @GetMapping(value = "/about-the-swamp.html", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getAbout() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "About — The Swamp");
        return new ModelAndView("about-the-swamp", props);
    }

}
