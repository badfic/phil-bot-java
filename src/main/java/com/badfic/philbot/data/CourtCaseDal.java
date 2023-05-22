package com.badfic.philbot.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class CourtCaseDal {
    private final CourtCaseRepository courtCaseRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;
    private final Long2ObjectMap<CourtCase> cache;

    public CourtCaseDal(CourtCaseRepository courtCaseRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.courtCaseRepository = courtCaseRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
        cache = Long2ObjectMaps.synchronize(new Long2ObjectArrayMap<>());

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

    public void deleteById(long id) {
        courtCaseRepository.deleteById(id);
        cache.remove(id);
    }

    public Optional<CourtCase> findById(long id) {
        return Optional.ofNullable(cache.get(id));
    }

    public Collection<CourtCase> findAll() {
        return cache.values();
    }

}
