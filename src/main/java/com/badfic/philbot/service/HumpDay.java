package com.badfic.philbot.service;

import com.badfic.philbot.data.SwampyGamesConfig;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HumpDay extends BaseService {

    @Scheduled(cron = "${swampy.schedule.phil.humpday}", zone = "${swampy.schedule.timezone}")
    public void philHumpDay() {
        philJda.getTextChannelsByName("general", false).stream().findAny().ifPresent(channel -> {
            channel.sendMessage("https://cdn.discordapp.com/attachments/323666308107599872/834310518478471218/mullethumpygrinch.png").queue();
        });
    }

    @Scheduled(cron = "${swampy.schedule.behrad.humpday}", zone = "${swampy.schedule.timezone}")
    public void behradHumpDay() {
        philJda.getTextChannelsByName("general", false).stream().findAny().ifPresent(channel -> {
            SwampyGamesConfig swampyGamesConfig = swampyGamesConfigDao.get();

            discordWebhookSendService.sendMessage(channel.getIdLong(), swampyGamesConfig.getBehradNickname(), swampyGamesConfig.getBehradAvatar(),
                    "https://tenor.com/view/itis-wednesdaymy-dudes-wednesday-viralyoutube-gif-18012295");
        });
    }

}
