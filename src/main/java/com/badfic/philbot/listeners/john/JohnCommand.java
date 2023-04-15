package com.badfic.philbot.listeners.john;

import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.john.JohnResponsesConfig;
import com.badfic.philbot.data.john.JohnResponsesConfigRepository;
import com.badfic.philbot.listeners.BasicResponsesBot;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class JohnCommand extends BasicResponsesBot<JohnResponsesConfig> {

    public JohnCommand(JohnResponsesConfigRepository johnResponsesConfigRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        super(johnResponsesConfigRepository, jdbcAggregateTemplate, "john", JohnResponsesConfig::new,
                SwampyGamesConfig::getJohnNickname, SwampyGamesConfig::getJohnAvatar);
    }

}
