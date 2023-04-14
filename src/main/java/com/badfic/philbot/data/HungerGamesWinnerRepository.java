package com.badfic.philbot.data;

import java.util.Set;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HungerGamesWinnerRepository extends ListCrudRepository<HungerGamesWinnerEntity, Long> {

    @Query("SELECT message_id FROM hunger_games_winner")
    Set<Long> findAllIds();

}
