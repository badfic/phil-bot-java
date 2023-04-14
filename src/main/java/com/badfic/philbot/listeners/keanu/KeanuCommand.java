package com.badfic.philbot.listeners.keanu;

import com.badfic.philbot.data.keanu.KeanuResponsesConfig;
import com.badfic.philbot.data.keanu.KeanuResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class KeanuCommand extends BasicResponsesBot<KeanuResponsesConfig> {

    public KeanuCommand(ObjectMapper objectMapper, KeanuResponsesConfigRepository keanuResponsesConfigRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        super(keanuResponsesConfigRepository, jdbcAggregateTemplate, objectMapper, "keanu", KeanuResponsesConfig::new);
    }

}
