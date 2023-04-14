package com.badfic.philbot.data;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ReminderRepository extends ListCrudRepository<Reminder, Long> {
}
