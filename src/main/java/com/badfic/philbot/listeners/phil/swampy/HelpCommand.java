package com.badfic.philbot.listeners.phil.swampy;

import com.jagrosh.jdautilities.command.CommandEvent;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand extends BaseSwampy {

    public HelpCommand() {
        name = "help";
        help = "`!!help` to get a link to the commands webpage that lists all of the commands.";
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply("Find help for commands here: " + baseConfig.hostname + "/commands");
    }

}
