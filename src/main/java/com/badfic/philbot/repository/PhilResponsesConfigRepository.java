package com.badfic.philbot.repository;

import com.badfic.philbot.data.PhilResponsesConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PhilResponsesConfigRepository extends JpaRepository<PhilResponsesConfig, Short>, JpaSpecificationExecutor<PhilResponsesConfig> {
}
