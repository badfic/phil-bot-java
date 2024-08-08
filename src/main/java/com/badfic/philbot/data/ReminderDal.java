package com.badfic.philbot.data;

import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReminderDal {
    private final ReminderRepository reminderRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public Reminder insert(final Reminder reminder) {
        return jdbcAggregateTemplate.insert(reminder);
    }

    public void deleteById(final long id) {
        reminderRepository.deleteById(id);
    }

    public Optional<Reminder> findById(final long id) {
        return reminderRepository.findById(id);
    }

    public boolean existsById(final long id) {
        return findById(id).isPresent();
    }

    public Collection<Reminder> findAll() {
        return reminderRepository.findAll();
    }
}
