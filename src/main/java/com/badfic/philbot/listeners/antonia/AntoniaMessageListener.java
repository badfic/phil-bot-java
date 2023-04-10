package com.badfic.philbot.listeners.antonia;

import com.badfic.philbot.config.Constants;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class AntoniaMessageListener {

    private static final Pattern ANTONIA_PATTERN = Constants.compileWords("antonia|toni|tony|stark|tash|iron man|tin can");
    private static final Map<String, List<Pair<Pattern, String>>> USER_TRIGGER_WORDS = Map.of(
            "307611036134146080", List.of(ImmutablePair.of(Constants.compileWords("I love you"), "I know")),
            "323520695550083074", List.of(ImmutablePair.of(Constants.compileWords("togna"), "bologna"),
                    ImmutablePair.of(Constants.compileWords("child"), "Yes father?")));
    private static final ConcurrentMap<Long, Pair<String, Long>> LAST_WORD_MAP = new ConcurrentHashMap<>();

    private final AntoniaCommand antoniaCommand;

    public AntoniaMessageListener(AntoniaCommand antoniaCommand) {
        this.antoniaCommand = antoniaCommand;
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw();

        if (StringUtils.isBlank(msgContent) || msgContent.startsWith(Constants.PREFIX) || event.getAuthor().isBot()) {
            return;
        }

        long channelId = event.getMessage().getChannel().getIdLong();
        LAST_WORD_MAP.compute(channelId, (key, oldValue) -> {
            if (oldValue == null) {
                return new ImmutablePair<>(msgContent, 1L);
            }
            if (oldValue.getLeft().equalsIgnoreCase(msgContent)) {
                if (oldValue.getRight() + 1 >= 3 && "bird".equalsIgnoreCase(msgContent.trim())) {
                    antoniaCommand.getAntoniaJda().getTextChannelById(channelId).sendMessage("the bird is the word").queue();
                    return new ImmutablePair<>(msgContent, 0L);
                }
                if (oldValue.getRight() + 1 >= 3 && "word".equalsIgnoreCase(msgContent.trim())) {
                    antoniaCommand.getAntoniaJda().getTextChannelById(channelId).sendMessage("the word is the bird").queue();
                    return new ImmutablePair<>(msgContent, 0L);
                }
                if (oldValue.getRight() + 1 >= 3 && "mattgrinch".equalsIgnoreCase(msgContent.trim())) {
                    antoniaCommand.getAntoniaJda()
                            .getTextChannelById(channelId)
                            .sendMessage("https://cdn.discordapp.com/attachments/707453916882665552/914409167610056734/unknown.png")
                            .queue();
                    return new ImmutablePair<>(msgContent, 0L);
                }

                return new ImmutablePair<>(msgContent, oldValue.getRight() + 1);
            } else {
                return new ImmutablePair<>(msgContent, 1L);
            }
        });

        if (StringUtils.containsIgnoreCase(msgContent, "kite man")) {
            antoniaCommand.getAntoniaJda().getTextChannelById(channelId)
                    .sendMessage("Hell yea").queue();
            return;
        }
        if (StringUtils.containsIgnoreCase(msgContent, "owen wilson")) {
            antoniaCommand.getAntoniaJda().getTextChannelById(channelId)
                    .sendMessage("wow").queue();
            return;
        }

        Constants.checkUserTriggerWords(event, USER_TRIGGER_WORDS);

        if (ANTONIA_PATTERN.matcher(msgContent).find()) {
            antoniaCommand.execute(new CommandEvent(event, Constants.PREFIX, null, null));
            return;
        }
    }

}
