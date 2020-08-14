package com.badfic.philbot.listeners;

import com.badfic.philbot.data.Phrase;
import com.badfic.philbot.repository.PhraseRepository;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.List;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class MessageListener extends ListenerAdapter {

    private static final Pattern PHIL_PATTERN = Pattern.compile("\\b(phil|klemmer)\\b", Pattern.CASE_INSENSITIVE);

    private final PhilCommand philCommand;
    private final CommandClient commandClient;
    private final PhraseRepository phraseRepository;

    @Autowired
    public MessageListener(PhilCommand philCommand, CommandClient commandClient, PhraseRepository phraseRepository) {
        this.philCommand = philCommand;
        this.commandClient = commandClient;
        this.phraseRepository = phraseRepository;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith("!!") || event.getAuthor().isBot()) {
            return;
        }

        if (PHIL_PATTERN.matcher(msgContent).find()) {
            philCommand.execute(new CommandEvent(event, null, commandClient));
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
