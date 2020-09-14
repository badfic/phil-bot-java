package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.Phrase;
import com.badfic.philbot.repository.PhraseRepository;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.List;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class PhilMessageListener extends ListenerAdapter implements PhilMarker {

    private static final Pattern PHIL_PATTERN = Pattern.compile("\\b(phil|klemmer|phellen|cw|willip|schlemmer|pharole|klaskin|phreddie|klercury|philliam)\\b", Pattern.CASE_INSENSITIVE);

    private final PhilCommand philCommand;
    private final BastardCommand bastardCommand;
    private final CommandClient philCommandClient;
    private final PhraseRepository phraseRepository;

    @Autowired
    public PhilMessageListener(PhilCommand philCommand,
                               BastardCommand bastardCommand,
                               @Qualifier("philCommandClient") CommandClient philCommandClient,
                               PhraseRepository phraseRepository) {
        this.philCommand = philCommand;
        this.bastardCommand = bastardCommand;
        this.philCommandClient = philCommandClient;
        this.phraseRepository = phraseRepository;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith("!!") || event.getAuthor().isBot()) {
            return;
        }

        if (event.getMember().getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase("18+"))) {
            bastardCommand.execute(new CommandEvent(event, null, philCommandClient));
        }

        if (PHIL_PATTERN.matcher(msgContent).find()) {
            philCommand.execute(new CommandEvent(event, null, philCommandClient));
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

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        bastardCommand.voiceJoined(event.getMember());
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        bastardCommand.voiceLeft(event.getMember());
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        long points = 1;
        if (event.getReactionEmote().isEmote()) {
            points += BastardCommand.NORMAL_MSG_POINTS;
        }
        bastardCommand.givePointsToMember(points, event.getMember());
    }

}
