package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import org.springframework.stereotype.Component;

@Component
class HelpCommand extends BaseBangCommand {
    HelpCommand() {
        name = "help";
        help = "`!!help` to get a link to the commands webpage that lists all of the commands.";
    }

    @Override
    public void execute(final CommandEvent event) {
        event.reply("Find help for commands here: " + baseConfig.hostname + "/members/commands");
    }
}
