package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.data.phil.PhilResponsesConfig;
import com.badfic.philbot.data.phil.PhilResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class PhilCommand extends BasicResponsesBot<PhilResponsesConfig> {

    public PhilCommand(ObjectMapper objectMapper, PhilResponsesConfigRepository philResponsesConfigRepository) {
        super(philResponsesConfigRepository, objectMapper, "phil", PhilResponsesConfig::new);
    }

}
