package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.PhilMarker;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class QuickPoll extends BaseSwampy implements PhilMarker {

    public QuickPoll() {
        name = "qp";
        aliases = new String[] {"quickpoll"};
        help = "!!qp\nExample `!!qp Should I eat taco bell`";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (StringUtils.isBlank(event.getArgs())) {
            event.replyError("Please enter a question. Example `!!qp Should I eat taco bell`");
            return;
        }

        event.reply(event.getAuthor().getAsMention() + " asks: " + event.getArgs(), msg -> {
            msg.addReaction("✅").queue();
            msg.addReaction("❌").queue();
        });
    }
}
