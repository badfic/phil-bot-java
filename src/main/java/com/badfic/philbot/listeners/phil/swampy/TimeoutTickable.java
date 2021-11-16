package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.data.phil.TimeoutCase;
import com.badfic.philbot.data.phil.TimeoutCaseRepository;
import com.badfic.philbot.service.BaseService;
import com.badfic.philbot.service.MinuteTickable;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.stereotype.Component;

@Component
public class TimeoutTickable extends BaseService implements MinuteTickable {

    @Resource
    private TimeoutCaseRepository timeoutCaseRepository;

    @Override
    public void runMinutelyTask() {
        TextChannel timeoutChannel = philJda.getTextChannelById(baseConfig.timeoutChannelId);
        Guild guild = philJda.getGuilds().get(0);

        for (TimeoutCase timeoutCase : timeoutCaseRepository.findAll()) {
            if (timeoutCase.getReleaseDate().isBefore(LocalDateTime.now())) {
                try {
                    guild.getChannels()
                            .stream()
                            .filter(c -> c.getType() == ChannelType.TEXT || c.getType() == ChannelType.VOICE)
                            .flatMap(c -> c.getMemberPermissionOverrides().stream())
                            .filter(mpo -> mpo.getMember() != null && mpo.getMember().getIdLong() == timeoutCase.getUserId())
                            .forEach(mpo -> mpo.delete().queue());

                    timeoutCaseRepository.deleteById(timeoutCase.getUserId());
                } catch (Exception e) {
                    honeybadgerReporter.reportError(e, null, "Failed to release user " + timeoutCase.getUserId() + " from timeout");
                    if (timeoutChannel != null) {
                        timeoutChannel.sendMessage("Failed to release <@!" + timeoutCase.getUserId() + "> from timeout").queue();
                    }
                    continue;
                }

                if (timeoutChannel != null) {
                    timeoutChannel.sendMessage("<@!" + timeoutCase.getUserId() + "> has been released from timeout").queue();
                }
            }
        }
    }

}
