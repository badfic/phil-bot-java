package com.badfic.philbot.service;

import com.badfic.philbot.config.Constants;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
public class PhilsJokes extends BaseService {

    @Scheduled(cron = "${swampy.schedule.phil.dadjoke}", zone = "${swampy.schedule.timezone}")
    public void sayDadJoke() {
        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE);
        headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);
        ResponseEntity<String> dadJoke = restTemplate.exchange("https://icanhazdadjoke.com/", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        if (dadJoke.getBody() != null) {
            philJda.getTextChannelsByName("general", false).stream().findAny().ifPresent(channel -> {
                channel.sendMessage(dadJoke.getBody()).queue();
            });
        }
    }

}
