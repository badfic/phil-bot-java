package com.badfic.philbot.commands;

import com.badfic.philbot.config.BaseConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class Tumblr extends BaseNormalCommand {
    private final RestTemplate restTemplate;
    private final String apiKey;

    public Tumblr(RestTemplate restTemplate, BaseConfig baseConfig) {
        name = "tumblr";
        help = "!!tumblr username\nShow this tumblr users latest post";
        this.restTemplate = restTemplate;
        this.apiKey = baseConfig.tumblrConsumerKey;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] split = event.getArgs().split("\\s+");

        if (ArrayUtils.getLength(split) != 1) {
            event.replyError("Please specify a username. Example `!!tumblr usernamehere`");
            return;
        }

        String tumblrUser = split[0];
        try {
            ResponseEntity<Info> responseEntity = restTemplate.getForEntity(
                    String.format("https://api.tumblr.com/v2/blog/%s/posts?api_key=%s", tumblrUser, apiKey), Info.class);

            Info info = responseEntity.getBody();

            if (info != null) {
                Response response = info.response();
                List<Post> posts = response.posts();

                if (CollectionUtils.isNotEmpty(posts)) {
                    posts.sort((a, b) -> Long.compare(b.timestamp().getEpochSecond(), a.timestamp().getEpochSecond()));

                    Post post = posts.get(0);

                    event.reply("\uD83D\uDD16 **" + response.blog().title() + "** \n\uD83D\uDD8A️ **Last Post**: " + post.postUrl());
                }
            }
        } catch (Exception e) {
            log.error("Failed to get [user={}] tumblr posts", tumblrUser, e);
            event.replyError("Something went wrong trying to get user's posts: " + tumblrUser);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Info(Response response) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Response(Blog blog, List<Post> posts) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Blog(String title) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Post(Instant timestamp, @JsonProperty("post_url") String postUrl) {}

}
