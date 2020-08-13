package com.badfic.philbot.repository;

import com.badfic.philbot.data.DiscordUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DiscordUserRepository extends JpaRepository<DiscordUser, String>, JpaSpecificationExecutor<DiscordUser> {
}
