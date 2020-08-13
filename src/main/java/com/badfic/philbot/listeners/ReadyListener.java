package com.badfic.philbot.listeners;

import java.lang.invoke.MethodHandles;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReadyListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("Received ready event for [user={}]", event.getJDA().getSelfUser());
    }

}
