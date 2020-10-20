package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.PhilMarker;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Optional;
import javax.annotation.Resource;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class ModMail extends Command implements PhilMarker {

    @Resource(name = "philJda")
    @Lazy
    private JDA philJda;

    public ModMail() {
        name = "mods";
        guildOnly = false;
    }

    @Override
    public void execute(CommandEvent event) {
        Optional<TextChannel> modChannel = philJda.getGuilds()
                .get(0)
                .getTextChannelsByName("mod-chat-and-delegation", false)
                .stream()
                .findFirst();

        if (modChannel.isPresent()) {
            MessageEmbed msg = new EmbedBuilder()
                    .setTitle("Incoming Mod Mail")
                    .setDescription("New mod mail from " + (event.getChannelType() == ChannelType.PRIVATE ? "anonymous user" : event.getAuthor().getAsMention())
                            + "\n\n" + event.getMessage().getContentRaw())
                    .build();
            modChannel.get().sendMessage(msg).queue();
            event.replySuccess("Successfully sent your message along to the mods");
        } else {
            event.replyError("Failed to send your message to the mods, please let the mods know this command is broken");
        }
    }
}
