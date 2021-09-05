package com.badfic.philbot.data.phil;

import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyMarvelMemeRepository extends JpaRepository<DailyMarvelMemeEntity, Long>, JpaSpecificationExecutor<DailyMarvelMemeEntity> {

    @Query("SELECT messageId FROM DailyMarvelMemeEntity")
    Set<Long> findAllIds();

}
