package com.badfic.philbot.listeners.behrad;

import com.badfic.philbot.data.behrad.BehradResponsesConfig;
import com.badfic.philbot.data.behrad.BehradResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class BehradCommand extends BasicResponsesBot<BehradResponsesConfig> {

    public BehradCommand(ObjectMapper objectMapper, BehradResponsesConfigRepository behradResponsesConfigRepository) {
        super(behradResponsesConfigRepository, objectMapper, "behrad", BehradResponsesConfig::new);
    }

}
