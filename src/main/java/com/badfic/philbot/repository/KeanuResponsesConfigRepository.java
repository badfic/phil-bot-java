package com.badfic.philbot.repository;

import com.badfic.philbot.data.keanu.KeanuResponsesConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface KeanuResponsesConfigRepository extends JpaRepository<KeanuResponsesConfig, Short>, JpaSpecificationExecutor<KeanuResponsesConfig> {
}
