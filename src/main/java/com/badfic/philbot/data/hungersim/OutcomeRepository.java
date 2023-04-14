package com.badfic.philbot.data.hungersim;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutcomeRepository extends ListCrudRepository<Outcome, Long> {
}
