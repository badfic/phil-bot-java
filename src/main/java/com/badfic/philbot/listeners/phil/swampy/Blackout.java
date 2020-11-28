package com.badfic.philbot.listeners.phil.swampy;

import static net.dv8tion.jda.api.Permission.MESSAGE_ADD_REACTION;
import static net.dv8tion.jda.api.Permission.MESSAGE_ATTACH_FILES;
import static net.dv8tion.jda.api.Permission.MESSAGE_EMBED_LINKS;
import static net.dv8tion.jda.api.Permission.MESSAGE_HISTORY;
import static net.dv8tion.jda.api.Permission.MESSAGE_READ;
import static net.dv8tion.jda.api.Permission.MESSAGE_WRITE;
import static net.dv8tion.jda.api.Permission.VIEW_CHANNEL;

import com.badfic.philbot.config.PhilMarker;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
public class Blackout extends BaseSwampy implements PhilMarker {

    public Blackout() {
        name = "blackout";
        help = "Add/Remove member specific overrides to channel (can be mentioned channels or defaults to ALL channels)\n" +
                "So that when it is active, these members (all members) cannot read/write/see these channels at all.\n" +
                "Example: `!!blackout #general` Add member specific overrides for mentioned channel and ALL members\n" +
                "Example: `!!blackout` Adds member specific overrides for ALL channels and ALL members\n" +
                "Example: `!!blackout reset` Removes all member-specific overrides.";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getAuthor().getIdLong() != 307611036134146080L && !event.getAuthor().getId().equalsIgnoreCase(baseConfig.ownerId)) {
            return;
        }

        String args = event.getArgs();

        if (args.startsWith("reset")) {
            for (GuildChannel channel : event.getGuild().getChannels()) {
                if (channel.getType() == ChannelType.TEXT || channel.getType() == ChannelType.VOICE) {
                    for (PermissionOverride memberPermissionOverride : channel.getMemberPermissionOverrides()) {
                        memberPermissionOverride.delete().queue();
                    }
                    event.reply("Removed member overrides for channel: " + channel.getName());
                }
            }
        } else {
            if (CollectionUtils.isNotEmpty(event.getMessage().getMentionedChannels())) {

                for (GuildChannel channel : event.getMessage().getMentionedChannels()) {
                    if (channel.getType() == ChannelType.TEXT || channel.getType() == ChannelType.VOICE) {
                        fireDrillPermissions(event, channel);
                        event.reply("Added member overrides for channel: " + channel.getName());
                    }
                }
            } else {
                for (GuildChannel channel : event.getGuild().getChannels()) {
                    if (channel.getType() == ChannelType.TEXT || channel.getType() == ChannelType.VOICE) {
                        fireDrillPermissions(event, channel);
                        event.reply("Added member overrides for channel: " + channel.getName());
                    }
                }
            }
        }

        event.replySuccess("Blackout complete");
    }

    private void fireDrillPermissions(CommandEvent event, GuildChannel channel) {
        if (channel.getId().equals(baseConfig.fireDrillChannelId)) {
            for (Member member : event.getGuild().getMembers()) {
                if (!member.getUser().isBot()) {
                    channel.upsertPermissionOverride(member).setAllow(
                            VIEW_CHANNEL, MESSAGE_ADD_REACTION, MESSAGE_READ, MESSAGE_WRITE, MESSAGE_EMBED_LINKS,
                            MESSAGE_ATTACH_FILES, MESSAGE_HISTORY).queue();
                }
            }
        } else {
            for (Member member : event.getGuild().getMembers()) {
                if (!member.getUser().isBot()) {
                    channel.upsertPermissionOverride(member).deny(Permission.ALL_PERMISSIONS).queue();
                }
            }
        }
    }

}
