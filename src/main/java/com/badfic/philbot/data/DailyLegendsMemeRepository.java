package com.badfic.philbot.data;

import java.util.Set;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyLegendsMemeRepository extends ListCrudRepository<DailyLegendsMemeEntity, Long> {

    @Query("SELECT message_id FROM daily_legends_meme")
    Set<Long> findAllIds();

}
