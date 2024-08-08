package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class DailyRandomNumber extends BaseService {

    @Scheduled(cron = "${swampy.schedule.phil.dailyrandomnumber}", zone = "${swampy.schedule.timezone}")
    void dailyRandomNumber() {
        final var general = philJda.getTextChannelsByName("general", false).getFirst();

        final var letters = new Character[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L'};
        final var number = randomNumberService.nextInt(1, 15);

        general.sendMessage(String.format("<@307611036134146080>, %s%d", Constants.pickRandom(letters), number)).queue();
    }

}
