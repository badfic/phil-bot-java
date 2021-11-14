package com.badfic.philbot.web.members;

import com.badfic.philbot.data.phil.Rank;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class RanksController extends BaseMembersController {

    @GetMapping(value = "/ranks", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Ranks");
        addCommonProps(httpServletRequest, props);
        props.put("ranks", Rank.getAllRanks());

        return new ModelAndView("ranks", props);
    }

}
