package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.data.phil.PhilResponsesConfig;
import com.badfic.philbot.data.phil.PhilResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class PhilCommand extends BasicResponsesBot<PhilResponsesConfig> {

    public PhilCommand(PhilResponsesConfigRepository philResponsesConfigRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        super(philResponsesConfigRepository, jdbcAggregateTemplate, "phil", PhilResponsesConfig::new, null, null);
    }

}
