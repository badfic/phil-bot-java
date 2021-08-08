package com.badfic.philbot.listeners.antonia;

import com.badfic.philbot.config.Constants;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AntoniaMessageListener {

    private static final Pattern ANTONIA_PATTERN = Constants.compileWords("antonia|tony|stark|tash|iron man|tin can");
    private static final Multimap<String, Pair<Pattern, String>> USER_TRIGGER_WORDS = ImmutableMultimap.<String, Pair<Pattern, String>>builder()
            .put("307611036134146080", ImmutablePair.of(Constants.compileWords("I love you"), "I know"))
            .put("323520695550083074", ImmutablePair.of(Constants.compileWords("togna"), "bologna"))
            .put("323520695550083074", ImmutablePair.of(Constants.compileWords("child"), "Yes father?"))
            .build();
    private static final ConcurrentMap<Long, Pair<String, Long>> LAST_WORD_MAP = new ConcurrentHashMap<>();

    private final AntoniaCommand antoniaCommand;

    @Autowired
    public AntoniaMessageListener(AntoniaCommand antoniaCommand) {
        this.antoniaCommand = antoniaCommand;
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw();

        if (StringUtils.isBlank(msgContent) || msgContent.startsWith("!!") || event.getAuthor().isBot()) {
            return;
        }

        long channelId = event.getMessage().getChannel().getIdLong();
        LAST_WORD_MAP.compute(channelId, (key, oldValue) -> {
            if (oldValue == null) {
                return new ImmutablePair<>(msgContent, 1L);
            }
            if (oldValue.getLeft().equalsIgnoreCase(msgContent)) {
                if (oldValue.getRight() + 1 >= 3 && "bird".equalsIgnoreCase(msgContent)) {
                    antoniaCommand.getAntoniaJda().getTextChannelById(channelId).sendMessage("the bird is the word").queue();
                    return new ImmutablePair<>(msgContent, 0L);
                }
                if (oldValue.getRight() + 1 >= 3 && "word".equalsIgnoreCase(msgContent)) {
                    antoniaCommand.getAntoniaJda().getTextChannelById(channelId).sendMessage("the word is the bird").queue();
                    return new ImmutablePair<>(msgContent, 0L);
                }

                return new ImmutablePair<>(msgContent, oldValue.getRight() + 1);
            } else {
                return new ImmutablePair<>(msgContent, 1L);
            }
        });

        if (StringUtils.containsIgnoreCase(msgContent, "kite man")) {
            event.getJDA().getGuilds().get(0).getTextChannelById(event.getChannel().getId())
                    .sendMessage("Hell yea").queue();
            return;
        }
        if (StringUtils.containsIgnoreCase(msgContent, "owen wilson")) {
            event.getJDA().getGuilds().get(0).getTextChannelById(event.getChannel().getId())
                    .sendMessage("wow").queue();
            return;
        }

        Constants.checkUserTriggerWords(event, USER_TRIGGER_WORDS);

        if (ANTONIA_PATTERN.matcher(msgContent).find()) {
            antoniaCommand.execute(new CommandEvent(event, null, null));
            return;
        }
    }

}
