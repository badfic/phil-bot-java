package com.badfic.philbot.data;

import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordUserRepository extends ListCrudRepository<DiscordUser, String> {

    List<DiscordUser> findByXpGreaterThan(long number);

}
