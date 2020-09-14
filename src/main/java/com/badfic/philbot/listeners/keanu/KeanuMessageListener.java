package com.badfic.philbot.listeners.keanu;

import com.badfic.philbot.config.KeanuMarker;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class KeanuMessageListener extends ListenerAdapter implements KeanuMarker {

    private static final Pattern KEANU_PATTERN = Pattern.compile("\\b(keanu|reeves|neo|john wick|puppy|puppies|pupper|doggo|doge)\\b", Pattern.CASE_INSENSITIVE);

    private final KeanuCommand keanuCommand;
    private final CommandClient keanuCommandClient;

    @Autowired
    public KeanuMessageListener(KeanuCommand keanuCommand,
                                @Qualifier("keanuCommandClient") CommandClient keanuCommandClient) {
        this.keanuCommand = keanuCommand;
        this.keanuCommandClient = keanuCommandClient;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith("!!") || event.getAuthor().isBot()) {
            return;
        }

        if (KEANU_PATTERN.matcher(msgContent).find()) {
            keanuCommand.execute(new CommandEvent(event, null, keanuCommandClient));
            return;
        }
    }

}
