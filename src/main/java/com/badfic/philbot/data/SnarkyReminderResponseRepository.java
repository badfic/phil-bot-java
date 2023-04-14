package com.badfic.philbot.data;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnarkyReminderResponseRepository extends ListCrudRepository<SnarkyReminderResponse, Long> {
}
