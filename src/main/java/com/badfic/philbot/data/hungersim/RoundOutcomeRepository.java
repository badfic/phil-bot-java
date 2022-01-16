package com.badfic.philbot.data.hungersim;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RoundOutcomeRepository extends JpaRepository<RoundOutcome, Long>, JpaSpecificationExecutor<RoundOutcome> {

    List<RoundOutcome> findByRound(Round round);

    List<RoundOutcome> findByOutcome(Outcome outcome);

    boolean existsByRoundAndOutcome(Round round, Outcome outcome);

}
