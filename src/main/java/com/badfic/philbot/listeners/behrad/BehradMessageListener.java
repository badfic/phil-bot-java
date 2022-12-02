package com.badfic.philbot.listeners.behrad;

import com.badfic.philbot.config.Constants;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BehradMessageListener {

    private static final Pattern BEHRAD_PATTERN = Constants.compileWords("behrad|shayan|sobhian|marijuana|weed|420|stoned|stoner|kush|hey b|sup sloth");
    private static final Multimap<String, Pair<Pattern, String>> USER_TRIGGER_WORDS = ImmutableMultimap.<String, Pair<Pattern, String>>builder()
            .put("323520695550083074", ImmutablePair.of(Constants.compileWords("child"), "Yes father?"))
            .build();

    private final BehradCommand behradCommand;

    @Autowired
    public BehradMessageListener(BehradCommand behradCommand) {
        this.behradCommand = behradCommand;
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw();

        if (StringUtils.isBlank(msgContent) || msgContent.startsWith(Constants.PREFIX) || event.getAuthor().isBot()) {
            return;
        }

        Constants.checkUserTriggerWords(event, USER_TRIGGER_WORDS);

        if (BEHRAD_PATTERN.matcher(msgContent).find()) {
            behradCommand.execute(new CommandEvent(event, Constants.PREFIX, null, null));
            return;
        }

        if (StringUtils.containsIgnoreCase(msgContent, "i'm gay")) {
            event.getJDA().getGuilds().get(0).getTextChannelById(event.getChannel().getId()).sendMessage("same").queue();
            return;
        }

        if (StringUtils.containsIgnoreCase(msgContent, "salsa")) {
            event.getJDA().getGuilds().get(0).getTextChannelById(event.getChannel().getId()).sendMessage("you know the rule about salsa ( ͡° ͜ʖ ͡°)").queue();
            return;
        }
    }

}
