package com.badfic.philbot.listeners.keanu;

import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.keanu.KeanuResponsesConfig;
import com.badfic.philbot.data.keanu.KeanuResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class KeanuCommand extends BasicResponsesBot<KeanuResponsesConfig> {

    public KeanuCommand(KeanuResponsesConfigRepository keanuResponsesConfigRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        super(keanuResponsesConfigRepository, jdbcAggregateTemplate, "keanu", KeanuResponsesConfig::new,
                SwampyGamesConfig::getKeanuNickname, SwampyGamesConfig::getKeanuAvatar);
    }

}
