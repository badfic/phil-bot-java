package com.badfic.philbot.data;

import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourtCaseDal {
    private final CourtCaseRepository courtCaseRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public CourtCase update(final CourtCase courtCase) {
        return courtCaseRepository.save(courtCase);
    }

    public CourtCase insert(final CourtCase courtCase) {
        return jdbcAggregateTemplate.insert(courtCase);
    }

    public void deleteById(final long id) {
        courtCaseRepository.deleteById(id);
    }

    public Optional<CourtCase> findById(final long id) {
        return courtCaseRepository.findById(id);
    }

    public Collection<CourtCase> findAll() {
        return courtCaseRepository.findAll();
    }

}
