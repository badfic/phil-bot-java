package com.badfic.philbot.listeners.antonia;

import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AntoniaMessageListener extends ListenerAdapter {

    private static final Pattern ANTONIA_PATTERN = Pattern.compile("\\b(antonia|toni|tony|stark|tash|iron man|tin can)\\b", Pattern.CASE_INSENSITIVE);

    private final AntoniaCommand antoniaCommand;

    @Autowired
    public AntoniaMessageListener(AntoniaCommand antoniaCommand) {
        this.antoniaCommand = antoniaCommand;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith("!!") || event.getAuthor().isBot()) {
            return;
        }

        if (ANTONIA_PATTERN.matcher(msgContent).find()) {
            antoniaCommand.execute(new CommandEvent(event, null, null));
            return;
        }
    }

}
