package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.MessageEmbed;
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

        MessageEmbed messageEmbed = Constants.simpleEmbedThumbnail(
                "Quick Poll",
                event.getAuthor().getAsMention() + " asks:\n\n" + event.getArgs(),
                "https://emoji.gg/assets/emoji/4392_ablobcouncil.gif");

        event.reply(messageEmbed, msg -> {
            msg.addReaction("❌").submit().thenRun(() -> msg.addReaction("✅").queue());
        });
    }
}
