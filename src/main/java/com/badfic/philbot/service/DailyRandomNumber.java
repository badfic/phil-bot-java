package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyRandomNumber extends BaseService {

    @Scheduled(cron = "${swampy.schedule.phil.dailyrandomnumber}", zone = "${swampy.schedule.timezone}")
    public void dailyRandomNumber() {
        TextChannel general = philJda.getTextChannelsByName("general", false).get(0);

        Character[] letters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L'};
        int number = randomNumberService.nextInt(1, 15);

        general.sendMessage(String.format("<@307611036134146080>, %s%d", Constants.pickRandom(letters), number)).queue();
    }

}
