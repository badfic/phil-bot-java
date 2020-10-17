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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class KeanuMessageListener extends ListenerAdapter implements KeanuMarker {

    private static final Pattern KEANU_PATTERN = compile("keanu|reeves|neo|john wick|puppy|puppies|pupper|doggo|doge");
    private static final Multimap<String, Pair<Pattern, String>> USER_TRIGGER_WORDS = ImmutableMultimap.<String, Pair<Pattern, String>>builder()
            .put("513187180198363136", ImmutablePair.of(compile("husband"), "Hi honey"))
            .put("513187180198363136", ImmutablePair.of(compile("love"), "I love you too"))
            .put("601043580945170443", ImmutablePair.of(compile("dad"), "Hi pumpkin"))
            .put("323520695550083074", ImmutablePair.of(compile("son"), "father"))
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

        Collection<Pair<Pattern, String>> userTriggers = USER_TRIGGER_WORDS.get(event.getAuthor().getId());
        if (CollectionUtils.isNotEmpty(userTriggers)) {
            Optional<String> match = userTriggers.stream().filter(t -> t.getLeft().matcher(msgContent).find()).map(Pair::getRight).findAny();

            if (match.isPresent()) {
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", " + match.get()).queue();
                return;
            }
        }

        if (KEANU_PATTERN.matcher(msgContent).find()) {
            keanuCommand.execute(new CommandEvent(event, null, keanuCommandClient));
            return;
        }
    }

    private static Pattern compile(String s) {
        return Pattern.compile("\\b(" + s + ")\\b", Pattern.CASE_INSENSITIVE);
    }

}
