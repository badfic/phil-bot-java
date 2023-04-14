package com.badfic.philbot.data;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface CourtCaseRepository extends ListCrudRepository<CourtCase, Long> {
}
