package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
class QuickPoll extends BaseBangCommand {
    QuickPoll() {
        name = "qp";
        aliases = new String[] {"quickpoll"};
        help = "Start a quick poll with a Yes and No option.\nExample `!!qp Should I eat taco bell`";
    }

    @Override
    public void execute(final CommandEvent event) {
        if (StringUtils.isBlank(event.getArgs())) {
            event.replyError("Please enter a question. Example `!!qp Should I eat taco bell`");
            return;
        }

        final var messageEmbed = Constants.simpleEmbedThumbnail(
                "Quick Poll",
                event.getAuthor().getAsMention() + " asks:\n\n" + event.getArgs(),
                "https://emoji.gg/assets/emoji/4392_ablobcouncil.gif");

        event.reply(messageEmbed, msg -> msg.addReaction(Emoji.fromUnicode("❌")).submit().thenRun(() -> msg.addReaction(Emoji.fromUnicode("✅")).queue()));
    }
}
