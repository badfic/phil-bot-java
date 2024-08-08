package com.badfic.philbot.web.members;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HungerSimController extends BaseMembersController {

    /** PAGES **/

    @GetMapping(value = "/hunger-sim", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getMainPage(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Hunger Sim");
        addCommonProps(httpServletRequest.getSession(), props);

        return new ModelAndView("hunger-sim", props);
    }

    @GetMapping(value = "/hunger-sim-pronouns", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getPronounsPage(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Hunger Sim - Pronouns");
        addCommonProps(httpServletRequest.getSession(), props);

        return new ModelAndView("hunger-sim-pronouns", props);
    }

    @GetMapping(value = "/hunger-sim-players", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getPlayersPage(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Hunger Sim - Players");
        addCommonProps(httpServletRequest.getSession(), props);

        return new ModelAndView("hunger-sim-players", props);
    }

    @GetMapping(value = "/hunger-sim-outcomes", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getOutcomesPage(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Hunger Sim - Outcomes");
        addCommonProps(httpServletRequest.getSession(), props);

        return new ModelAndView("hunger-sim-outcomes", props);
    }

    @GetMapping(value = "/hunger-sim-rounds", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRoundsPage(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Hunger Sim - Rounds");
        addCommonProps(httpServletRequest.getSession(), props);

        return new ModelAndView("hunger-sim-rounds", props);
    }

    @GetMapping(value = "/hunger-sim-new-game", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getNewGamePage(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Hunger Sim - New Game");
        addCommonProps(httpServletRequest.getSession(), props);

        return new ModelAndView("hunger-sim-new-game", props);
    }

    @GetMapping(value = "/hunger-sim-game", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRunGamePage(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Hunger Sim - Run Game");
        addCommonProps(httpServletRequest.getSession(), props);

        return new ModelAndView("hunger-sim-game", props);
    }

}
