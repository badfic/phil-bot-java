package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.config.PhilMarker;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PhilMessageListener extends ListenerAdapter implements PhilMarker {

    private static final Pattern PHIL_PATTERN = Pattern.compile("\\b(phil|klemmer|phellen|cw|willip|schlemmer|pharole|klaskin|phreddie|klercury|philliam)\\b", Pattern.CASE_INSENSITIVE);

    private final PhilCommand philCommand;
    private final SwampyCommand swampyCommand;
    private final CommandClient philCommandClient;

    @Autowired
    public PhilMessageListener(PhilCommand philCommand,
                               SwampyCommand swampyCommand,
                               @Qualifier("philCommandClient") CommandClient philCommandClient) {
        this.philCommand = philCommand;
        this.swampyCommand = swampyCommand;
        this.philCommandClient = philCommandClient;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw();

        if (msgContent.startsWith("!!") || event.getAuthor().isBot()) {
            return;
        }

        if (event.getAuthor().getId().equalsIgnoreCase("486427102854381568") && StringUtils.containsIgnoreCase(msgContent, "I'm out")) {
            swampyCommand.takePointsFromMember(1000, event.getMember());
        }

        if (StringUtils.containsIgnoreCase(msgContent, "my wife")) {
            swampyCommand.takePointsFromMember(100, event.getMember());
        }

        swampyCommand.execute(new CommandEvent(event, "", philCommandClient));

        if (PHIL_PATTERN.matcher(msgContent).find()) {
            philCommand.execute(new CommandEvent(event, null, philCommandClient));
            return;
        }
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        swampyCommand.voiceJoined(event.getMember());
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        swampyCommand.voiceLeft(event.getMember());
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        long points = SwampyCommand.NORMAL_REACTION_POINTS;
        if (event.getReactionEmote().isEmote()) {
            points = SwampyCommand.EMOTE_REACTION_POINTS;
        }
        swampyCommand.givePointsToMember(points, event.getMember());
    }

}
