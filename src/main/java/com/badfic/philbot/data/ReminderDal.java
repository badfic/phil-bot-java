package com.badfic.philbot.data;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReminderDal {
    private final ReminderRepository reminderRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;
    private final Cache<Long, Reminder> cache;

    public ReminderDal(ReminderRepository reminderRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.reminderRepository = reminderRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
        cache = Caffeine.newBuilder().build();

        for (Reminder reminder : reminderRepository.findAll()) {
            cache.put(reminder.getId(), reminder);
        }
    }

    public Reminder insert(Reminder reminder) {
        Reminder saved = jdbcAggregateTemplate.insert(reminder);
        cache.put(saved.getId(), saved);
        return saved;
    }

    public void deleteById(Long id) {
        reminderRepository.deleteById(id);
        cache.invalidate(id);
    }

    public Optional<Reminder> findById(Long id) {
        return Optional.ofNullable(cache.getIfPresent(id));
    }

    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    public Collection<Reminder> findAll() {
        return cache.asMap().values();
    }
}
