package com.badfic.philbot.web.members;

import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.commands.ModHelpAware;
import com.badfic.philbot.config.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class CommandsController extends BaseMembersController {

    private final List<BaseNormalCommand> commands;

    @GetMapping(value = "/commands", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);
        HttpSession session = httpServletRequest.getSession();
        boolean isMod = Boolean.TRUE.equals(session.getAttribute(DISCORD_IS_MOD));

        MutableObject<SimpleCommand> swampyCommand = new MutableObject<>();

        List<SimpleCommand> simpleCommandsList = commands.stream()
                .filter(c -> {
                    if ("swampy".equalsIgnoreCase(c.getName())) {
                        swampyCommand.setValue(new SimpleCommand(c.getName(), StringUtils.join(emptyToNull(c.getAliases()), ", "), c.getRequiredRole(), c.getHelp(),
                                getModHelp(isMod, c)));
                        return false;
                    }

                    if (c.isOwnerCommand() || (
                            (c.getName().endsWith("Talk") ||
                                    c.getName().equals("antonia") ||
                                    c.getName().equals("behrad") ||
                                    c.getName().equals("phil") ||
                                    c.getName().equals("john") ||
                                    c.getName().equals("keanu")
                            ) && !isMod)) {
                        return false;
                    }

                    return isMod || !StringUtils.equalsIgnoreCase(c.getRequiredRole(), Constants.ADMIN_ROLE);
                })
                .map(command ->
                    new SimpleCommand(
                            command.getName(),
                            StringUtils.join(emptyToNull(command.getAliases()), ", "),
                            command.getRequiredRole(),
                            command.getHelp(),
                            getModHelp(isMod, command))
                )
                .sorted(Comparator.comparing(SimpleCommand::name))
                .collect(Collectors.toList());

        simpleCommandsList = Stream.concat(Stream.of(swampyCommand.getValue()), simpleCommandsList.stream()).collect(Collectors.toList());

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Commands");
        addCommonProps(session, props);
        props.put("commands", simpleCommandsList);

        return new ModelAndView("commands", props);
    }

    private static Object[] emptyToNull(Object[] array) {
        return ArrayUtils.isEmpty(array) ? null : array;
    }

    private static String getModHelp(boolean isMod, BaseNormalCommand command) {
        if (isMod && command instanceof ModHelpAware modHelpAware) {
            return modHelpAware.getModHelp();
        }
        return null;
    }

    public record SimpleCommand(String name, String aliases, String requiredRole, String help, String modHelp) {}

}
