package com.badfic.philbot.listeners.behrad;

import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BehradMessageListener extends ListenerAdapter {

    private static final Pattern BEHRAD_PATTERN = Pattern.compile("\\b(behrad|shayan|sobhian|marijuana|weed|420|stoned|stoner|kush|hey b|sup sloth)\\b", Pattern.CASE_INSENSITIVE);

    private final BehradCommand behradCommand;

    @Autowired
    public BehradMessageListener(BehradCommand behradCommand) {
        this.behradCommand = behradCommand;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith("!!") || event.getAuthor().isBot()) {
            return;
        }

        if (BEHRAD_PATTERN.matcher(msgContent).find()) {
            behradCommand.execute(new CommandEvent(event, null, null));
            return;
        }

        if (StringUtils.containsIgnoreCase(msgContent, "i'm gay")) {
            event.getJDA().getGuilds().get(0).getTextChannelById(event.getChannel().getId()).sendMessage("same").queue();
            return;
        }
    }

}
