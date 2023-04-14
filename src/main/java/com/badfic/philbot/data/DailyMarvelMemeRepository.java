package com.badfic.philbot.data;

import java.util.Set;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyMarvelMemeRepository extends ListCrudRepository<DailyMarvelMemeEntity, Long> {

    @Query("SELECT message_id FROM daily_marvel_meme")
    Set<Long> findAllIds();

}
