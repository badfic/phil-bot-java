package com.badfic.philbot.data;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RssEntryRepository extends ListCrudRepository<RssEntry, String> {

    long countByFeedUrl(String feedUrl);

}
