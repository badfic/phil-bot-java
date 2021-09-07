package com.badfic.philbot.data.phil;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MemeCommandRepository extends JpaRepository<MemeCommandEntity, String>, JpaSpecificationExecutor<MemeCommandEntity> {
}
