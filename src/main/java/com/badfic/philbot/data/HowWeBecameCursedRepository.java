package com.badfic.philbot.data;

import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HowWeBecameCursedRepository extends JpaRepository<HowWeBecameCursedEntity, Long>, JpaSpecificationExecutor<HowWeBecameCursedEntity> {

    @Query("SELECT messageId FROM HowWeBecameCursedEntity")
    Set<Long> findAllIds();

}
