package com.badfic.philbot.data.hungersim;

import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoundOutcomeRepository extends ListCrudRepository<RoundOutcome, Long> {

    List<RoundOutcome> findByRound(Long round);

    List<RoundOutcome> findByOutcome(Long outcome);

    boolean existsByRoundAndOutcome(Long round, Long outcome);

}
