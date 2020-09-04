package com.badfic.philbot.listeners;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.BehradMarker;
import com.badfic.philbot.config.KeanuMarker;
import com.badfic.philbot.config.PhilMarker;
import java.lang.invoke.MethodHandles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenericReadyListener extends ListenerAdapter implements PhilMarker, BehradMarker, KeanuMarker {
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
        MessageEmbed messageEmbed = new EmbedBuilder()
                .setTitle("Restarted")
                .setDescription(String.format("I just restarted\ngit sha: %s\ncommit msg: %s", commitSha, commitMessage))
                .build();
        event.getJDA().getTextChannelsByName("test-channel", false).get(0).sendMessage(messageEmbed).queue();
    }

}
