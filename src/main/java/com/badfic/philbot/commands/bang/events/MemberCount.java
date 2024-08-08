package com.badfic.philbot.commands.bang.events;

import com.badfic.philbot.CommandEvent;
import com.badfic.philbot.commands.bang.BaseBangCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.service.DailyTickable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class MemberCount extends BaseBangCommand implements DailyTickable {

    public MemberCount() {
        name = "memberCount";
        requiredRole = Constants.ADMIN_ROLE;
        help = """
                `!!memberCount` display member count
                `!!memberCount set 12345` sets the member count voice channel to the voice channel with the id 12345
                `!!memberCount update` manually updates the member count in the member count voice channel
                """;
    }

    @Override
    public void execute(final CommandEvent event) {
        var args = event.getArgs();

        if (StringUtils.isBlank(args)) {
            event.reply(event.getGuild().getMembers().size() + " members");
        } else if (StringUtils.containsIgnoreCase(args, "set")) {
            args = args.replace("set", "").trim();

            final long voiceChannelId;
            try {
                voiceChannelId = Long.parseLong(args);
            } catch (final NumberFormatException e) {
                event.replyError("Could not parse voice channel id as a number");
                return;
            }

            final var voiceChannelById = event.getGuild().getVoiceChannelById(voiceChannelId);

            if (voiceChannelById == null) {
                event.replyError("Could not find voice channel with id: " + voiceChannelId);
                return;
            }

            final var swampyGamesConfig = getSwampyGamesConfig();
            swampyGamesConfig.setMemberCountVoiceChannelId(voiceChannelId);
            saveSwampyGamesConfig(swampyGamesConfig);

            voiceChannelById.getManager().setName(event.getGuild().getMembers().size() + " members").queue();
            event.replySuccess("Successfully set channel and updated member count");
        } else if (StringUtils.containsIgnoreCase(args, "update")) {
            final var success = updateCount();

            if (!success) {
                event.replyError("Failed to update count. Try setting voice channel with `!!memberCount set 12345`");
            }
        } else {
            event.replyError("Unrecognized memberCount command");
        }
    }

    @Override
    public void runDailyTask() {
        updateCount();
    }

    public boolean updateCount() {
        final var swampyGamesConfig = getSwampyGamesConfig();
        final var guild = philJda.getGuildById(baseConfig.guildId);
        final var voiceChannelById = philJda.getVoiceChannelById(swampyGamesConfig.getMemberCountVoiceChannelId());
        if (voiceChannelById != null) {
            voiceChannelById.getManager().setName(guild.getMembers().size() + " members").queue();
            Constants.debugToTestChannel(philJda, "Successfully updated member count channel");
            return true;
        }

        return false;
    }

}
