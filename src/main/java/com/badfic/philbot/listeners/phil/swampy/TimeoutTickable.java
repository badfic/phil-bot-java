package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.TimeoutCase;
import com.badfic.philbot.data.phil.TimeoutCaseRepository;
import com.badfic.philbot.service.MinuteTickable;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.stereotype.Component;

@Component
public class TimeoutTickable extends NonCommandSwampy implements MinuteTickable {

    @Resource
    private TimeoutCaseRepository timeoutCaseRepository;

    @Override
    public void tick() throws Exception {
        TextChannel megaHellChannel = philJda.getTextChannelsByName(Constants.MEGA_HELL_CHANNEL, false).get(0);

        for (TimeoutCase timeoutCase : timeoutCaseRepository.findAll()) {
            if (timeoutCase.getReleaseDate().isBefore(LocalDateTime.now())) {
                try {
                    philJda.getGuilds()
                            .get(0)
                            .getChannels()
                            .stream()
                            .filter(c -> c.getType() == ChannelType.TEXT || c.getType() == ChannelType.VOICE)
                            .flatMap(c -> c.getMemberPermissionOverrides().stream())
                            .filter(mpo -> mpo.getMember() != null && mpo.getMember().getIdLong() == timeoutCase.getUserId())
                            .forEach(mpo -> mpo.delete().queue());

                    timeoutCaseRepository.deleteById(timeoutCase.getUserId());
                } catch (Exception e) {
                    honeybadgerReporter.reportError(e, null, "Failed to release user " + timeoutCase.getUserId() + " from timeout");
                    megaHellChannel.sendMessage("Failed to release <@!" + timeoutCase.getUserId() + "> from timeout").queue();
                    continue;
                }
                megaHellChannel.sendMessage("<@!" + timeoutCase.getUserId() + "> has been released from timeout").queue();
            }
        }
    }

}
