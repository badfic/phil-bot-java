package com.badfic.philbot.data;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class SwampyGamesConfigDao {

    private final SwampyGamesConfigRepository swampyGamesConfigRepository;
    private final AtomicReference<SwampyGamesConfig> reference;

    public SwampyGamesConfigDao(SwampyGamesConfigRepository swampyGamesConfigRepository) {
        this.swampyGamesConfigRepository = swampyGamesConfigRepository;

        SwampyGamesConfig config = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID).orElseGet(() -> {
            SwampyGamesConfig singleton = new SwampyGamesConfig();
            singleton.setId(SwampyGamesConfig.SINGLETON_ID);
            return swampyGamesConfigRepository.save(singleton);
        });

        reference = new AtomicReference<>(config);
    }

    public SwampyGamesConfig getSwampyGamesConfig()  {
        return reference.get();
    }

    public SwampyGamesConfig saveSwampyGamesConfig(SwampyGamesConfig swampyGamesConfig) {
        SwampyGamesConfig saved = swampyGamesConfigRepository.save(swampyGamesConfig);
        reference.set(saved);
        return saved;
    }

}
