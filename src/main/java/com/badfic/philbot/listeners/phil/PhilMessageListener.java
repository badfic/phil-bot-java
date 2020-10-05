package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Optional;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        if (event.getRoles().stream().anyMatch(r -> r.getName().equals(Constants.CHAOS_CHILDREN_ROLE) || r.getName().equals(Constants.EIGHTEEN_PLUS_ROLE))) {
            String mention = event.getMember().getAsMention();
            event.getGuild().getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0).sendMessage(mention + " Congratulations on not being a newbie " +
                    "anymore my chaotic friend! Now you're entered into The Swampys, our server wide chaos games. " +
                    "You don't have to actively participate, but if you'd like to join the chaos with us, take a peek at what The Swampys has to offer.\n\n" +
                    "https://discordapp.com/channels/740999022340341791/761398315119280158/761411776561807410").queue();
        }
    }

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {
        begone(event.getUser(), event);
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        begone(event.getUser(), event);
    }

    private void begone(User user, GenericGuildEvent event) {
        swampyCommand.removeFromGames(user.getId());
        Optional<TextChannel> announcementsChannel = event.getGuild().getTextChannelsByName("announcements", false).stream().findFirst();
        announcementsChannel.ifPresent(channel -> channel.sendMessage("Begone Bot " + user.getAsMention()).queue());
    }

}
