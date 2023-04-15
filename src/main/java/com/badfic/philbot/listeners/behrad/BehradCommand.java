package com.badfic.philbot.listeners.behrad;

import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.behrad.BehradResponsesConfig;
import com.badfic.philbot.data.behrad.BehradResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class BehradCommand extends BasicResponsesBot<BehradResponsesConfig> {

    public BehradCommand(BehradResponsesConfigRepository behradResponsesConfigRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        super(behradResponsesConfigRepository, jdbcAggregateTemplate, "behrad", BehradResponsesConfig::new,
                SwampyGamesConfig::getBehradNickname, SwampyGamesConfig::getBehradAvatar);
    }

}
