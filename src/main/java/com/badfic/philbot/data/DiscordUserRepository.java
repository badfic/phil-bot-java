package com.badfic.philbot.data;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordUserRepository extends JpaRepository<DiscordUser, String>, JpaSpecificationExecutor<DiscordUser> {

    Optional<DiscordUser> findByTriviaGuid(UUID triviaGuid);

}
