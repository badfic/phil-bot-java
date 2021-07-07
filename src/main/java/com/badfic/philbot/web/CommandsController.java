package com.badfic.philbot.web;

import com.badfic.philbot.config.Constants;
import com.jagrosh.jdautilities.command.Command;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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

        List<SimpleCommand> simpleCommandsList = commands.stream()
                .filter(c -> {
                    if (c.isOwnerCommand() || (c.getName().endsWith("Talk") && !isMod)) {
                        return false;
                    }

                    return isMod || !StringUtils.equalsIgnoreCase(c.getRequiredRole(), Constants.ADMIN_ROLE);
                })
                .map(command -> {
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

                    return new SimpleCommand(command.getName(), command.getAliases(), command.getRequiredRole(), command.getHelp(), modHelp.getValue());
                })
                .sorted(Comparator.comparing(SimpleCommand::getName))
                .collect(Collectors.toList());

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Commands");
        props.put("username", httpServletRequest.getSession().getAttribute(DISCORD_USERNAME));
        props.put("isMod", httpServletRequest.getSession().getAttribute(DISCORD_IS_MOD));
        props.put("commands", simpleCommandsList);

        return new ModelAndView("commands", props);
    }

    public static class SimpleCommand {
        private final String name;
        private final String aliases;
        private final String requiredRole;
        private final String help;
        private final String modHelp;

        public SimpleCommand(String name, String[] aliases, String requiredRole, String help, String modHelp) {
            this.name = name;
            this.aliases = aliases.length > 0 ? String.join(", ", aliases) : null;
            this.requiredRole = requiredRole;
            this.help = help;
            this.modHelp = modHelp;
        }

        public String getName() {
            return name;
        }

        public String getAliases() {
            return aliases;
        }

        public String getRequiredRole() {
            return requiredRole;
        }

        public String getHelp() {
            return help;
        }

        public String getModHelp() {
            return modHelp;
        }
    }

}
