package com.badfic.philbot.listeners.behrad;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.BehradMarker;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class BehradMessageListener extends ListenerAdapter implements BehradMarker {

    private static final Pattern BEHRAD_PATTERN = Pattern.compile("\\b(behrad|shayan|sobhian|marijuana|weed|420|stoned|high|stoner|kush)\\b", Pattern.CASE_INSENSITIVE);

    private final boolean isTestEnvironment;
    private final BehradCommand behradCommand;
    private final CommandClient behradCommandClient;

    @Autowired
    public BehradMessageListener(BaseConfig baseConfig,
                                 BehradCommand behradCommand,
                                 @Qualifier("behradCommandClient") CommandClient behradCommandClient) {
        isTestEnvironment = "test".equalsIgnoreCase(baseConfig.nodeEnvironment);
        this.behradCommand = behradCommand;
        this.behradCommandClient = behradCommandClient;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith("!!") || event.getAuthor().isBot()) {
            return;
        }

        if (BEHRAD_PATTERN.matcher(msgContent).find()) {
            behradCommand.execute(new CommandEvent(event, null, behradCommandClient));
            return;
        }

        if (isTestEnvironment && !"test-channel".equalsIgnoreCase(event.getChannel().getName())) {
            return;
        }

        if (StringUtils.containsIgnoreCase(msgContent, "i'm gay")) {
            event.getChannel().sendMessage("same").queue();
            return;
        }
    }

}
