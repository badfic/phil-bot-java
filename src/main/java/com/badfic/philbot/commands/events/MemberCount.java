package com.badfic.philbot.commands.events;

import com.badfic.philbot.commands.BaseNormalCommand;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.service.DailyTickable;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class MemberCount extends BaseNormalCommand implements DailyTickable {

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
    protected void execute(CommandEvent event) {
        String args = event.getArgs();

        if (StringUtils.isBlank(args)) {
            event.reply(event.getGuild().getMembers().size() + " members");
        } else if (StringUtils.containsIgnoreCase(args, "set")) {
            args = args.replace("set", "").trim();

            long voiceChannelId;
            try {
                voiceChannelId = Long.parseLong(args);
            } catch (NumberFormatException e) {
                event.replyError("Could not parse voice channel id as a number");
                return;
            }

            VoiceChannel voiceChannelById = event.getGuild().getVoiceChannelById(voiceChannelId);

            if (voiceChannelById == null) {
                event.replyError("Could not find voice channel with id: " + voiceChannelId);
                return;
            }

            SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
            swampyGamesConfig.setMemberCountVoiceChannelId(voiceChannelId);
            saveSwampyGamesConfig(swampyGamesConfig);

            voiceChannelById.getManager().setName(event.getGuild().getMembers().size() + " members").queue();
            event.replySuccess("Successfully set channel and updated member count");
        } else if (StringUtils.containsIgnoreCase(args, "update")) {
            boolean success = updateCount();

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
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        Guild guild = philJda.getGuildById(baseConfig.guildId);
        VoiceChannel voiceChannelById = philJda.getVoiceChannelById(swampyGamesConfig.getMemberCountVoiceChannelId());
        if (voiceChannelById != null) {
            voiceChannelById.getManager().setName(guild.getMembers().size() + " members").queue();
            Constants.debugToTestChannel(philJda, "Successfully updated member count channel");
            return true;
        }

        return false;
    }

}
