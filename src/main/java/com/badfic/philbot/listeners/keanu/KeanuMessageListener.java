package com.badfic.philbot.listeners.keanu;

import com.badfic.philbot.config.Constants;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class KeanuMessageListener {

    private static final Pattern KEANU_PATTERN = Constants.compileWords("keanu|reeves|neo|john wick|puppy|puppies|pupper|doggo|doge");
    private static final ListMultimap<String, Pair<Pattern, String>> USER_TRIGGER_WORDS = ImmutableListMultimap.<String, Pair<Pattern, String>>builder()
            .put("323520695550083074", ImmutablePair.of(Constants.compileWords("child"), "Yes father?"))
            .build();

    private final KeanuCommand keanuCommand;

    public KeanuMessageListener(KeanuCommand keanuCommand) {
        this.keanuCommand = keanuCommand;
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw();

        if (StringUtils.isBlank(msgContent) || msgContent.startsWith(Constants.PREFIX) || event.getAuthor().isBot()) {
            return;
        }

        if (StringUtils.containsIgnoreCase(msgContent, "lightning mcqueen")) {
            event.getJDA().getTextChannelById(event.getChannel().getId())
                    .sendMessage("KACHOW!").queue();
            return;
        }

        Constants.checkUserTriggerWords(event, USER_TRIGGER_WORDS);

        if (KEANU_PATTERN.matcher(msgContent).find()) {
            keanuCommand.execute(new CommandEvent(event, Constants.PREFIX, null, null));
            return;
        }
    }

}
