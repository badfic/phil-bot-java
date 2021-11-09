package com.badfic.philbot.web;

import com.badfic.philbot.config.Constants;
import com.jagrosh.jdautilities.command.Command;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.http.MediaType;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class CommandsController extends BaseController {

    @Resource
    private List<Command> commands;

    @GetMapping(value = "/commands", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);
        boolean isMod = Boolean.TRUE.equals(httpServletRequest.getSession().getAttribute(DISCORD_IS_MOD));

        MutableObject<SimpleCommand> swampyCommand = new MutableObject<>();

        List<SimpleCommand> simpleCommandsList = commands.stream()
                .filter(c -> {
                    if ("swampy".equalsIgnoreCase(c.getName())) {
                        swampyCommand.setValue(new SimpleCommand(c.getName(), StringUtils.join(emptyToNull(c.getAliases()), ", "), c.getRequiredRole(), c.getHelp(),
                                getModHelp(isMod, c).getValue()));
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
                .map(command -> {
                    final MutableObject<String> modHelp = getModHelp(isMod, command);

                    return new SimpleCommand(
                            command.getName(),
                            StringUtils.join(emptyToNull(command.getAliases()), ", "),
                            command.getRequiredRole(),
                            command.getHelp(),
                            modHelp.getValue());
                })
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

    private static MutableObject<String> getModHelp(boolean isMod, Command command) {
        final MutableObject<String> modHelp = new MutableObject<>();

        if (isMod) {
            try {
                ReflectionUtils.doWithFields(command.getClass(), field -> {
                    if ("modhelp".equalsIgnoreCase(field.getName())) {
                        try {
                            field.setAccessible(true);
                            modHelp.setValue(((String) field.get(command)));
                        } catch (IllegalAccessException ignored) {}
                    }
                });
            } catch (Exception ignored) {}
        }
        return modHelp;
    }

    public static record SimpleCommand(String name, String aliases, String requiredRole, String help, String modHelp) {}

}
