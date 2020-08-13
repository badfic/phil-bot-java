package com.badfic.philbot.listeners;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageListener extends ListenerAdapter {

    private static final Pattern PHIL_PATTERN = Pattern.compile("\\b(phil|klemmer)\\b", Pattern.CASE_INSENSITIVE);

    private final PhilCommand philCommand;
    private final CommandClient commandClient;

    @Autowired
    public MessageListener(PhilCommand philCommand, CommandClient commandClient) {
        this.philCommand = philCommand;
        this.commandClient = commandClient;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw();

        if (!msgContent.startsWith("!!") && !event.getAuthor().isBot() && PHIL_PATTERN.matcher(msgContent).matches()) {
            philCommand.execute(new CommandEvent(event, null, commandClient));
        }
    }

}
