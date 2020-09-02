package com.badfic.philbot.repository;

import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.Phrase;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PhraseRepository extends JpaRepository<Phrase, UUID>, JpaSpecificationExecutor<Phrase> {

    List<Phrase> findAllByDiscordUser(DiscordUser discordUser);

    List<Phrase> findAllByDiscordUser_id(String id);

}
