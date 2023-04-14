package com.badfic.philbot.data;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseQuoteRepository<T extends BaseQuote> extends ListCrudRepository<T, Long> {

    boolean existsByMessageId(long messageId);

}
