package com.badfic.philbot.web.members;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.ModHelpAware;
import com.jagrosh.jdautilities.command.Command;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CommandsController extends BaseMembersController {

    private final List<Command> commands;

    public CommandsController(List<Command> commands) {
        this.commands = commands;
    }

    @GetMapping(value = "/commands", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);
        boolean isMod = Boolean.TRUE.equals(httpServletRequest.getSession().getAttribute(DISCORD_IS_MOD));

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
        addCommonProps(httpServletRequest, props);
        props.put("commands", simpleCommandsList);

        return new ModelAndView("commands", props);
    }

    private static Object[] emptyToNull(Object[] array) {
        return ArrayUtils.isEmpty(array) ? null : array;
    }

    private static String getModHelp(boolean isMod, Command command) {
        if (isMod && command instanceof ModHelpAware) {
            return ((ModHelpAware) command).getModHelp();
        }
        return null;
    }

    public record SimpleCommand(String name, String aliases, String requiredRole, String help, String modHelp) {}

}
