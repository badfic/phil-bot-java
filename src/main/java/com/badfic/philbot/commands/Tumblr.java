package com.badfic.philbot.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Post;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Tumblr extends BaseNormalCommand {
    private final JumblrClient jumblrClient;

    public Tumblr(JumblrClient jumblrClient) {
        name = "tumblr";
        help = "!!tumblr username\nShow this tumblr users latest post";
        this.jumblrClient = jumblrClient;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] split = event.getArgs().split("\\s+");

        if (ArrayUtils.getLength(split) != 1) {
            event.replyError("Please specify a username. Example `!!tumblr usernamehere`");
            return;
        }

        try {
            Blog blog = jumblrClient.blogInfo(split[0] + ".tumblr.com");

            if (blog != null) {
                List<Post> posts = blog.posts();
                posts.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                if (CollectionUtils.isNotEmpty(posts)) {
                    Post post = posts.get(0);

                    event.reply("\uD83D\uDD16 **" + blog.getTitle() + "** \n\uD83D\uDD8A️ **Last Post**: " + post.getPostUrl());
                }
            }
        } catch (Exception e) {
            log.error("Failed to get [user={}] tumblr posts", split[0], e);
            honeybadgerReporter.reportError(e, "Failed to get user's tumblr posts: " + split[0]);
            event.replyError("Something went wrong trying to get user's posts: " + split[0]);
        }
    }

}
