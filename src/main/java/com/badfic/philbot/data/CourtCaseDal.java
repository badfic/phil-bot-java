package com.badfic.philbot.data;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class CourtCaseDal {
    private final CourtCaseRepository courtCaseRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public CourtCaseDal(CourtCaseRepository courtCaseRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.courtCaseRepository = courtCaseRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
    }

    public CourtCase update(CourtCase courtCase) {
        return courtCaseRepository.save(courtCase);
    }

    public CourtCase insert(CourtCase courtCase) {
        return jdbcAggregateTemplate.insert(courtCase);
    }

    public void deleteById(long id) {
        courtCaseRepository.deleteById(id);
    }

    public Optional<CourtCase> findById(long id) {
        return courtCaseRepository.findById(id);
    }

    public Collection<CourtCase> findAll() {
        return courtCaseRepository.findAll();
    }

}
