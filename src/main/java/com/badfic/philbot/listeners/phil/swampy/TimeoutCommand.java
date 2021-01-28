package com.badfic.philbot.listeners.phil.swampy;

import static net.dv8tion.jda.api.Permission.MESSAGE_ATTACH_FILES;
import static net.dv8tion.jda.api.Permission.MESSAGE_EMBED_LINKS;
import static net.dv8tion.jda.api.Permission.MESSAGE_WRITE;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.TimeoutCase;
import com.badfic.philbot.data.phil.TimeoutCaseRepository;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import javax.annotation.Resource;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
public class TimeoutCommand extends BaseSwampy implements PhilMarker {

    private static final int TIMEOUT_MINUTES = 7;

    @Resource
    private TimeoutCaseRepository timeoutCaseRepository;

    public TimeoutCommand() {
        name = "timeout";
        requiredRole = Constants.ADMIN_ROLE;
        help = "put a user in timeout (temporary mute/ban/jail).\n" +
                "`!!timeout @user` puts a user in timeout for 5 minutes.\n" +
                "`!!timeout 30 @user` puts a user in timeout for 30 minutes.";
    }

    @Override
    protected void execute(CommandEvent event) {
        TextChannel timeoutChannel = philJda.getTextChannelById(baseConfig.timeoutChannelId);

        if (CollectionUtils.size(event.getMessage().getMentionedMembers()) != 1) {
            event.replyError("Please mention one user to put in timeout");
            return;
        }
        Member member = event.getMessage().getMentionedMembers().get(0);

        if (member.getUser().isBot()) {
            event.replyError("You can't put a bot in timeout");
            return;
        }

        if (hasRole(member, Constants.ADMIN_ROLE) || hasRole(member, Constants.MOD_ROLE)) {
            event.replyError("You can't put a mod in timeout");
            return;
        }

        if (timeoutCaseRepository.existsById(member.getIdLong())) {
            event.replyError("That user is already in timeout");
            return;
        }

        try {
            timeoutCaseRepository.save(new TimeoutCase(member.getIdLong(), LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(TIMEOUT_MINUTES)));

            event.getGuild()
                    .getChannels()
                    .stream()
                    .filter(c -> c.getType() == ChannelType.TEXT || c.getType() == ChannelType.VOICE)
                    .forEach(channel -> assignOverridePermissions(channel, member));

            if (timeoutChannel != null) {
                timeoutChannel.sendMessage(member.getEffectiveName() + " has been put in timeout for " + TIMEOUT_MINUTES + " minutes").queue();
            }
        } catch (Exception e) {
            honeybadgerReporter.reportError(e, null, "Failed to put user " + member.getAsMention() + " in timeout");
        }
    }

    private void assignOverridePermissions(GuildChannel channel, Member member) {
        if (baseConfig.timeoutChannelId.equals(channel.getId())) {
            channel.upsertPermissionOverride(member).setAllow(MESSAGE_WRITE, MESSAGE_EMBED_LINKS, MESSAGE_ATTACH_FILES).queue();
        } else {
            channel.upsertPermissionOverride(member).deny(Permission.ALL_PERMISSIONS).queue();
        }
    }

}
