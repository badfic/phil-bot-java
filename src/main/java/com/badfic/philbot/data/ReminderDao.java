package com.badfic.philbot.data;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Collection;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ReminderDao {
    private final ReminderRepository reminderRepository;
    private final Cache<Long, Reminder> cache;

    public ReminderDao(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
        cache = Caffeine.newBuilder().build();

        for (Reminder reminder : reminderRepository.findAll()) {
            cache.put(reminder.getId(), reminder);
        }
    }

    public Reminder save(Reminder reminder) {
        Reminder saved = reminderRepository.save(reminder);
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
