package com.badfic.philbot.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SwampyGamesConfigRepository extends JpaRepository<SwampyGamesConfig, Short>, JpaSpecificationExecutor<SwampyGamesConfig> {
}
