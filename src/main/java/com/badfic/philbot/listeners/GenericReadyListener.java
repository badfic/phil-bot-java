package com.badfic.philbot.listeners;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.Constants;
import java.lang.invoke.MethodHandles;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenericReadyListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String commitSha;
    private final String commitMessage;

    @Autowired
    public GenericReadyListener(BaseConfig baseConfig) {
        this.commitSha = baseConfig.commitSha;
        this.commitMessage = baseConfig.commitMessage;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("Received ready event for [user={}]", event.getJDA().getSelfUser());
        MessageEmbed messageEmbed = Constants.simpleEmbed("Restarted",
                String.format("We just restarted\ngit sha: %s\ncommit msg: %s", commitSha, commitMessage), Constants.SWAMP_GREEN);
        event.getJDA().getTextChannelsByName("test-channel", false).get(0).sendMessage(messageEmbed).queue();
    }

}
