package com.badfic.philbot.data;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class SwampyGamesConfigDao {

    private final SwampyGamesConfigRepository swampyGamesConfigRepository;
    private final AtomicReference<SwampyGamesConfig> reference;

    public SwampyGamesConfigDao(SwampyGamesConfigRepository swampyGamesConfigRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.swampyGamesConfigRepository = swampyGamesConfigRepository;

        SwampyGamesConfig config = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID).orElseGet(() -> {
            SwampyGamesConfig singleton = new SwampyGamesConfig();
            singleton.setId(SwampyGamesConfig.SINGLETON_ID);
            return jdbcAggregateTemplate.insert(singleton);
        });

        reference = new AtomicReference<>(config);
    }

    public SwampyGamesConfig get()  {
        return reference.get();
    }

    public SwampyGamesConfig update(SwampyGamesConfig swampyGamesConfig) {
        SwampyGamesConfig saved = swampyGamesConfigRepository.save(swampyGamesConfig);
        reference.set(saved);
        return saved;
    }

}
