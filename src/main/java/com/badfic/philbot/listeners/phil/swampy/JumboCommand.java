package com.badfic.philbot.listeners.phil.swampy;

import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.List;
import net.dv8tion.jda.api.entities.Emote;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
public class JumboCommand extends BaseSwampy {

    public JumboCommand() {
        name = "jumbo";
        help = "Jumbo an emote\n" +
                "`!!jumbo :shrekphil:`";
    }

    @Override
    protected void execute(CommandEvent event) {
        List<Emote> emotes = event.getMessage().getEmotes();

        if (CollectionUtils.size(emotes) != 1) {
            event.replyError("Please specify one emote");
            return;
        }

        Emote emote = emotes.get(0);
        event.reply(emote.getImageUrl());
    }
}
