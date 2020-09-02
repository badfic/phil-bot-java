package com.badfic.philbot.repository;

import com.badfic.philbot.data.behrad.BehradResponsesConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BehradResponsesConfigRepository extends JpaRepository<BehradResponsesConfig, Short>, JpaSpecificationExecutor<BehradResponsesConfig> {
}
