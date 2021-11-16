package com.badfic.philbot.data.phil;

import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseQuoteRepository<T extends BaseQuote> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    boolean existsByMessageId(long messageId);

    @Query("select id from #{#entityName}")
    List<Long> findAllIds();

    @Query("select image from #{#entityName} where image is not null")
    Set<String> findAllNonNullImages();

}
