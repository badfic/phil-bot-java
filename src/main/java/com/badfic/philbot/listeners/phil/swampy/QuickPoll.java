package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.PhilMarker;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
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

        MessageEmbed messageEmbed = new EmbedBuilder()
                .setTitle("Quick Poll")
                .setDescription(event.getAuthor().getAsMention() + " asks:\n\n" + event.getArgs())
                .setThumbnail("https://emoji.gg/assets/emoji/4392_ablobcouncil.gif")
                .build();

        event.reply(messageEmbed, msg -> {
            msg.addReaction("❌").submit().thenRun(() -> msg.addReaction("✅").queue());
        });
    }
}
