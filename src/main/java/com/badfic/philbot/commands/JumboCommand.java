package com.badfic.philbot.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.List;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
public class JumboCommand extends BaseCommand {

    public JumboCommand() {
        name = "jumbo";
        help = "Jumbo an emote\n" +
                "`!!jumbo :shrekphil:`";
    }

    @Override
    protected void execute(CommandEvent event) {
        List<CustomEmoji> emotes = event.getMessage().getMentions().getCustomEmojis();

        if (CollectionUtils.size(emotes) != 1) {
            event.replyError("Please specify one emote");
            return;
        }

        CustomEmoji emote = emotes.get(0);
        event.reply(emote.getImageUrl());
    }
}
