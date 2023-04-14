package com.badfic.philbot.data;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class CourtCaseDao {
    private final CourtCaseRepository courtCaseRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;
    private final Cache<Long, CourtCase> cache;

    public CourtCaseDao(CourtCaseRepository courtCaseRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.courtCaseRepository = courtCaseRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
        cache = Caffeine.newBuilder().build();

        for (CourtCase courtCase : courtCaseRepository.findAll()) {
            cache.put(courtCase.getDefendantId(), courtCase);
        }
    }

    public CourtCase update(CourtCase courtCase) {
        CourtCase saved = courtCaseRepository.save(courtCase);
        cache.put(saved.getDefendantId(), saved);
        return saved;
    }

    public CourtCase insert(CourtCase courtCase) {
        CourtCase saved = jdbcAggregateTemplate.insert(courtCase);
        cache.put(saved.getDefendantId(), saved);
        return saved;
    }

    public void deleteById(Long id) {
        courtCaseRepository.deleteById(id);
        cache.invalidate(id);
    }

    public Optional<CourtCase> findById(Long id) {
        return Optional.ofNullable(cache.getIfPresent(id));
    }

    public Collection<CourtCase> findAll() {
        return cache.asMap().values();
    }

}
