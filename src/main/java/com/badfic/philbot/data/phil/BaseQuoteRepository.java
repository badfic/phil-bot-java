package com.badfic.philbot.data.phil;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseQuoteRepository<T extends BaseQuote> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    boolean existsByMessageId(long messageId);

    @Query("select id from #{#entityName}")
    List<Long> findAllIds();

}
