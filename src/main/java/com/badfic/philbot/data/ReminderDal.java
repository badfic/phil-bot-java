package com.badfic.philbot.data;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReminderDal {
    private final ReminderRepository reminderRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public ReminderDal(ReminderRepository reminderRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.reminderRepository = reminderRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
    }

    public Reminder insert(Reminder reminder) {
        return jdbcAggregateTemplate.insert(reminder);
    }

    public void deleteById(long id) {
        reminderRepository.deleteById(id);
    }

    public Optional<Reminder> findById(long id) {
        return reminderRepository.findById(id);
    }

    public boolean existsById(long id) {
        return findById(id).isPresent();
    }

    public Collection<Reminder> findAll() {
        return reminderRepository.findAll();
    }
}
