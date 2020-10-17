package com.badfic.philbot.listeners.keanu;

import com.badfic.philbot.config.KeanuMarker;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class KeanuMessageListener extends ListenerAdapter implements KeanuMarker {

    private static final Pattern KEANU_PATTERN = Pattern.compile("\\b(keanu|reeves|neo|john wick|puppy|puppies|pupper|doggo|doge)\\b", Pattern.CASE_INSENSITIVE);
    private static final Multimap<String, Pair<String, String>> USER_TRIGGER_WORDS = ImmutableMultimap.<String, Pair<String, String>>builder()
            .put("513187180198363136", ImmutablePair.of("husband", "Hi honey"))
            .put("513187180198363136", ImmutablePair.of("love", "I love you too"))
            .put("601043580945170443", ImmutablePair.of("dad", "Hi pumpkin"))
            .put("323520695550083074", ImmutablePair.of("son", "father"))
            .build();

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

        Collection<Pair<String, String>> userTriggers = USER_TRIGGER_WORDS.get(event.getAuthor().getId());
        if (CollectionUtils.isNotEmpty(userTriggers)) {
            Optional<Pair<String, String>> match = userTriggers.stream().filter(t -> StringUtils.containsIgnoreCase(msgContent, t.getLeft())).findAny();

            if (match.isPresent()) {
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", " + match.get().getRight()).queue();
                return;
            }
        }

        if (KEANU_PATTERN.matcher(msgContent).find()) {
            keanuCommand.execute(new CommandEvent(event, null, keanuCommandClient));
            return;
        }
    }

}
