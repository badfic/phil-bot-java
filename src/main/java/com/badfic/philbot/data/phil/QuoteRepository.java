package com.badfic.philbot.data.phil;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteRepository extends BaseQuoteRepository<Quote> {

    @Query(nativeQuery = true, value = "select extract(isodow from created) from quote order by date_part asc")
    int[] getQuoteDaysOfWeek();

    @Query(nativeQuery = true, value = "select mode() within group ( order by user_id ) from quote")
    long getMostQuotedUser();

}
