package com.badfic.philbot.data.phil;

import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HungerGamesWinnerRepository extends JpaRepository<HungerGamesWinnerEntity, Long>, JpaSpecificationExecutor<HungerGamesWinnerEntity> {

    @Query("SELECT messageId FROM HungerGamesWinnerEntity")
    Set<Long> findAllIds();

}
