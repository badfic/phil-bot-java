package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.data.phil.PhilResponsesConfig;
import com.badfic.philbot.data.phil.PhilResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class PhilCommand extends BasicResponsesBot<PhilResponsesConfig> {

    public PhilCommand(ObjectMapper objectMapper, PhilResponsesConfigRepository philResponsesConfigRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        super(philResponsesConfigRepository, jdbcAggregateTemplate, objectMapper, "phil", PhilResponsesConfig::new);
    }

}
