package com.badfic.philbot.data.hungersim;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OutcomeRepository extends JpaRepository<Outcome, Long>, JpaSpecificationExecutor<Outcome> {
}
