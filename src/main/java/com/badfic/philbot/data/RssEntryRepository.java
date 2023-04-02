package com.badfic.philbot.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RssEntryRepository extends JpaRepository<RssEntry, String>, JpaSpecificationExecutor<RssEntry> {

    long countByFeedUrl(String feedUrl);

}
