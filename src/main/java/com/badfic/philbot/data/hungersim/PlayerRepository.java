package com.badfic.philbot.data.hungersim;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends ListCrudRepository<Player, Long> {

    Optional<Player> findByDiscordUser(String discordUserId);

}
