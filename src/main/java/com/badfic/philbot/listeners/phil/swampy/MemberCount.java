package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class MemberCount extends BaseSwampy implements PhilMarker {

    public MemberCount() {
        name = "memberCount";
        requiredRole = Constants.ADMIN_ROLE;
        help = "`!!memberCount` display member count\n" +
                "`!!memberCount set 12345` sets the member count voice channel to the voice channel with the id 12345\n" +
                "`!!memberCount update` manually updates the member count in the member count voice channel\n";
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
            swampyGamesConfig.setMemberCountChannel(voiceChannelId);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            voiceChannelById.getManager().setName(event.getGuild().getMembers().size() + " members").queue();
            event.replySuccess("Successfully set channel and updated member count");
        } else if (StringUtils.containsIgnoreCase(args, "update")) {
            boolean success = updateCount();

            if (success) {
                event.replySuccess("Successfully updated member count");
            } else {
                event.replyError("Failed to update count. Try setting voice channel with `!!memberCount set 12345`");
            }
        } else {
            event.replyError("Unrecognized memberCount command");
        }
    }

    public boolean updateCount() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        Guild guild = philJda.getGuilds().get(0);
        VoiceChannel voiceChannelById = guild.getVoiceChannelById(swampyGamesConfig.getMemberCountChannel());
        if (voiceChannelById != null) {
            voiceChannelById.getManager().setName(guild.getMembers().size() + " members").queue();
            return true;
        }

        return false;
    }
}
