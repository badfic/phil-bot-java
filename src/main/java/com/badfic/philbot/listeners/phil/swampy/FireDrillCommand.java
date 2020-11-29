package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.FireDrillPermission;
import com.badfic.philbot.data.phil.FireDrillPermissionRepository;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Resource;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FireDrillCommand extends BaseSwampy implements PhilMarker {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Resource
    private FireDrillPermissionRepository fireDrillPermissionRepository;

    public FireDrillCommand() {
        name = "fireDrill";
        help = "Save and drop all permissions from all channels.\n" +
                "So that when it is active, all members cannot read/write/see all the channels at all (except the fire drill channel).\n" +
                "Example: `!!fireDrill` Saves and drops permissions from all channels.\n" +
                "Example: `!!fireDrill reset` Restores permissions to all channels.";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getAuthor().getIdLong() != 307611036134146080L && !event.getAuthor().getId().equalsIgnoreCase(baseConfig.ownerId)) {
            return;
        }

        String args = event.getArgs();

        if (args.startsWith("reset")) {
            if (fireDrillPermissionRepository.count() == 0) {
                event.replyError("There is nothing to reset, the database doesn't have any permissions for channels");
                return;
            }

            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (FireDrillPermission fireDrillPermission : fireDrillPermissionRepository.findAll()) {
                GuildChannel guildChannel = event.getGuild().getGuildChannelById(fireDrillPermission.getChannelId());
                Role role = event.getGuild().getRoleById(fireDrillPermission.getRoleId());

                CompletableFuture<PermissionOverride> future = guildChannel.upsertPermissionOverride(role)
                        .setPermissions(fireDrillPermission.getAllowPermission(), fireDrillPermission.getDenyPermission())
                        .submit();

                future.whenComplete((o, err) -> {
                    if (err != null) {
                        logger.error("Failed to set permissions for [channel={}], [role={}]", guildChannel.getName(), role.getName(), err);
                        event.replyError("Failed to set permissions for channel: " + guildChannel.getName() + ", role: " + role.getName());
                    } else {
                        fireDrillPermissionRepository.deleteById(fireDrillPermission.getId());
                    }
                });

                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> event.replySuccess("Finished reset of fireDrill"));
        } else {
            if (fireDrillPermissionRepository.count() > 0) {
                event.replyError("A fire drill was already triggered. You must do a !!fireDrill reset first.");
                return;
            }

            for (GuildChannel channel : event.getGuild().getChannels()) {
                long channelId = channel.getIdLong();
                for (PermissionOverride rolePermissionOverride : channel.getRolePermissionOverrides()) {
                    try {
                        long roleId = rolePermissionOverride.getRole().getIdLong();
                        long allowedRaw = rolePermissionOverride.getAllowedRaw();
                        long deniedRaw = rolePermissionOverride.getDeniedRaw();
                        long inheritRaw = rolePermissionOverride.getInheritRaw();

                        FireDrillPermission fireDrillPermission = new FireDrillPermission(channelId, roleId, allowedRaw, deniedRaw, inheritRaw);
                        fireDrillPermissionRepository.save(fireDrillPermission);
                    } catch (Exception e) {
                        logger.error("Failed to save permissions for [channel={}], [role={}]", channel.getName(), rolePermissionOverride.getRole(), e);
                        event.replyError("Failed to save permissions for channel: " + channel.getName()
                                + " role: " + rolePermissionOverride.getRole() + ". ABORTING before modifying permissions, for safety reasons.");
                        return;
                    }
                }
            }

            for (GuildChannel channel : event.getGuild().getChannels()) {
                for (PermissionOverride permissionOverride : channel.getPermissionOverrides()) {
                    try {
                        permissionOverride.delete().complete();
                    } catch (Exception e) {
                        if (permissionOverride.isMemberOverride()) {
                            logger.error("Failed to delete permissions from [channel={}], [member={}]", channel.getName(),
                                    permissionOverride.getMember().getEffectiveName(), e);
                            event.replyError("Failed to delete permissions from channel: " + channel.getName()
                                    + ", member: " + permissionOverride.getMember().getEffectiveName());
                        } else if (permissionOverride.isRoleOverride()) {
                            logger.error("Failed to delete permissions from [channel={}], [role={}]", channel.getName(),
                                    permissionOverride.getRole().getName(), e);
                            event.replyError("Failed to delete permissions from channel: " + channel.getName()
                                    + ", role: " + permissionOverride.getRole().getName());
                        } else {
                            logger.error("Failed to delete permissions from [channel={}]", channel.getName(), e);
                            event.replyError("Failed to delete permissions from channel: " + channel.getName());
                        }
                    }
                }

                Role everyoneRole = event.getGuild().getPublicRole();
                try {
                    channel.upsertPermissionOverride(everyoneRole).setDeny(Permission.ALL_PERMISSIONS).complete();
                } catch (Exception e) {
                    logger.error("Failed to deny @everyone from [channel={}]", channel.getName(), e);
                    event.replyError("Failed to deny @everyone permissions to channel: " + channel.getName());
                }
            }

            TextChannel fireDrillChannel = event.getGuild().getTextChannelById(baseConfig.fireDrillChannelId);

            if (fireDrillChannel != null) {
                Role everyoneRole = event.getGuild().getPublicRole();

                try {
                    fireDrillChannel.upsertPermissionOverride(everyoneRole)
                            .setAllow(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_WRITE,
                                    Permission.MESSAGE_HISTORY, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES)
                            .complete();
                } catch (Exception e) {
                    event.replyError("Failed to set @everyone permissions for fire drill channel. Please proceed to do it manually.");
                }
            } else {
                event.replyError("Could not find fire drill channel by id. So failed to set channel permissions for fire drill channel. " +
                        "Please proceed to do it manually.");
            }

            event.replySuccess("Finished fireDrill permissions saving and overriding @everyone to deny.");
        }
    }

}
