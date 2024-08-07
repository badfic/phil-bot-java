package com.badfic.philbot.commands.bang;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.config.Constants;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.springframework.stereotype.Component;

@Component
class ModMail extends BaseBangCommand {
    ModMail() {
        name = "mods";
        aliases = new String[] {"modMail"};
        guildOnly = false;
        help = """
                !!mods
                Send a message to the mods.
                You can DM phil to ask a question anonymously.
                Example: !!mods Hey mods I have a question about swampy stuff
                Or you can just send it in any channel if you don't care about anonymity.""";
    }

    @Override
    public void execute(final CommandEvent event) {
        final var modChannel = philJda
                .getTextChannelsByName("mod-chat-and-delegation", false)
                .stream()
                .findFirst();

        if (modChannel.isPresent()) {
            final var msg = Constants.simpleEmbed("Incoming Mod Mail",
                    "New mod mail from " + (event.getChannelType() == ChannelType.PRIVATE ? "anonymous user" : event.getAuthor().getAsMention())
                            + "\n\n" + event.getMessage().getContentRaw());
            modChannel.get().sendMessageEmbeds(msg).queue();
            event.replySuccess("Successfully sent your message along to the mods");
        } else {
            event.replyError("Failed to send your message to the mods, please let the mods know this command is broken");
        }
    }
}
