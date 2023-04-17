package com.badfic.philbot.service;

import com.badfic.philbot.data.SwampyGamesConfig;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HumpDay extends NonCommandSwampy {

    @Scheduled(cron = "${swampy.schedule.phil.humpday}", zone = "${swampy.schedule.timezone}")
    public void philHumpDay() {
        TextChannel general = philJda.getTextChannelsByName("general", false).get(0);

        general.sendMessage("https://cdn.discordapp.com/attachments/323666308107599872/834310518478471218/mullethumpygrinch.png").queue();
    }

    @Scheduled(cron = "${swampy.schedule.behrad.humpday}", zone = "${swampy.schedule.timezone}")
    public void behradHumpDay() {
        TextChannel general = philJda.getTextChannelsByName("general", false).get(0);
        SwampyGamesConfig swampyGamesConfig = swampyGamesConfigDao.get();

        discordWebhookSendService.sendMessage(general.getIdLong(), swampyGamesConfig.getBehradNickname(), swampyGamesConfig.getBehradAvatar(),
                "https://tenor.com/view/itis-wednesdaymy-dudes-wednesday-viralyoutube-gif-18012295");
    }

}
