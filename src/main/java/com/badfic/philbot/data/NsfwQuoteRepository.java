package com.badfic.philbot.data;

import java.util.List;
import java.util.Set;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NsfwQuoteRepository extends BaseQuoteRepository<NsfwQuote> {

    @Query("select id from nsfw_quote")
    List<Long> findAllIds();

    @Query("select image from nsfw_quote where image is not null")
    Set<String> findAllNonNullImages();

    @Query("select extract(isodow from created) from nsfw_quote order by date_part('isodow', created) asc")
    int[] getQuoteDaysOfWeek();

    @Query("select mode() within group ( order by user_id ) from nsfw_quote")
    long getMostQuotedUser();

    @Query("select extract(isodow from created) from nsfw_quote where user_id = :userId order by date_part('isodow', created) asc")
    int[] getQuoteDaysOfWeekForUser(@Param("userId") long userId);

}
