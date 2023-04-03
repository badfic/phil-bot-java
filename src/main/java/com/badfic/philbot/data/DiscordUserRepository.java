package com.badfic.philbot.data;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordUserRepository extends JpaRepository<DiscordUser, String>, JpaSpecificationExecutor<DiscordUser> {

    List<DiscordUser> findByXpGreaterThan(long number);

}
