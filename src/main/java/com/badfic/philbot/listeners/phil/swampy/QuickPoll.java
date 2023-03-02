package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class QuickPoll extends BaseSwampy {

    public QuickPoll() {
        name = "qp";
        aliases = new String[] {"quickpoll"};
        help = "Start a quick poll with a Yes and No option.\nExample `!!qp Should I eat taco bell`";
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

        event.reply(messageEmbed, msg -> msg.addReaction(Emoji.fromUnicode("❌")).submit().thenRun(() -> msg.addReaction(Emoji.fromUnicode("✅")).queue()));
    }
}
