package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
public class PhilsJokes extends NonCommandSwampy {

    @Scheduled(cron = "0 0,30 * * * *")
    public void sayDadJoke() {
        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE);
        headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);
        ResponseEntity<String> dadJoke = restTemplate.exchange("https://icanhazdadjoke.com/", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        if (dadJoke.getBody() != null) {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).stream().findAny().ifPresent(swampysChannel -> {
                swampysChannel.sendMessage(dadJoke.getBody()).queue();
            });
        }
    }

    @Scheduled(cron = "0 15,45 * * * *")
    public void sayMiscJoke() {
        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);
        ResponseEntity<JokeApiResult> joke = restTemplate.exchange(
                "https://v2.jokeapi.dev/joke/Any?blacklistFlags=nsfw,religious,political,racist,sexist,explicit&type=twopart",
                HttpMethod.GET, new HttpEntity<>(headers), JokeApiResult.class);

        JokeApiResult jokeBody = joke.getBody();
        if (jokeBody != null && jokeBody.getSetup() != null && jokeBody.getDelivery() != null) {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).stream().findAny().ifPresent(swampysChannel -> {
                swampysChannel.sendMessage(jokeBody.getSetup()).queue();
                swampysChannel.sendMessage(jokeBody.getDelivery()).queueAfter(10, TimeUnit.SECONDS);
            });
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JokeApiResult {
        private String setup;
        private String delivery;

        public String getSetup() {
            return setup;
        }

        public void setSetup(String setup) {
            this.setup = setup;
        }

        public String getDelivery() {
            return delivery;
        }

        public void setDelivery(String delivery) {
            this.delivery = delivery;
        }
    }

}
