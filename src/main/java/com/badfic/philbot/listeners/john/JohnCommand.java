package com.badfic.philbot.listeners.john;

import com.badfic.philbot.data.john.JohnResponsesConfig;
import com.badfic.philbot.data.john.JohnResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class JohnCommand extends BasicResponsesBot<JohnResponsesConfig> {

    public JohnCommand(ObjectMapper objectMapper, JohnResponsesConfigRepository johnResponsesConfigRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        super(johnResponsesConfigRepository, jdbcAggregateTemplate, objectMapper, "john", JohnResponsesConfig::new);
    }

}
