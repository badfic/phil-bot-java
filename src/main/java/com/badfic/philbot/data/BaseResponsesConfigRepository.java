package com.badfic.philbot.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseResponsesConfigRepository<T extends BaseResponsesConfig>  extends JpaRepository<T, Short>, JpaSpecificationExecutor<T> {
}
