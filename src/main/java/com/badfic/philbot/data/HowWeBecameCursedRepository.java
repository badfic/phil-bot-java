package com.badfic.philbot.data;

import java.util.Set;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HowWeBecameCursedRepository extends ListCrudRepository<HowWeBecameCursedEntity, Long> {

    @Query("SELECT message_id FROM how_we_became_cursed")
    Set<Long> findAllIds();

}
