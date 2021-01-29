package com.badfic.philbot.web;

import com.jagrosh.jdautilities.command.Command;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
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

        List<SimpleCommand> simpleCommandsList = commands.stream()
                .filter(c -> !c.isOwnerCommand() && !c.getName().equalsIgnoreCase("fireDrill") && !c.getName().endsWith("Talk"))
                .map(command -> {
            String modHelp = null;
            try {
                modHelp = Arrays.stream(command.getClass().getDeclaredFields())
                        .filter(f -> "modhelp".equalsIgnoreCase(f.getName()))
                        .findAny()
                        .map(f -> {
                            try {
                                f.setAccessible(true);
                                return ((String) f.get(command));
                            } catch (IllegalAccessException ignored) {
                                return null;
                            }
                        }).orElse(null);
            } catch (Exception ignored) {}

            return new SimpleCommand(command.getName(), command.getAliases(), command.getRequiredRole(), command.getHelp(), modHelp);
        }).collect(Collectors.toList());

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Commands");
        props.put("username", httpServletRequest.getSession().getAttribute(DISCORD_USERNAME));
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
            this.aliases = String.join(", ", aliases);
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
