package com.badfic.philbot.data;

import com.badfic.philbot.config.Constants;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class SwampyGamesConfigDal {

    private final SwampyGamesConfigRepository swampyGamesConfigRepository;

    public SwampyGamesConfigDal(SwampyGamesConfigRepository swampyGamesConfigRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.swampyGamesConfigRepository = swampyGamesConfigRepository;

        swampyGamesConfigRepository.findById(Constants.DATA_SINGLETON_ID).orElseGet(() -> {
            SwampyGamesConfig singleton = new SwampyGamesConfig();
            singleton.setId(Constants.DATA_SINGLETON_ID);
            return jdbcAggregateTemplate.insert(singleton);
        });
    }

    public SwampyGamesConfig get()  {
        return swampyGamesConfigRepository.findById(Constants.DATA_SINGLETON_ID).orElseThrow();
    }

    public SwampyGamesConfig update(SwampyGamesConfig swampyGamesConfig) {
        return swampyGamesConfigRepository.save(swampyGamesConfig);
    }

}
