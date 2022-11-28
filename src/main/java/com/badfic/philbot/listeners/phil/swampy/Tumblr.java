package com.badfic.philbot.listeners.phil.swampy;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Post;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Tumblr extends BaseSwampy {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private JumblrClient jumblrClient;

    public Tumblr() {
        name = "tumblr";
        help = "!!tumblr username\nShow this tumblr users latest post";
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
            logger.error("Failed to get [user={}] tumblr posts", split[0], e);
            honeybadgerReporter.reportError(e, "Failed to get user's tumblr posts: " + split[0]);
            event.replyError("Something went wrong trying to get user's posts: " + split[0]);
            throw e;
        }
    }

}
