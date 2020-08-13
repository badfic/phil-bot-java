package com.badfic.philbot.listeners;

import com.badfic.philbot.config.PhilbotAppConfig;
import com.badfic.philbot.model.PhilConfigJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PhilCommand extends Command {

    private final boolean isTestEnvironment;
    private final PhilConfigJson kidFriendlyConfig;
    private final PhilConfigJson nsfwConfig;
    private final PhilConfigJson testConfig;

    @Autowired
    public PhilCommand(ObjectMapper objectMapper, PhilbotAppConfig philbotAppConfig) throws Exception {
        isTestEnvironment = "test".equalsIgnoreCase(philbotAppConfig.nodeEnvironment);
        name = "phil";
        help = "provides a random message";
        aliases = new String[] {"klemmer", "KLEMMER", "PHIL"};
        kidFriendlyConfig = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("kidFriendlyConfig.json"), PhilConfigJson.class);
        nsfwConfig = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("nsfwConfig.json"), PhilConfigJson.class);
        nsfwConfig.getResponses().addAll(kidFriendlyConfig.getResponses());

        testConfig = new PhilConfigJson();
        testConfig.setChannels(Collections.singleton("test-channel"));
        testConfig.setResponses(nsfwConfig.getResponses());
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String channelName = event.getChannel().getName();

        List<String> responses;
        if (isTestEnvironment) {
            if (testConfig.getChannels().contains(channelName)) {
                responses = testConfig.getResponses();
            } else {
                return;
            }
        } else {
            if (kidFriendlyConfig.getChannels().contains(channelName)) {
                responses = kidFriendlyConfig.getResponses();
            } else if (nsfwConfig.getChannels().contains(channelName)) {
                responses = nsfwConfig.getResponses();
            } else {
                return;
            }
        }

        int idx = ThreadLocalRandom.current().nextInt(responses.size());
        event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", " + responses.get(idx)).queue();
    }

}
