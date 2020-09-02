package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.Phrase;
import com.badfic.philbot.repository.PhraseRepository;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.List;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class PhilMessageListener extends ListenerAdapter implements PhilMarker {

    private static final Pattern PHIL_PATTERN = Pattern.compile("\\b(phil|klemmer|phellen|cw|willip|schlemmer)\\b", Pattern.CASE_INSENSITIVE);

    private final boolean isTestEnvironment;
    private final PhilCommand philCommand;
    private final CommandClient philCommandClient;
    private final PhraseRepository phraseRepository;

    @Autowired
    public PhilMessageListener(PhilCommand philCommand,
                               @Qualifier("philCommandClient") CommandClient philCommandClient,
                               PhraseRepository phraseRepository,
                               BaseConfig baseConfig) {
        isTestEnvironment = "test".equalsIgnoreCase(baseConfig.nodeEnvironment);
        this.philCommand = philCommand;
        this.philCommandClient = philCommandClient;
        this.phraseRepository = phraseRepository;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith("!!") || event.getAuthor().isBot()) {
            return;
        }

        if (PHIL_PATTERN.matcher(msgContent).find()) {
            philCommand.execute(new CommandEvent(event, null, philCommandClient));
            return;
        }

        if (isTestEnvironment && !"test-channel".equalsIgnoreCase(event.getChannel().getName())) {
            return;
        }

        List<Phrase> phrases = phraseRepository.findAllByDiscordUser_id(event.getAuthor().getId());

        if (!CollectionUtils.isEmpty(phrases)) {
            for (Phrase phrase : phrases) {
                Pattern phrasePattern = Pattern.compile("\\b(" + Pattern.quote(phrase.getPhrase()) + ")\\b", Pattern.CASE_INSENSITIVE);

                if (phrasePattern.matcher(msgContent).find()) {
                    phrase.setCounter(phrase.getCounter() + 1);
                    phraseRepository.save(phrase);
                }
            }
        }
    }

}
