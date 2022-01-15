package com.badfic.philbot.web.members;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HungerSimController extends BaseMembersController {

    /** PAGES **/

    @GetMapping(value = "/hunger-sim", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getMainPage(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Hunger Sim");
        addCommonProps(httpServletRequest, props);

        return new ModelAndView("hunger-sim", props);
    }

    @GetMapping(value = "/hunger-sim-pronouns", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getPronounsPage(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Hunger Sim - Pronouns");
        addCommonProps(httpServletRequest, props);

        return new ModelAndView("hunger-sim-pronouns", props);
    }

    @GetMapping(value = "/hunger-sim-players", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getPlayersPage(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Hunger Sim - Players");
        addCommonProps(httpServletRequest, props);

        return new ModelAndView("hunger-sim-players", props);
    }

    @GetMapping(value = "/hunger-sim-outcomes", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getOutcomesPage(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Hunger Sim - Outcomes");
        addCommonProps(httpServletRequest, props);

        return new ModelAndView("hunger-sim-outcomes", props);
    }

    @GetMapping(value = "/hunger-sim-rounds", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRoundsPage(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Hunger Sim - Rounds");
        addCommonProps(httpServletRequest, props);

        return new ModelAndView("hunger-sim-rounds", props);
    }

    @GetMapping(value = "/hunger-sim-new-game", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getNewGamePage(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Hunger Sim - New Game");
        addCommonProps(httpServletRequest, props);

        return new ModelAndView("hunger-sim-new-game", props);
    }

    @GetMapping(value = "/hunger-sim-game", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRunGamePage(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Hunger Sim - Run Game");
        addCommonProps(httpServletRequest, props);

        return new ModelAndView("hunger-sim-game", props);
    }

}
