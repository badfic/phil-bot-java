package com.badfic.philbot.data.hungersim;

import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoundRepository extends ListCrudRepository<Round, Long> {

    List<Round> findByOpeningRound(Boolean openingRound);

}
