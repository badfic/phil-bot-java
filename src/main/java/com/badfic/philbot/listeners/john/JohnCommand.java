package com.badfic.philbot.listeners.john;

import com.badfic.philbot.data.john.JohnResponsesConfig;
import com.badfic.philbot.data.john.JohnResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JohnCommand extends BasicResponsesBot<JohnResponsesConfig> {

    public JohnCommand(ObjectMapper objectMapper, JohnResponsesConfigRepository johnResponsesConfigRepository) {
        super(johnResponsesConfigRepository, objectMapper, "john", JohnResponsesConfig::new);
    }

}
