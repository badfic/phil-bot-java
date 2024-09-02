package com.badfic.philbot.data;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DailyMemeRepository<T> extends ListCrudRepository<T, Long> {
}
