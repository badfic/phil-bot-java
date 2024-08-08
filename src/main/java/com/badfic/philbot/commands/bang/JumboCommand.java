package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
class JumboCommand extends BaseBangCommand {
    JumboCommand() {
        name = "jumbo";
        help = "Jumbo an emote\n" +
                "`!!jumbo :shrekphil:`";
    }

    @Override
    public void execute(final CommandEvent event) {
        final var emotes = event.getMessage().getMentions().getCustomEmojis();

        if (CollectionUtils.size(emotes) != 1) {
            event.replyError("Please specify one emote");
            return;
        }

        final var emote = emotes.getFirst();
        event.reply(emote.getImageUrl());
    }
}
