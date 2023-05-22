package com.badfic.philbot.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReminderDal {
    private final ReminderRepository reminderRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;
    private final Long2ObjectMap<Reminder> cache;

    public ReminderDal(ReminderRepository reminderRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.reminderRepository = reminderRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
        cache = Long2ObjectMaps.synchronize(new Long2ObjectArrayMap<>());

        for (Reminder reminder : reminderRepository.findAll()) {
            cache.put(reminder.getId(), reminder);
        }
    }

    public Reminder insert(Reminder reminder) {
        Reminder saved = jdbcAggregateTemplate.insert(reminder);
        cache.put(saved.getId(), saved);
        return saved;
    }

    public void deleteById(long id) {
        reminderRepository.deleteById(id);
        cache.remove(id);
    }

    public Optional<Reminder> findById(long id) {
        return Optional.ofNullable(cache.get(id));
    }

    public boolean existsById(long id) {
        return findById(id).isPresent();
    }

    public Collection<Reminder> findAll() {
        return cache.values();
    }
}
