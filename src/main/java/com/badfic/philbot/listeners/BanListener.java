package com.badfic.philbot.listeners;

import java.util.Optional;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class BanListener extends ListenerAdapter {

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {
        begone(event.getUser(), event);
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        begone(event.getUser(), event);
    }

    private void begone(User user, GenericGuildEvent event) {
        Optional<TextChannel> announcementsChannel = event.getGuild().getTextChannelsByName("announcements", false).stream().findFirst();
        announcementsChannel.ifPresent(channel -> channel.sendMessage("Begone Bot " + user.getAsMention()).queue());
    }

}
