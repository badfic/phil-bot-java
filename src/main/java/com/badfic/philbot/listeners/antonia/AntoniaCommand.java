package com.badfic.philbot.listeners.antonia;

import com.badfic.philbot.data.antonia.AntoniaResponsesConfig;
import com.badfic.philbot.data.antonia.AntoniaResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class AntoniaCommand extends BasicResponsesBot<AntoniaResponsesConfig> {

    public AntoniaCommand(ObjectMapper objectMapper, AntoniaResponsesConfigRepository antoniaResponsesConfigRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        super(antoniaResponsesConfigRepository, jdbcAggregateTemplate, objectMapper, "antonia", AntoniaResponsesConfig::new);
    }

}
